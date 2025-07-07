package com.pspd;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GsonDeserializer<T> implements Deserializer<T> {
    private final Gson gson = new Gson();
    private Class<T> type;

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Object typeName = configs.get("gson.deserializer.type");
        if (typeName == null) {
            throw new IllegalArgumentException("Missing 'gson.deserializer.type' property in Kafka config");
        }
        try {
            this.type = (Class<T>) Class.forName((String) typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class for gson.deserializer.type: " + typeName, e);
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        return gson.fromJson(new String(data, StandardCharsets.UTF_8), type);
    }

    @Override
    public void close() {}
}
