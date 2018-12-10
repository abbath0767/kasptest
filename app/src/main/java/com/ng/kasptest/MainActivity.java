package com.ng.kasptest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ng.kasptest.model.Request;
import com.ng.kasptest.model.Stopper;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText mMaxTimeEt;
    private EditText mRequestGenerateTimeEt;
    private EditText mExecuteDelayTimeEt;
    private TextView mCurrentRequestCountTv;
    private TextView mTimerTv;
    private TextView mStatusTv;
    private Button mGenerateNewRequestButton;
    private Button mStopButton;

    private Random mRandom = new Random();
    private Settings mSettings = new Settings();
    private RequestGenerator mRequestGenerator = new RequestGenerator(mRandom, mSettings);
    private RequestHandler mRequestHandler = new RequestHandler(mRandom, mSettings);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();
        setData();
        setOnClickListeners();
        setOnEditTextListeners();
        subscribeToData();
    }

    private void setViews() {
        mMaxTimeEt = findViewById(R.id.request_max_time_et);
        mRequestGenerateTimeEt = findViewById(R.id.request_generate_time_et);
        mExecuteDelayTimeEt = findViewById(R.id.request_execute_delay_max_time_et);
        mCurrentRequestCountTv = findViewById(R.id.current_executed_request_count);
        mTimerTv = findViewById(R.id.timer);
        mStatusTv = findViewById(R.id.status);
        mGenerateNewRequestButton = findViewById(R.id.main_button_new_request);
        mStopButton = findViewById(R.id.stop);
    }

    private void setData() {
        mMaxTimeEt.setText(String.valueOf(mSettings.requestMaxTime));
        mRequestGenerateTimeEt.setText(String.valueOf(mSettings.requestExecuteDelayMaxTime));
        mExecuteDelayTimeEt.setText(String.valueOf(mSettings.requestExecuteDelayMaxTime));
        mCurrentRequestCountTv.setText(getString(R.string.request_count, 0));
        mTimerTv.setText(getString(R.string.countdown_time, mSettings.countDownTime));
        mStatusTv.setText(getString(R.string.status_stop));
    }

    private void setOnClickListeners() {
        mGenerateNewRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewRequest(false);
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
    }

    private void setOnEditTextListeners() {
        mMaxTimeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    mSettings.requestMaxTime = Integer.valueOf(s.toString());
                } catch (Exception e) {
                    Log.e("TAG", "String to Int cast exception. value: " + s.toString());
                }
            }
        });

        mRequestGenerateTimeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    mSettings.requestGenerateMaxtime = Integer.valueOf(s.toString());
                } catch (Exception e) {
                    Log.e("TAG", "String to Int cast exception. value: " + s.toString());
                }
            }
        });

        mExecuteDelayTimeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    mSettings.requestExecuteDelayMaxTime = Integer.valueOf(s.toString());
                } catch (Exception e) {
                    Log.e("TAG", "String to Int cast exception. value: " + s.toString());
                }
            }
        });
    }

    private void generateNewRequest(final boolean stop) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Stopper stopper = new Stopper(stop);
                Request request = mRequestGenerator.getRequest(stopper);

                mRequestHandler.processRequest(request, stopper);
            }
        }).start();
    }

    private void stop() {
        mRequestHandler.processRequest(null, new Stopper(true));
    }

    private void subscribeToData() {
        mRequestHandler.subsctibeToRequestCount(new RequestHandler.RequestCountListener() {
            @Override
            public void onRequestCountChange(int value) {
                mCurrentRequestCountTv.setText(getString(R.string.request_count, value));
            }
        });

        mRequestHandler.subscribeToCountDown(new RequestHandler.CountDownListener() {
            @Override
            public void onCountDownChange(int value, boolean isStopped) {
                mTimerTv.setText(getString(R.string.countdown_time, value));
                String statusText;
                if (!isStopped) {
                    statusText = getString(R.string.status_active);
                } else {
                    statusText = getString(R.string.status_stop);
                }
                mStatusTv.setText(statusText);
            }
        });
    }

    private void unsubscribeFromData() {
        mRequestHandler.unsubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribeToData();
    }

    @Override
    protected void onPause() {
        unsubscribeFromData();
        super.onPause();
    }
}
