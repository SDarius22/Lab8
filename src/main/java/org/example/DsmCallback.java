package org.example;

public interface DsmCallback {
    /**
     * Called when a subscribed variable changes.
     *
     * @param sequence global sequence number of this change (total order)
     * @param varName  name of the variable that changed
     * @param newValue new integer value
     */
    void onVariableChanged(long sequence, String varName, int newValue);
}