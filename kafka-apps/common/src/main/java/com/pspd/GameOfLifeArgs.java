package com.pspd;


public record GameOfLifeArgs(int powMin, int powMax) {
    public GameOfLifeArgs {
        if (powMin <= 1 || powMax <= 1) {
            throw new IllegalArgumentException("Power must be greater than one");
        }
        if (powMin > powMax) {
            throw new IllegalArgumentException("powMin must be less than powMax");
        }
    }
}