package com.brianledbetter.tuneadjuster

import android.annotation.TargetApi
import android.app.Notification
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import com.brianledbetter.tuneadjuster.elm327.BluetoothThread
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.*
import android.support.v4.app.NotificationCompat
import java.util.concurrent.locks.LockSupport


class BluetoothService: Service() {
    private var btThread : BluetoothThread? = null
    private var threadMessenger : Messenger? = null
    private val messageToActivityHandler = Handler { message ->
        val messageIntent = message.obj as Intent
        threadMessenger?.send(messageWithIntent(messageIntent))
        if (messageIntent.action == ServiceActions.Responses.SOCKET_CLOSED) {
            btThread = null
        }
        true
    }

    private val fromActivityMessenger = Messenger(Handler { message ->
        val messageIntent = message.obj as Intent

        if (message.replyTo != null) {
            threadMessenger = message.replyTo
        }

        when (messageIntent.action) {
            ServiceActions.Requests.START_CONNECTION -> {
                val selectedDevice = messageIntent.getStringExtra("BluetoothDevice")
                val b = BluetoothAdapter.getDefaultAdapter()
                val bluetoothDevice = b.getRemoteDevice(selectedDevice)
                btThread = BluetoothThread(bluetoothDevice, Messenger(messageToActivityHandler))
                btThread?.start()
                threadMessenger?.send(messageWithIntent(getStatusIntent()))
            }
            ServiceActions.Requests.GET_CONNECTION_STATUS -> {
                threadMessenger?.send(messageWithIntent(getStatusIntent()))
            }
            else -> { // Forward on to connection thread
                val forwardMessage = btThread?.handler?.obtainMessage()
                forwardMessage?.obj = messageIntent
                btThread?.handler?.sendMessage(forwardMessage)
            }
        }
        true
    })

    private fun getStatusIntent() : Intent {
        return if (btThread != null) {
            Intent(ServiceActions.Responses.CONNECTION_ACTIVE)
        } else {
            Intent(ServiceActions.Responses.CONNECTION_NOT_ACTIVE)
        }
    }

    private fun messageWithIntent(i: Intent) : Message {
        val message = Message()
        message.obj = i
        return message
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        // Android continues to be the worst development platform in history
        val channelId = "ed_tune_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH)
            val description = getString(R.string.channel_description)
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.app_icon)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()
        startForeground(ServiceActions.SERVICE_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        LockSupport.unpark(btThread)
    }

    override fun onBind(intent: Intent?): IBinder {
        return fromActivityMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }
}