package kitchenpos.domain;

import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Price {

    private BigDecimal value;

    protected Price() {
    }

    public Price(final BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }

        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Price price = (Price) o;
        return Objects.equals(value, price.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public boolean biggerThan(final BigDecimal otherValue) {
        return value.compareTo(otherValue) > 0;
    }
}
