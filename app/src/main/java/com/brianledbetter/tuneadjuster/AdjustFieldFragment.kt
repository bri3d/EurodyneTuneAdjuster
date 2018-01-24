package com.brianledbetter.tuneadjuster

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_adjust_field.*

class AdjustFieldFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private var minValue: Int? = null
    private var maxValue: Int? = null
    private var title: String? = null

    private var mListener: OnParameterAdjustedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            minValue = arguments.getInt(ARG_MIN_VALUE)
            maxValue = arguments.getInt(ARG_MAX_VALUE)
            title = arguments.getString(ARG_TITLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_adjust_field, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.valueBar.setOnSeekBarChangeListener(this)

        this.valueBar.max = ((maxValue ?: 0) - (minValue ?: 0))
        this.minValueLabel.text = minValue.toString()
        this.maxValueLabel.text = maxValue.toString()
        this.titleLabel.text = title
        this.rawValue.text = (this.valueBar.progress + (minValue ?: 0)).toString()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnParameterAdjustedListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onProgressChanged(p0: SeekBar?, value: Int, p2: Boolean) {
        this.rawValue.text = (value + (minValue?: 0)).toString()
        this.rawValue.setTextColor(Color.RED)
        mListener?.onParameterAdjusted(value)
    }

    fun setValueFromData(value: Int) {
        this.rawValue.text = value.toString()
        this.valueBar.progress = value
        this.rawValue.setTextColor(Color.BLACK)
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }

    interface OnParameterAdjustedListener {
        fun onParameterAdjusted(value: Int)
    }

    companion object {
        private val ARG_MIN_VALUE = "minValue"
        private val ARG_MAX_VALUE = "maxValue"
        private val ARG_TITLE = "title"

        fun newInstance(minValue: Int, maxValue: Int, title: String): AdjustFieldFragment {
            val fragment = AdjustFieldFragment()
            val args = Bundle()
            args.putInt(ARG_MIN_VALUE, minValue)
            args.putInt(ARG_MAX_VALUE, maxValue)
            args.putString(ARG_TITLE, title)
            fragment.arguments = args
            return fragment
        }
    }
}