package com.pspd;


public record Message(int powMin, int powMax) {
    public Message {
        if (powMin <= 1 || powMax <= 1) {
            throw new IllegalArgumentException("Power must be greater than one");
        }
        if (powMin > powMax) {
            throw new IllegalArgumentException("powMin must be less than powMax");
        }
    }
}