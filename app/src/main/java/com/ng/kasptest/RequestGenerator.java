package com.ng.kasptest;

import com.ng.kasptest.model.Producer;
import com.ng.kasptest.model.Request;
import com.ng.kasptest.model.Stopper;

import java.util.Random;

public class RequestGenerator implements Producer {

    private final Random mRandom ;
    private final Settings mSettings;

    private RequestGenerator() {
        mRandom = null;
        mSettings = null;
    }

    public RequestGenerator(Random random, Settings settings) {
        this.mRandom = random;
        this.mSettings = settings;
    }

    @Override
    public Request getRequest(Stopper stopSignal) {
        if (stopSignal.isStop()) {
            return null;
        }

        boolean needDelay = mRandom.nextBoolean();

        if (needDelay) {
            if (mSettings.requestGenerateMaxtime > 0) {
                try {
                    Thread.sleep((long) mRandom.nextInt(mSettings.requestExecuteDelayMaxTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return new RequestImpl();
        } else {
            return new RequestImpl();
        }
    }
}
