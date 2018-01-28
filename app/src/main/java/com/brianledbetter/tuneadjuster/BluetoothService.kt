package com.brianledbetter.tuneadjuster

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.brianledbetter.tuneadjuster.elm327.BluetoothThread

class BluetoothService: Service() {
    private var btThread : BluetoothThread? = null
    private var threadMessenger : Messenger? = null
    private val messageToActivityHandler = Handler() { message ->
        val messageIntent = message.obj as Intent
        threadMessenger?.send(messageWithIntent(messageIntent))
        if (messageIntent.action == "SocketClosed") {
            btThread = null
        }
        true
    }

    private val fromActivityMessenger = Messenger(Handler() { message ->
        val messageIntent = message.obj as Intent

        if (message.replyTo != null) {
            threadMessenger = message.replyTo
        }

        when (messageIntent.action) {
            "StartConnection" -> {
                val selectedDevice = messageIntent?.getStringExtra("BluetoothDevice")
                val b = BluetoothAdapter.getDefaultAdapter()
                val bluetoothDevice = b.getRemoteDevice(selectedDevice)
                btThread = BluetoothThread(bluetoothDevice, Messenger(messageToActivityHandler))
                btThread?.start()
                threadMessenger?.send(messageWithIntent(getStatusIntent()))
            }
            "GetConnectionStatus" -> {
                threadMessenger?.send(messageWithIntent(getStatusIntent()))
            }
            else -> { // Forward on to connection thread
                val message = btThread?.handler?.obtainMessage()
                message?.obj = messageIntent
                btThread?.handler?.sendMessage(message)
            }
        }
        true
    })

    private fun getStatusIntent() : Intent {
        if (btThread != null) {
            return Intent("ConnectionActive")
        } else {
            return Intent("ConnectionNotActive")
        }
    }

    private fun messageWithIntent(i: Intent) : Message {
        val message = Message()
        message.obj = i
        return message
    }

    override fun onBind(intent: Intent?): IBinder {
        return fromActivityMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }
}