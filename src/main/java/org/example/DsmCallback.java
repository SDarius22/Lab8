package org.example;

public interface DsmCallback {

    void onVariableChanged(long sequence, String varName, int newValue);
}