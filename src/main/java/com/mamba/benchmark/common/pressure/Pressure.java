package com.mamba.benchmark.common.pressure;

public abstract class Pressure {

    private volatile long beginTime = 0;

    private long getBeginTime() {
        if (this.beginTime == 0) {
            synchronized (this) {
                if (this.beginTime == 0) {
                    this.beginTime = System.currentTimeMillis();
                }
            }
        }
        return beginTime;
    }

    public int currentQuantity() {
        long beginTime = this.getBeginTime();
        long currentTime = System.currentTimeMillis();
        return this.getQuantity((int) ((currentTime - beginTime) / 1000));
    }

    protected abstract int getQuantity(int offset);

//    public static Pressure parse(String type, String config) {
//        switch (type) {
//            case "fixed":
//                return parseFixed(config);
//            case "gradient":
//                return parseGradient(config);
//            case "custom":
//                return parseCustom(config);
//            default:
//                throw new IllegalArgumentException("Invalid type: " + type);
//        }
//    }
//
//    private static Fixed parseFixed(String config) {
//        JSONObject json = JSONObject.parseObject(config);
//        Integer quantity = json.getInteger("quantity");
//        Integer duration = json.getInteger("duration");
//        if (quantity == null || duration == null) {
//            throw new IllegalArgumentException("Invalid config: " + config);
//        }
//        Integer rampup = json.getInteger("rampup");
//        if (rampup == null) {
//            return new Fixed(quantity, duration);
//        } else {
//            return new Fixed(quantity, duration, rampup);
//        }
//    }
//
//    private static Gradient parseGradient(String config) {
//        JSONObject json = JSONObject.parseObject(config);
//        Integer initialQuantity = json.getInteger("initialQuantity");
//        Integer finalQuantity = json.getInteger("finalQuantity");
//        Integer incrementPerStep = json.getInteger("incrementPerStep");
//        Integer durationPerStep = json.getInteger("durationPerStep");
//        if (initialQuantity == null || finalQuantity == null || incrementPerStep == null || durationPerStep == null) {
//            throw new IllegalArgumentException("Invalid config: " + config);
//        }
//        return new Gradient(initialQuantity, finalQuantity, incrementPerStep, durationPerStep);
//    }
//
//    private static Custom parseCustom(String config) {
//        JSONObject json = JSONObject.parseObject(config);
//        int[] quantities = json.getObject("quantities", int[].class);
//        Integer durationPerStep = json.getInteger("durationPerStep");
//        if (quantities == null || durationPerStep == null) {
//            throw new IllegalArgumentException("Invalid config: " + config);
//        }
//        return new Custom(quantities, durationPerStep);
//    }
}
