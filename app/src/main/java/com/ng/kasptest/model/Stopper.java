package com.ng.kasptest.model;

public class Stopper {

    private Stopper() {
    }

    public Stopper(Boolean isStop) {
        this.isStop = isStop;
    }

    private boolean isStop;

    public boolean isStop() {
        return isStop;
    }
}
