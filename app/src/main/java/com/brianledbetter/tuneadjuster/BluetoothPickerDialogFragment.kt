package com.brianledbetter.tuneadjuster

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.Button

import android.content.Context

/**
 * Created by b3d on 12/19/15.
 */
class BluetoothPickerDialogFragment : DialogFragment() {
    var mPossibleDevices: Array<BluetoothDevice?> = emptyArray()
    private var mSelectedDevice: String? = null
    private var mListener: BluetoothDialogListener? = null
    private var mOKButton: Button? = null

    interface BluetoothDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, selectedDevice: String)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog
        mOKButton = d.getButton(Dialog.BUTTON_POSITIVE)
        mOKButton?.isEnabled = mPossibleDevices.isNotEmpty()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = activity as? BluetoothDialogListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState != null) {
            mPossibleDevices = savedInstanceState.getParcelableArray("bluetoothDevices") as Array<BluetoothDevice?>
        }
        val builder = AlertDialog.Builder(activity)
        val bluetoothDevices = arrayOfNulls<CharSequence>(mPossibleDevices.size)
        for (i in mPossibleDevices.indices) {
            bluetoothDevices[i] = (mPossibleDevices[i] as BluetoothDevice).name
        }
        mSelectedDevice = (mPossibleDevices[0] as BluetoothDevice).address
        builder.setTitle(R.string.pick_bluetooth)
                .setSingleChoiceItems(bluetoothDevices, 0
                ) { _, which -> mSelectedDevice = (mPossibleDevices[which])?.address }
                // Set the action buttons
                .setPositiveButton(R.string.ok) { _, _ ->
                    mListener?.onDialogPositiveClick(this@BluetoothPickerDialogFragment, mSelectedDevice.toString())
                    dismiss()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    mListener?.onDialogNegativeClick(this@BluetoothPickerDialogFragment)
                    dismiss()
                }
        return builder.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArray("bluetoothDevices", mPossibleDevices)
    }
}