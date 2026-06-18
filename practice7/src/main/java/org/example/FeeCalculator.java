package org.example;

public class FeeCalculator {
    public int calculateFee(int amount) {
        if (amount >= 1000) return 0;
        else return 5;
    }
}