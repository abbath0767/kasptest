package com.ng.kasptest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.ng.kasptest.model.Stopper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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
        timer.text = getString(R.string.countdown_time, Settings.countDownTime)
        status.text = getString(R.string.status_stop)
    }

    private fun setOnClickListeners() {
        main_button_new_request.setOnClickListener { generateNewRequest(false) }
        stop.setOnClickListener { stop() }
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
            current_executed_request_count.text = getString(R.string.request_count, count)
        }
        RequestHandler.subscribeToCountDown { countDownTime, isStopped ->
            timer.text = getString(R.string.countdown_time, countDownTime)
            status.text =
                    if (!isStopped) getString(R.string.status_active)
                    else getString(R.string.status_stop)
        }
    }

    private fun generateNewRequest(stop: Boolean) {
        Thread(Runnable {
            val stopper = Stopper(stop)
            val request = RequestGenerator.getRequest(stopper)

            RequestHandler.processRequest(request, stopper)
        }).start()
    }

    private fun stop() {
        RequestHandler.processRequest(null, Stopper(true))
    }
}
