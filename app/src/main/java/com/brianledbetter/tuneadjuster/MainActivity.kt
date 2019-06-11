package com.brianledbetter.tuneadjuster

import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.widget.CompoundButton
import androidx.fragment.app.FragmentActivity
import com.brianledbetter.tuneadjuster.elm327.EcuIO
import com.brianledbetter.tuneadjuster.elm327.EurodyneIO

class MainActivity : FragmentActivity(), AdjustFieldFragment.OnParameterAdjustedListener, BluetoothPickerDialogFragment.BluetoothDialogListener {
    private var boostValue = 0
    private var octaneValue = 0
    private var e85Value = 0
    private var boostEnabled = false
    private var octaneEnabled = false
    private var e85Enabled = false

    private var serviceReceiveMessenger = Messenger(Handler { message ->
        handleMessage(message)
        true
    })

    private var serviceMessenger : Messenger? = null

    inner class BluetoothConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            val statusIntent = Intent(ServiceActions.Requests.GET_CONNECTION_STATUS)
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

        connectionSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked && !isActive) {
                startConnection()
            } else if (!isChecked) {
                stopConnection()
            }
        }

        button.setOnClickListener { _ ->
            saveBoostAndOctane()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putBoolean("Active", isActive)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun startConnection() {
        val b = BluetoothAdapter.getDefaultAdapter()

        if (b == null) {
            Toast.makeText(applicationContext, "ERROR! " + "No Bluetooth Device available!", Toast.LENGTH_LONG).show()
            return
        }

        if (!b.isEnabled) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnOn, 0)
        }

        val devices = b.bondedDevices.toTypedArray()
        if (devices.isNotEmpty()) {
            val bpdf = BluetoothPickerDialogFragment()
            bpdf.mPossibleDevices = devices
            bpdf.show(supportFragmentManager, "BluetoothPickerDialogFragment")
        } else {
            Toast.makeText(applicationContext, "ERROR! " + "No Bluetooth Device available!", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopConnection() {
        val stopIntent = Intent(ServiceActions.Requests.STOP_CONNECTION)
        serviceMessenger?.send(messageWithIntent(stopIntent))
    }

    private fun saveBoostAndOctane() {
        statusLabel.text = resources.getString(R.string.saving)
        val saveIntent = Intent(ServiceActions.Requests.SAVE)
        saveIntent.putExtra("BoostInfo", EurodyneIO.BoostInfo(0,0, boostValue))
        saveIntent.putExtra("OctaneInfo", EurodyneIO.OctaneInfo(0, 0, octaneValue))
        if(e85Enabled) {
            saveIntent.putExtra("e85Info", EurodyneIO.E85Info(e85Value))
        }
        serviceMessenger?.send(messageWithIntent(saveIntent))
    }

    private fun handleMessage(message: Message) {
        val intent = message.obj as? Intent
        when (intent?.action) {
            ServiceActions.Responses.SOCKET_CLOSED, ServiceActions.Responses.CONNECTION_NOT_ACTIVE -> {
                statusLabel.text = resources.getString(R.string.not_connected)
                isActive = false
                connectionSwitch.isChecked = false
            }
            ServiceActions.Responses.CONNECTION_ACTIVE -> {
                isActive = true
                connectionSwitch.isChecked = true
                statusLabel.text = resources.getString(R.string.connecting)
            }
            ServiceActions.Responses.CONNECTED -> {
                statusLabel.text = resources.getString(R.string.connected)
                serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.FETCH_ECU_DATA)))

            }
            ServiceActions.Responses.ECU_DATA -> {
                serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.FETCH_FEATURE_FLAGS)))
                val ecuIdData = intent.getParcelableExtra<EcuIO.EcuInfo>("ecuInfo")
                val ecuIdFragment = EcuIdFragment.newInstance(ecuIdData.swNumber, ecuIdData.swVersion, ecuIdData.vinNumber)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.ecuIdFragmentContainer, ecuIdFragment, "ecuId")
                        .commit()
            }
            ServiceActions.Responses.FEATURE_FLAGS -> {
                val featureFlags = intent.getParcelableExtra<EurodyneIO.FeatureFlagInfo>("featureFlags")
                if (featureFlags != null) {
                    boostEnabled = featureFlags.boostEnabled
                    octaneEnabled = featureFlags.octaneEnabled
                    e85Enabled = featureFlags.e85Enabled
                }
                serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.FETCH_TUNE_DATA)))
            }
            ServiceActions.Responses.TUNE_DATA -> {
                statusLabel.text = resources.getString(R.string.got_data)
                val octaneData = intent.getParcelableExtra<EurodyneIO.OctaneInfo>("octaneInfo")
                val boostData = intent.getParcelableExtra<EurodyneIO.BoostInfo>("boostInfo")
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                if (octaneEnabled && octaneData != null) {
                    val newFragment = AdjustFieldFragment.newInstance(octaneData.minimum, octaneData.maximum, octaneData.current, "Octane")
                    fragmentTransaction.replace(R.id.fieldOneFragmentContainer, newFragment, "fieldOne")
                }
                if (boostEnabled && boostData != null) {
                    val newFragment = AdjustFieldFragment.newInstance(boostData.minimum, boostData.maximum, boostData.current, "Boost")
                    fragmentTransaction.replace(R.id.fieldTwoFragmentContainer, newFragment, "fieldTwo")
                }
                fragmentTransaction.commit()
                if (e85Enabled) {
                    serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.FETCH_E85)))
                }
            }
            ServiceActions.Responses.E85_DATA -> {
                val e85Data = intent.getParcelableExtra<EurodyneIO.E85Info>("e85Info")
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                if (e85Enabled && e85Data != null) {
                    val newFragment =  AdjustFieldFragment.newInstance(0, 100, e85Data.current, "Alternative Fuel Content")
                    fragmentTransaction.replace(R.id.fieldThreeFragmentContainer, newFragment, "fieldThree")
                }
                fragmentTransaction.commit()
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
        if (name == "Alternative Fuel Content") {
            e85Value = value
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        stopConnection()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, selectedDevice: String) {
        val connectIntent = Intent(ServiceActions.Requests.START_CONNECTION)
        connectIntent.putExtra("BluetoothDevice", selectedDevice)
        serviceMessenger?.send(messageWithIntent(connectIntent))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val serviceIntent = Intent(this, BluetoothService::class.java)
        stopService(serviceIntent)
    }

    private fun messageWithIntent(i : Intent) : Message {
        val message = Message()
        message.obj = i
        message.replyTo = serviceReceiveMessenger
        return message
    }
}
