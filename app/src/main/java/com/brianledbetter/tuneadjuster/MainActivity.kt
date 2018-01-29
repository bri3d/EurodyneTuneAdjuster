package com.brianledbetter.tuneadjuster

import android.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.widget.CompoundButton
import com.brianledbetter.tuneadjuster.elm327.EurodyneIO

class MainActivity : AppCompatActivity(), AdjustFieldFragment.OnParameterAdjustedListener, BluetoothPickerDialogFragment.BluetoothDialogListener {
    private var fieldOneFragment: AdjustFieldFragment? = null
    private var fieldTwoFragment: AdjustFieldFragment? = null
    private var boostValue = 0
    private var octaneValue = 0

    private var serviceReceiveMessenger = Messenger(Handler({ message ->
       handleMessage(message)
        true
    }))

    private var serviceMessenger : Messenger? = null

    inner class BluetoothConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            val statusIntent = Intent(BluetoothService.GET_CONNECTION_STATUS)
            serviceMessenger?.send(messageWithIntent(statusIntent))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isActive = false
        }
    }

    private var serviceConnection = BluetoothConnection()

    private var isActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, BluetoothService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection,
                Context.BIND_AUTO_CREATE)

        isActive = savedInstanceState?.getBoolean("Active") ?: false

        connectionSwitch.setOnCheckedChangeListener({ _: CompoundButton, isChecked: Boolean ->
            if (isChecked && !isActive) {
                startConnection()
            } else if (!isChecked) {
                stopConnection()
            }
        })

        button.setOnClickListener({_ ->
            saveBoostAndOctane()
        } )
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putBoolean("Active", isActive)
        super.onSaveInstanceState(outState, outPersistentState)

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun startConnection() {
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

    private fun stopConnection() {
        val stopIntent = Intent(BluetoothService.STOP_CONNECTION)
        serviceMessenger?.send(messageWithIntent(stopIntent))
    }

    private fun saveBoostAndOctane() {
        statusLabel.text = resources.getString(R.string.saving)
        val saveIntent = Intent("SaveBoostAndOctane")
        saveIntent.putExtra("BoostInfo", EurodyneIO.BoostInfo(0,0, boostValue))
        saveIntent.putExtra("OctaneInfo", EurodyneIO.OctaneInfo(0, 0, octaneValue))
        serviceMessenger?.send(messageWithIntent(saveIntent))
    }

    private fun handleMessage(message: Message) {
        val intent = message.obj as? Intent
        when (intent?.action) {
            BluetoothService.SOCKET_CLOSED, BluetoothService.CONNECTION_NOT_ACTIVE -> {
                statusLabel.text = resources.getString(R.string.not_connected)
                isActive = false
                connectionSwitch.isChecked = false
            }
            BluetoothService.CONNECTION_ACTIVE -> {
                isActive = true
                connectionSwitch.isChecked = true
                statusLabel.text = resources.getString(R.string.connecting)
            }
            else -> {
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
        stopConnection()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, selectedDevice: String) {
        val connectIntent = Intent(BluetoothService.START_CONNECTION)
        connectIntent.putExtra("BluetoothDevice", selectedDevice)
        serviceMessenger?.send(messageWithIntent(connectIntent))
    }

    private fun messageWithIntent(i : Intent) : Message {
        val message = Message()
        message.obj = i
        message.replyTo = serviceReceiveMessenger
        return message
    }
}
