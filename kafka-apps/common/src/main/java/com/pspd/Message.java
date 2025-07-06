package com.pspd;


public record Message(int powMin, int powMax) {
    public Message {
        if (powMin < 0 || powMax < 0) {
            throw new IllegalArgumentException("Power must be positive");
        }
        if (powMin > powMax) {
            throw new IllegalArgumentException("powMin must be less than powMax");
        }
    }
}