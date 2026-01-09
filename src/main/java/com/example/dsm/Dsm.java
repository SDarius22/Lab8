package com.example.dsm;

public interface Dsm {

    void write(int varId, int value);

    int read(int varId);

    /**
     * Atomically compares the current value of a variable with `expectedValue`
     * and, if equal, sets it to `newValue`.
     *
     * @param varId         variable index
     * @param expectedValue value to compare against
     * @param newValue      value to set if comparison succeeds
     * @return true if the exchange was performed, false otherwise
     */
    boolean compareAndExchange(int varId, int expectedValue, int newValue);

    /**
     * Registers a listener that will be called on each variable change,
     * in the same global order for all listeners.
     */
    void addListener(DsmListener listener);

    /**
     * Unregisters a previously registered listener.
     */
    void removeListener(DsmListener listener);

    interface DsmListener {
        /**
         * Called when a DSM variable changes.
         *
         * @param varId    the variable index
         * @param newValue the new value written
         * @param version  a monotonically increasing global version number
         */
        void onVariableChanged(int varId, int newValue, long version);
    }
}
