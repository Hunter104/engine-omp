package com.pspd;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class GsonSerializer<T> implements Serializer<T> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) return null;
        return gson.toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(java.util.Map<String, ?> configs, boolean isKey) {
    }
}
