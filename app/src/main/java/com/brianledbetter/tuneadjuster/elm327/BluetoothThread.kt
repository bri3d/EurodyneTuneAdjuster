package com.brianledbetter.tuneadjuster.elm327

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.brianledbetter.tuneadjuster.ServiceActions
import java.io.IOException
import java.util.*
import java.util.concurrent.locks.LockSupport


class BluetoothThread(mmDevice: BluetoothDevice, private val mainMessenger : Messenger) : Thread("BluetoothMainThread") {
    companion object {
        val MY_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    }

    private val mmSocket: BluetoothSocket?
    private var elmIO : ElmIO? = null
    val handler : Handler = Handler({ message ->
        val intent = message.obj as? Intent
        when(intent?.action) {
            ServiceActions.Requests.SAVE_BOOST_AND_OCTANE -> {
                if (elmIO != null) {
                    val boost: EurodyneIO.BoostInfo = intent.getParcelableExtra("BoostInfo")
                    val octane: EurodyneIO.OctaneInfo = intent.getParcelableExtra("OctaneInfo")
                    val edIo = EurodyneIO(UDSIO(elmIO!!))
                    edIo.setBoostInfo(boost.current)
                    edIo.setOctaneInfo(octane.current)
                    fetchTuneInfo()
                }
            }
            ServiceActions.Requests.FETCH_TUNE_DATA -> fetchTuneInfo()
            ServiceActions.Requests.FETCH_ECU_DATA -> fetchEcuInfo()
            ServiceActions.Requests.STOP_CONNECTION -> cancel()
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
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.cancelDiscovery()
        try {
            mmSocket!!.connect()
        } catch (connectException: IOException) {
            try {
                cancel()
            } catch (closeException: IOException) {
                Log.e(TAG, "Could not close the client socket", closeException)
            }
            return
        }

        val inputStream = mmSocket.inputStream
        val outputStream = mmSocket.outputStream

        this.elmIO = ElmIO(inputStream, outputStream)
        elmIO!!.start()

        val connectedMessage = Message()
        connectedMessage.obj = Intent(ServiceActions.Responses.CONNECTED)
        mainMessenger.send(connectedMessage)
        LockSupport.park()
        cancel()
    }

    private fun sendClosed() {
        val closeMessage = handler.obtainMessage()
        val intent = Intent(ServiceActions.Responses.SOCKET_CLOSED)
        closeMessage.obj = intent
        mainMessenger.send(closeMessage)
    }

    private fun fetchTuneInfo() {
        val edIo = EurodyneIO(UDSIO(elmIO!!))
        val octaneInfo = edIo.getOctaneInfo()
        val boostInfo = edIo.getBoostInfo()
        val message = Message()
        val tuneIntent = Intent(ServiceActions.Responses.TUNE_DATA)
        tuneIntent.putExtra("boostInfo", boostInfo)
        tuneIntent.putExtra("octaneInfo", octaneInfo)
        message.obj = tuneIntent
        mainMessenger.send(message)
    }

    private fun fetchEcuInfo() {
        val ecuIO = EcuIO(UDSIO(elmIO!!))
        val ecuInfo = ecuIO.getEcuInfo()
        val message = Message()
        val ecuIntent = Intent(ServiceActions.Responses.ECU_DATA)
        ecuIntent.putExtra("ecuInfo", ecuInfo)
        message.obj = ecuIntent
        mainMessenger.send(message)
    }

    private fun cancel() {
        try {
            elmIO?.stop()
            mmSocket!!.close()
            sendClosed()
            interrupt()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }

    }
}