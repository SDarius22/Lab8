package org.example;


public interface DsmNodeEndpoint extends DsmCallback {

    void onRequestCompleted(DsmMessage request, boolean success);
}
