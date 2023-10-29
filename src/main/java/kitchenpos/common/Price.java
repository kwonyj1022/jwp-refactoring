package kitchenpos.common;

import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Price {

    public static final Price ZERO = new Price(BigDecimal.ZERO);

    private BigDecimal value;

    protected Price() {
    }

    public Price(final BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }

        this.value = value;
    }

    public boolean biggerThan(final Price otherPrice) {
        return value.compareTo(otherPrice.value) > 0;
    }

    public Price add(final Price otherPrice) {
        final BigDecimal addValue = value.add(otherPrice.value);

        return new Price(addValue);
    }

    public Price multiply(final long quantity) {
        final BigDecimal multiplyValue = this.value.multiply(BigDecimal.valueOf(quantity));

        return new Price(multiplyValue);
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
}