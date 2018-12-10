package com.ng.kasptest;

import com.ng.kasptest.model.Consumer;
import com.ng.kasptest.model.Request;
import com.ng.kasptest.model.Stopper;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestHandler implements Consumer {

    private RequestHandler() {
        numCores = 0;
        mExecutor = null;
        mSettings = null;
        mRandom = null;
    }

    public RequestHandler(Random random, Settings settings) {
        int processorCount = Runtime.getRuntime().availableProcessors();
        if (processorCount <= 2)
            numCores = 8;
        else
            numCores = processorCount;

        reinitExecгtor();

        this.mRandom = random;
        this.mSettings = settings;
    }

    private final int numCores;
    private ThreadPoolExecutor mExecutor;
    private final Settings mSettings;

    private RequestCountListener mRequestCountListener;
    private CountDownListener mCountDownListener;

    private final AtomicInteger mRequestCounter = new AtomicInteger(0);
    private final AtomicBoolean mIsStopped = new AtomicBoolean(false);
    private final AtomicBoolean mStopStarted = new AtomicBoolean(false);
    private final AtomicBoolean mCountDownStarted = new AtomicBoolean(false);
    private final Random mRandom;
    private Thread mCountDownThread;

    private void reinitExecгtor() {
        mExecutor = new ThreadPoolExecutor(2, numCores, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void processRequest(Request request, Stopper stopSignal) {
        if (stopSignal != null && stopSignal.isStop()) {
            stopAllProcess();
            return;
        }

        if (mIsStopped.get()) {
            return;
        }

        if (!mCountDownStarted.get()) {
            startCountDownTimer();
        }

        if (request == null)
            throw new IllegalArgumentException("If process not stop request must not be null");

        boolean needDelay = mRandom.nextBoolean();
        if (needDelay) {
            if (mSettings.requestExecuteDelayMaxTime > 0) {
                try {
                    Thread.sleep(mRandom.nextInt(mSettings.requestExecuteDelayMaxTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (mIsStopped.get()) {
                return;
            }

            executeRequest(request);
        } else {
            executeRequest(request);
        }
    }

    private void executeRequest(Request request) {
        if (mExecutor.isShutdown() || mExecutor.isTerminating()) {
            if (!mIsStopped.get()) {
                reinitExecгtor();
            }
        }

        int value = mRequestCounter.incrementAndGet();
        if (mRequestCountListener != null) {
            mRequestCountListener.onRequestCountChange(value);
        }

        RequestWrapper requestWrapper = new RequestWrapper(new FinishCallback() {
            @Override
            public void doOnFinish() {
                int value = mRequestCounter.decrementAndGet();
                if (mRequestCountListener != null) {
                    mRequestCountListener.onRequestCountChange(value);
                }
            }
        }, mSettings, mRandom);

        try {
            mExecutor.execute(requestWrapper);
        } catch (Exception e) {
        }
    }

    private void stopAllProcess() {
        if (mStopStarted.get()) {
            return;
        }

        mStopStarted.set(true);
        mIsStopped.set(true);

        mExecutor.shutdownNow();
        if (mCountDownThread != null) {
            mCountDownThread.interrupt();
        }
        if (mCountDownListener != null) {
            mCountDownListener.onCountDownChange(mSettings.countDownTime, mIsStopped.get());
        }

        mRequestCounter.set(0);
        if (mRequestCountListener != null) {
            mRequestCountListener.onRequestCountChange(0);
        }


        mStopStarted.set(false);
        mIsStopped.set(false);
    }

    private void startCountDownTimer() {
        mCountDownStarted.set(true);

        mCountDownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int counter = mSettings.countDownTime;

                    while (true) {
                        if (mCountDownListener != null) {
                            mCountDownListener.onCountDownChange(counter--, mIsStopped.get());
                        }
                        Thread.sleep(1000);
                        if (counter == 0) {
                            processRequest(null, new Stopper(true));
                            mCountDownStarted.set(false);
                            if (mCountDownListener != null) {
                                mCountDownListener.onCountDownChange(mSettings.countDownTime, mIsStopped.get());
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    mCountDownStarted.set(false);
                }
            }
        });

        mCountDownThread.start();
    }

    public void subsctibeToRequestCount(RequestCountListener requestCountListener) {
        this.mRequestCountListener = requestCountListener;
    }

    public void subscribeToCountDown(CountDownListener countDownListener) {
        this.mCountDownListener = countDownListener;
    }

    public void unsubscribe() {
        mRequestCountListener = null;
        mCountDownListener = null;
    }

    public interface RequestCountListener {
        void onRequestCountChange(int value);
    }

    public interface CountDownListener {
        void onCountDownChange(int value, boolean isStopped);
    }

    private class RequestWrapper implements Runnable {

        private RequestWrapper() throws IllegalAccessException {
            mFinishCallback = null;
            mTime = 0;
            throw new IllegalAccessException("Illegal constructor!");
        }

        public RequestWrapper(FinishCallback finishCallback, Settings settings, Random random) {
            this.mFinishCallback = finishCallback;
            if (settings.requestMaxTime > 0) {
                mTime = random.nextInt(settings.requestMaxTime);
            } else {
                mTime = 1000;
            }
        }

        private final FinishCallback mFinishCallback;
        private final int mTime;

        @Override
        public void run() {
            try {
                Thread.sleep(mTime);
                mFinishCallback.doOnFinish();
            } catch (Exception e) {
            }
        }
    }

    private interface FinishCallback {
        void doOnFinish();
    }
}
