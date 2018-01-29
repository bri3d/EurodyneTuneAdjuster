package com.brianledbetter.tuneadjuster

import android.app.Notification
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.brianledbetter.tuneadjuster.elm327.BluetoothThread
import android.app.PendingIntent

class BluetoothService: Service() {
    private var btThread : BluetoothThread? = null
    private var threadMessenger : Messenger? = null
    private val messageToActivityHandler = Handler() { message ->
        val messageIntent = message.obj as Intent
        threadMessenger?.send(messageWithIntent(messageIntent))
        if (messageIntent.action == ServiceActions.SOCKET_CLOSED) {
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
            ServiceActions.START_CONNECTION -> {
                val selectedDevice = messageIntent?.getStringExtra("BluetoothDevice")
                val b = BluetoothAdapter.getDefaultAdapter()
                val bluetoothDevice = b.getRemoteDevice(selectedDevice)
                btThread = BluetoothThread(bluetoothDevice, Messenger(messageToActivityHandler))
                btThread?.start()
                threadMessenger?.send(messageWithIntent(getStatusIntent()))
            }
            ServiceActions.GET_CONNECTION_STATUS -> {
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
            return Intent(ServiceActions.CONNECTION_ACTIVE)
        } else {
            return Intent(ServiceActions.CONNECTION_NOT_ACTIVE)
        }
    }

    private fun messageWithIntent(i: Intent) : Message {
        val message = Message()
        message.obj = i
        return message
    }

    override fun onCreate() {
        super.onCreate()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.app_icon)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()
        startForeground(ServiceActions.SERVICE_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder {
        return fromActivityMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }
}