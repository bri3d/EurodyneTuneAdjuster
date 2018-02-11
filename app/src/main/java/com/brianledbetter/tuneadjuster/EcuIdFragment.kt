package com.brianledbetter.tuneadjuster

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_ecu_id.*

/**
 * Created by brian.ledbetter on 2/1/18.
 */
class EcuIdFragment : Fragment() {
    private var swNumber: String? = null
    private var swVersion: String? = null
    private var vin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            swNumber = arguments.getString(ARG_SOFTWARE_NUMBER)
            swVersion = arguments.getString(ARG_SOFTWARE_VERSION)
            vin = arguments.getString(ARG_VIN_NUMBER)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.softwareName.text = swNumber
        this.softwareVersion.text = swVersion
        this.vinNumber.text = vin
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_ecu_id, container, false)
    }

    companion object {
        const val ARG_SOFTWARE_NUMBER = "softwareNumber"
        const val ARG_SOFTWARE_VERSION = "softwareVersion"
        const val ARG_VIN_NUMBER = "vinNumber"

        fun newInstance(softwareNumber: String, softwareVersion: String, vinNumber: String): EcuIdFragment {
            val fragment = EcuIdFragment()
            val args = Bundle()
            args.putString(ARG_SOFTWARE_NUMBER, softwareNumber)
            args.putString(ARG_SOFTWARE_VERSION, softwareVersion)
            args.putString(ARG_VIN_NUMBER, vinNumber)
            fragment.arguments = args
            return fragment
        }
    }
}