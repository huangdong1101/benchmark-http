package com.mamba.benchmark.common.pressure;

import java.util.Objects;

public class Fixed extends Pressure {

    private final int quantity;

    private final int duration;

    private final int rampup;

    public Fixed(int quantity, int duration) {
        this(quantity, duration, 0);
    }

    public Fixed(int quantity, int duration, int rampup) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }
        if (duration < 10) {
            throw new IllegalArgumentException("Invalid duration: " + duration);
        }
        if (rampup < 0) {
            throw new IllegalArgumentException("Invalid rampup: " + rampup);
        }
        this.quantity = quantity;
        this.duration = duration;
        this.rampup = rampup;
    }

    @Override
    protected int getQuantity(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        if (offset > this.rampup + this.duration) {
            return -1;
        }
        if (offset >= this.rampup) {
            return this.quantity;
        }
        return (int) round(((long) this.quantity) * offset / (double) this.rampup);
    }

    private static long round(double d) {
        long l = (long) d;
        if (d == (double) l) {
            return l;
        } else {
            return (long) (d + Math.random());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fixed)) return false;
        Fixed fixed = (Fixed) o;
        return quantity == fixed.quantity &&
                duration == fixed.duration &&
                rampup == fixed.rampup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, duration, rampup);
    }

    @Override
    public String toString() {
        return "Fixed{" +
                "quantity=" + quantity +
                ", duration=" + duration +
                ", rampup=" + rampup +
                '}';
    }
}
