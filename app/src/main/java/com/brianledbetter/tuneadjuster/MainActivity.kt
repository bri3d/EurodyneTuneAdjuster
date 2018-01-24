package com.brianledbetter.tuneadjuster

import android.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.os.Parcelable
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.widget.CompoundButton
import com.brianledbetter.tuneadjuster.elm327.ConnectThread
import com.brianledbetter.tuneadjuster.elm327.EurodyneIO

class MainActivity : AppCompatActivity(), AdjustFieldFragment.OnParameterAdjustedListener, BluetoothPickerDialogFragment.BluetoothDialogListener {
    private var fieldOneFragment: AdjustFieldFragment? = null
    private var fieldTwoFragment: AdjustFieldFragment? = null
    private var receiver: Receiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        receiver = Receiver(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("TuneData"))
        connectionSwitch.setOnCheckedChangeListener({ buttonView: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                startConnection()
            } else {
                stopConnection()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    fun startConnection() {
        val b = BluetoothAdapter.getDefaultAdapter()

        if (!b.isEnabled) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnOn, 0)
        }

        val devices = b.bondedDevices.toTypedArray()
        if (devices.isNotEmpty()) {
            val bpdf = BluetoothPickerDialogFragment()
            bpdf.mPossibleDevices = (devices as Array<Parcelable>)
            bpdf.show(fragmentManager, "BluetoothPickerDialogFragment")
        } else {
            Toast.makeText(applicationContext, "ERROR! " + "No Bluetooth Device available!", Toast.LENGTH_LONG).show()
        }
    }

    fun stopConnection() {
    }

    override fun onParameterAdjusted(value: Int) {
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }

    override fun onDialogPositiveClick(dialog: DialogFragment, selectedDevice: String) {
        val b = BluetoothAdapter.getDefaultAdapter()
        val device = b.getRemoteDevice(selectedDevice)
        val connectThread = ConnectThread(device, this)
        connectThread.start()
    }

    class Receiver(val activity : MainActivity) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "TuneData") {
                val octaneData = intent?.getParcelableExtra<EurodyneIO.OctaneInfo>("octaneInfo")
                val boostData = intent?.getParcelableExtra<EurodyneIO.BoostInfo>("boostInfo")
                if (octaneData != null && boostData != null) {
                    activity.fieldOneFragment = AdjustFieldFragment.newInstance(octaneData.minimum, octaneData.maximum, "Octane")
                    activity.fieldTwoFragment = AdjustFieldFragment.newInstance(boostData.minimum, boostData.maximum, "Boost")
                    activity.fieldOneFragment?.setValueFromData(octaneData.current)
                    activity.fieldTwoFragment?.setValueFromData(boostData.current)
                    activity.supportFragmentManager.beginTransaction()
                            .add(R.id.fieldOneFragmentContainer, activity.fieldOneFragment, "fieldOne")
                            .add(R.id.fieldTwoFragmentContainer, activity.fieldTwoFragment, "fieldTwo")
                            .commit()
                }
            }
        }
    }

}
