package com.brianledbetter.tuneadjuster

import android.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.os.Parcelable
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.widget.CompoundButton
import com.brianledbetter.tuneadjuster.elm327.BluetoothThread
import com.brianledbetter.tuneadjuster.elm327.EurodyneIO

class MainActivity : AppCompatActivity(), AdjustFieldFragment.OnParameterAdjustedListener, BluetoothPickerDialogFragment.BluetoothDialogListener {
    private var fieldOneFragment: AdjustFieldFragment? = null
    private var fieldTwoFragment: AdjustFieldFragment? = null

    private var handler: Handler = Handler({ message ->
       handleMessage(message)
        true
    })

    private var boostValue = 0
    private var octaneValue = 0

    private var bluetoothThread : BluetoothThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectionSwitch.setOnCheckedChangeListener({ _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                startConnection()
            } else {
                stopConnection()
            }
        })

        button.setOnClickListener({_ ->
            saveBoostAndOctane()
        } )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun startConnection() {
        statusLabel.text = resources.getString(R.string.connecting)
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
        val stopIntent = Intent("StopConnection")
        val stopMessage = bluetoothThread?.handler?.obtainMessage()
        stopMessage?.obj = stopIntent
        bluetoothThread?.handler?.sendMessage(stopMessage)
    }

    fun saveBoostAndOctane() {
        statusLabel.text = resources.getString(R.string.saving)
        val saveIntent = Intent("SaveBoostAndOctane")
        saveIntent.putExtra("BoostInfo", EurodyneIO.BoostInfo(0,0, boostValue))
        saveIntent.putExtra("OctaneInfo", EurodyneIO.OctaneInfo(0, 0, octaneValue))
        val saveMessage = bluetoothThread?.handler?.obtainMessage()
        saveMessage?.obj = saveIntent
        bluetoothThread?.handler?.sendMessage(saveMessage)
    }

    fun handleMessage(message: Message) {
        val intent = message.obj as? Intent
        if (intent?.action == "SocketClosed") {
            statusLabel.text = resources.getString(R.string.not_connected)
            connectionSwitch.isChecked = false
        } else {
            val octaneData = intent?.getParcelableExtra<EurodyneIO.OctaneInfo>("octaneInfo")
            val boostData = intent?.getParcelableExtra<EurodyneIO.BoostInfo>("boostInfo")
            if (octaneData != null && boostData != null) {
                statusLabel.text = resources.getString(R.string.got_data)
                fieldOneFragment = AdjustFieldFragment.newInstance(octaneData.minimum, octaneData.maximum, octaneData.current, "Octane")
                fieldTwoFragment = AdjustFieldFragment.newInstance(boostData.minimum, boostData.maximum, boostData.current, "Boost")
                boostValue = boostData.current
                octaneValue = octaneData.current
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fieldOneFragmentContainer, fieldOneFragment, "fieldOne")
                        .replace(R.id.fieldTwoFragmentContainer, fieldTwoFragment, "fieldTwo")
                        .commit()
            }
        }
    }

    override fun onParameterAdjusted(name: String, value: Int) {
        if (name == "Octane") {
            octaneValue = value
        }
        if (name == "Boost") {
            boostValue = value
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }

    override fun onDialogPositiveClick(dialog: DialogFragment, selectedDevice: String) {
        val b = BluetoothAdapter.getDefaultAdapter()
        val device = b.getRemoteDevice(selectedDevice)
        bluetoothThread = BluetoothThread(device, handler)
        bluetoothThread?.start()
    }
}
