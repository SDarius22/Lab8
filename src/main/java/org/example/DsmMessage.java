package org.example;


public class DsmMessage {

    public enum Type {
        WRITE,
        COMPARE_AND_EXCHANGE
    }

    private final Type type;
    private final String nodeId;
    private final String varName;
    private final int value;          // for WRITE, or newValue for CAS
    private final Integer expected;   // only used for CAS, null for WRITE

    private DsmMessage(Type type, String nodeId, String varName, int value, Integer expected) {
        this.type = type;
        this.nodeId = nodeId;
        this.varName = varName;
        this.value = value;
        this.expected = expected;
    }

    public static DsmMessage write(String nodeId, String varName, int newValue) {
        return new DsmMessage(Type.WRITE, nodeId, varName, newValue, null);
    }

    public static DsmMessage compareAndExchange(String nodeId, String varName,
                                                int expected, int newValue) {
        return new DsmMessage(Type.COMPARE_AND_EXCHANGE, nodeId, varName, newValue, expected);
    }

    public Type getType() {
        return type;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getVarName() {
        return varName;
    }

    public int getValue() {
        return value;
    }

    public Integer getExpected() {
        return expected;
    }
}
