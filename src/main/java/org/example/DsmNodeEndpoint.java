package org.example;


public interface DsmNodeEndpoint extends DsmCallback {
    /**
     * Called when a request (WRITE or CAS) has been processed.
     * For WRITE, success is always true.
     * For CAS, success indicates whether the exchange happened.
     */
    void onRequestCompleted(DsmMessage request, boolean success);
}
