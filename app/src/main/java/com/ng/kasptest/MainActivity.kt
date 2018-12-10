package com.ng.kasptest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.ng.kasptest.model.Stopper
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isStop = false
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setData()
        setOnClickListeners()
        setOnEditTextListeners()

        subscribeToData()
    }

    private fun setData() {
        request_max_time_et.setText(Settings.requestMaxTime.toString())
        request_generate_time_et.setText(Settings.requestGenerateMaxtime.toString())
        request_execute_delay_max_time_et.setText(Settings.requestExecuteDelayMaxTime.toString())
        current_executed_request_count.text = getString(R.string.request_count, 0)
    }

    private fun setOnClickListeners() {
        main_button_new_request.setOnClickListener { generateNewRequest(isStop) }
    }

    private fun setOnEditTextListeners() {
        request_max_time_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    val newValue = s.toString().toInt()
                    Settings.requestMaxTime = newValue
                } catch (e: Exception) {
                    Log.e("TAG", "String to Int cast exception. value: ${s.toString()}")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        request_generate_time_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    val time = s.toString().toInt()
                    Settings.requestGenerateMaxtime = time
                } catch (e: Exception) {
                    Log.e("TAG", "String to Int cast exception. value: ${s.toString()}")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        request_execute_delay_max_time_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    val time = s.toString().toInt()
                    Settings.requestExecuteDelayMaxTime = time
                } catch (e: Exception) {
                    Log.e("TAG", "String to Int cast exception. value: ${s.toString()}")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun subscribeToData() {
        RequestHandler.subscribeToRequestCount { count ->
            Log.d("TAG", "New executed request count: $count")
            current_executed_request_count.text = getString(R.string.request_count, count)
        }
    }

    private fun generateNewRequest(stop: Boolean) {
        Thread(Runnable {
            val stopper = Stopper(stop)
            val request = RequestGenerator.getRequest(stopper)

            RequestHandler.processRequest(request, stopper)
        }).start()
    }
}