package ti.gateway.kubernetes.core;

public class KeyValueConfig {
    private KeyValue[] keyValues;

    public KeyValueConfig() {
    }

    public KeyValue[] getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(KeyValue[] keyValues) {
        this.keyValues = keyValues;
    }
}
