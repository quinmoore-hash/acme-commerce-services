package com.acme.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void multipliesAndAdds() {
        Money unit = Money.of("19.99", "usd");

        Money total = unit.times(3).plus(Money.of("0.03", "USD"));

        assertThat(total).isEqualTo(Money.of("60.00", "USD"));
        assertThat(total.getCurrency()).isEqualTo("USD");
    }

    @Test
    void rejectsCurrencyMismatch() {
        assertThatThrownBy(() -> Money.of("1", "USD").plus(Money.of("1", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
