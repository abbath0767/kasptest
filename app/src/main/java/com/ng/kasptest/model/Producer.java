package com.ng.kasptest.model;

public interface Producer {
    Request getRequest(Stopper stopSignal);
}
