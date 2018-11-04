package com.brianledbetter.tuneadjuster

class ServiceActions {
    companion object {
        const val SERVICE_ID = 1234
    }
    class Requests {
        companion object {
            // Requests to BluetoothThread/BluetoothService
            const val START_CONNECTION = "StartConnection"
            const val STOP_CONNECTION = "StopConnection"
            const val GET_CONNECTION_STATUS = "GetConnectionStatus"
            const val SAVE = "Save"
            const val FETCH_TUNE_DATA = "FetchTuneData"
            const val FETCH_ECU_DATA = "FetchEcuData"
            const val FETCH_FEATURE_FLAGS = "FetchFeatureFlags"
            const val FETCH_E85 = "FetchE85"

        }
    }
    class Responses {
        companion object {
            // Responses
            const val TUNE_DATA = "TuneData"
            const val ECU_DATA = "EcuData"
            const val E85_DATA = "E85Data"
            const val FEATURE_FLAGS = "FeatureFlags"
            const val SOCKET_CLOSED = "SocketClosed"
            const val CONNECTED = "Connected"
            const val CONNECTION_ACTIVE = "ConnectionActive"
            const val CONNECTION_NOT_ACTIVE = "ConnectionNotActive"
        }
    }
}