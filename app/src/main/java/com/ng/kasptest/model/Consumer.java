package com.ng.kasptest.model;

public interface Consumer {
    void processRequest(Request request, Stopper stopSignal);
}
