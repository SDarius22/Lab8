package org.example;

public final class DsmMessageCodec {

    private static final String SEP_REGEX = "\\|";
    private static final char SEP = '|';

    public static String encode(DsmMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg.getType().name()).append(SEP)
                .append(msg.getNodeId()).append(SEP)
                .append(msg.getVarName()).append(SEP)
                .append(msg.getValue()).append(SEP);
        Integer expected = msg.getExpected();
        sb.append(expected == null ? "-" : expected.toString());
        return sb.toString();
    }

    public static DsmMessage decode(String line) {
        if (line == null) {
            throw new IllegalArgumentException("line is null");
        }
        String[] parts = line.split(SEP_REGEX, -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid message format: " + line);
        }

        DsmMessage.Type type = DsmMessage.Type.valueOf(parts[0]);
        String nodeId = parts[1];
        String varName = parts[2];
        int value = Integer.parseInt(parts[3]);
        String expectedStr = parts[4];

        return switch (type) {
            case WRITE -> DsmMessage.write(nodeId, varName, value);
            case COMPARE_AND_EXCHANGE -> {
                if ("-".equals(expectedStr)) {
                    throw new IllegalArgumentException("Missing expected for CAS: " + line);
                }
                int expected = Integer.parseInt(expectedStr);
                yield DsmMessage.compareAndExchange(nodeId, varName, expected, value);
            }
        };
    }

    private DsmMessageCodec() {
    }
}
