package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeeCalculatorTest {
    private final FeeCalculator calculator = new FeeCalculator();

    @Test
    void weakTestThatFailsToKillMutant() {
        assertEquals(0, calculator.calculateFee(2000));
        assertEquals(5, calculator.calculateFee(500));
    }

    @Test
    void strongTestThatKillsMutant() {
        assertEquals(0, calculator.calculateFee(2000));
        assertEquals(5, calculator.calculateFee(500));
        assertEquals(0, calculator.calculateFee(1000));
    }
}