package com.brianledbetter.tuneadjuster.elm327

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java.io.IOException
import java.util.*


class BluetoothThread(private val mmDevice: BluetoothDevice, private val mainHandler : Handler) : Thread() {
    private val MY_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private val mmSocket: BluetoothSocket?
    var elmIO : ElmIO? = null
    val handler : Handler = Handler({ message ->
        val intent = message.obj as? Intent
        if (intent?.action == "SaveBoostAndOctane") {
            val boost : EurodyneIO.BoostInfo = intent.getParcelableExtra("BoostInfo")
            val octane : EurodyneIO.OctaneInfo = intent.getParcelableExtra("OctaneInfo")
            val edIo = EurodyneIO()
            val elmIO = elmIO!!
            edIo.setBoostInfo(elmIO, boost.current)
            edIo.setOctaneInfo(elmIO, octane.current)
            fetchInfo(elmIO)
        }
        true
    })

    init {
        var tmp: BluetoothSocket? = null

        try {
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID)
        } catch (e: IOException) {
            Log.e(TAG, "Socket's create() method failed", e)
        }

        mmSocket = tmp
    }

    override fun run() {
        try {
            mmSocket!!.connect()
        } catch (connectException: IOException) {
            try {
                mmSocket!!.close()
            } catch (closeException: IOException) {
                Log.e(TAG, "Could not close the client socket", closeException)
            }

            return
        }

        val inputStream = mmSocket.inputStream
        val outputStream = mmSocket.outputStream

        val elmIO = ElmIO(inputStream, outputStream)
        elmIO.start()
        fetchInfo(elmIO)
        this.elmIO = elmIO
    }

    fun fetchInfo(elmIO : ElmIO) {
        val edIo = EurodyneIO()
        val octaneInfo = edIo.getOctaneInfo(elmIO)
        val boostInfo = edIo.getBoostInfo(elmIO)
        val message = mainHandler.obtainMessage()
        val broadcastIntent = Intent()
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT)
        broadcastIntent.action = "TuneData"
        broadcastIntent.putExtra("boostInfo", boostInfo)
        broadcastIntent.putExtra("octaneInfo", octaneInfo)
        message.obj = broadcastIntent
        mainHandler.sendMessage(message)
    }

    fun cancel() {
        try {
            mmSocket!!.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }

    }
}