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

    public Money plus(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money minus(Money other) {
        if (this.amount < other.amount) throw new IllegalArgumentException("결과 금액이 음수가 될 수 없습니다.");
        return new Money(this.amount - other.amount);
    }

    public Money multiply(int factor) {
        if (factor < 0) throw new IllegalArgumentException("배수는 0 이상이어야 합니다.");
        return new Money(this.amount * factor);
    }
}
