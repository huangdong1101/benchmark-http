package com.mamba.benchmark.common.pressure;

import java.util.Objects;

public class Gradient extends Pressure {

    private final int initialQuantity;

    private final int finalQuantity;

    private final int incrementPerStep;

    private final int durationPerStep;

    public Gradient(int initialQuantity, int finalQuantity, int incrementPerStep, int durationPerStep) {
        if (initialQuantity < 1) {
            throw new IllegalArgumentException("Invalid initialQuantity: " + initialQuantity);
        }
        if (finalQuantity < 1) {
            throw new IllegalArgumentException("Invalid finalQuantity: " + finalQuantity);
        }
        //初始值不相等最终值
        if (initialQuantity == finalQuantity) {
            throw new IllegalArgumentException("Invalid initialQuantity: " + initialQuantity + ", finalQuantity: " + finalQuantity);
        }
        //增量值不等于0；初始值小于最终值，增量大于0；初始值大于最终值，增量小于0
        if (incrementPerStep == 0 || (finalQuantity - initialQuantity) * incrementPerStep <= 0) {
            throw new IllegalArgumentException("Invalid incrementPerStep: " + incrementPerStep);
        }
        //每阶段不小于10，总时长不小于60
        if (durationPerStep < 10 || durationPerStep * ((int) Math.ceil((finalQuantity - initialQuantity) / (double) incrementPerStep) + 1) < 60) {
            throw new IllegalArgumentException("Invalid durationPerStep: " + durationPerStep);
        }
        this.initialQuantity = initialQuantity;
        this.finalQuantity = finalQuantity;
        this.incrementPerStep = incrementPerStep;
        this.durationPerStep = durationPerStep;
    }

    @Override
    protected int getQuantity(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        int steps = (int) Math.ceil((this.finalQuantity - this.initialQuantity) / (double) this.incrementPerStep) + 1;
        int idx = offset / this.durationPerStep;
        if (idx == 0) {
            return this.initialQuantity;
        }
        if (idx == steps - 1) {
            return this.finalQuantity;
        }
        if (idx < steps - 1) {
            return this.initialQuantity + this.incrementPerStep * idx;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gradient)) return false;
        Gradient gradient = (Gradient) o;
        return initialQuantity == gradient.initialQuantity &&
                finalQuantity == gradient.finalQuantity &&
                incrementPerStep == gradient.incrementPerStep &&
                durationPerStep == gradient.durationPerStep;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialQuantity, finalQuantity, incrementPerStep, durationPerStep);
    }

    @Override
    public String toString() {
        return "Gradient{" +
                "initialQuantity=" + initialQuantity +
                ", finalQuantity=" + finalQuantity +
                ", incrementPerStep=" + incrementPerStep +
                ", durationPerStep=" + durationPerStep +
                '}';
    }
}
