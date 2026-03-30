package allmart.productservice.domain.product;

public record Money(long amount) {

    public Money {
        if (amount < 0) throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + amount);
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(0L);
    }
}
