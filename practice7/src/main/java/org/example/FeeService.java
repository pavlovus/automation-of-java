package org.example;

import java.math.BigDecimal;

public interface FeeService {
    BigDecimal calculateFee(BigDecimal amount);
}