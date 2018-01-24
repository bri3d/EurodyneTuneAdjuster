package com.brianledbetter.tuneadjuster.elm327

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java.io.IOException
import java.util.*


class ConnectThread(private val mmDevice: BluetoothDevice, private val context: Context) : Thread() {
    private val MY_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private val mmSocket: BluetoothSocket?

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
        elmIO.start("01")
        val edIo = EurodyneIO()
        val octaneInfo = edIo.getOctaneInfo(elmIO)
        val boostInfo = edIo.getBoostInfo(elmIO)
        val broadcastIntent = Intent()
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT)
        broadcastIntent.action = "TuneData"
        broadcastIntent.putExtra("boostInfo", boostInfo)
        broadcastIntent.putExtra("octaneInfo", octaneInfo)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }

    fun cancel() {
        try {
            mmSocket!!.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }

    }
}