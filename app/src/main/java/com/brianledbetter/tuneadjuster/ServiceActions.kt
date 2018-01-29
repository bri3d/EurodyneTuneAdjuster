package com.brianledbetter.tuneadjuster

class ServiceActions {
    companion object {
        const val SERVICE_ID = 1234

        // Responses
        const val TUNE_DATA = "TuneData"
        const val ECU_DATA = "EcuData"
        const val SOCKET_CLOSED = "SocketClosed"
        const val CONNECTED = "Connected"
        const val CONNECTION_ACTIVE = "ConnectionActive"
        const val CONNECTION_NOT_ACTIVE = "ConnectionNotActive"

        // Requests to BluetoothThread/BluetoothService
        const val START_CONNECTION = "StartConnection"
        const val STOP_CONNECTION = "StopConnection"
        const val GET_CONNECTION_STATUS = "GetConnectionStatus"
        const val SAVE_BOOST_AND_OCTANE = "SaveBoostAndOctane"
        const val FETCH_TUNE_DATA = "FetchTuneData"
        const val FETCH_ECU_DATA = "FetchEcuData"
    }
}