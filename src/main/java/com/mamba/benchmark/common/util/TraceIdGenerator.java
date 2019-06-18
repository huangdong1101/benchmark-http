package com.mamba.benchmark.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceIdGenerator {

    private static final char[][] HEX_DICT = genHexDict();

    /**
     * 本地地址
     */
    private static final char[] IP = getLocalAddress();

    /**
     * 初始化初始向量（初始时间 + 线程ID）
     */
    private static final ThreadLocal<char[]> START_VALUE = ThreadLocal.withInitial(() -> genStartValue());

    /**
     * 自增序列（最小值）
     */
    private static final int SEQUENCER_MIN = 0x000000;

    /**
     * 自增序列（最大值）
     */
    private static final int SEQUENCER_MAX = 0xffffff;

    /**
     * 自增序列（范围长度）
     */
    private static final int SEQUENCER_LEN = SEQUENCER_MAX - SEQUENCER_MIN + 1;

    /**
     * 循环自增序列
     */
    private static final AtomicInteger SEQUENCER = new AtomicInteger(-1);

    public static String genTraceId(String sign) {
        if (sign == null || sign.isEmpty()) {
            throw new IllegalArgumentException("Empty sign!");
        }
        if ((sign.length() != 2)) {
            throw new IllegalArgumentException("Invalid sign: " + sign);
        }
        return genTraceId(sign.charAt(0), sign.charAt(1));
    }

//    public static String genTraceId(char[] sign) {
//        if (sign == null || sign.length == 0) {
//            throw new IllegalArgumentException("Empty sign!");
//        }
//        if ((sign.length != 2) || !isHex(sign[0]) || !isHex(sign[1])) {
//            throw new IllegalArgumentException("Invalid sign: " + Arrays.toString(sign));
//        }
//        return genTraceId(sign[0], sign[1]);
//    }

    private static String genTraceId(char ch0, char ch1) {
        if (!isHex(ch0) || !isHex(ch1)) {
            throw new IllegalArgumentException("Invalid sign: " + ch0 + ch1);
        }
        char[] chars = new char[32];
        //IP（32bits，长度8）
        copyHex(chars, IP, 0, 8);

        //时间戳（32bits，长度8）
        long timestamp = System.currentTimeMillis() / 1000;
        copyHex(chars, timestamp, 8, 4);

        //初始向量（32bits，长度8）
        copyHex(chars, START_VALUE.get(), 16, 8);

        //自增序列（24bits，长度6）
        int sequence = getSequence();
        copyHex(chars, sequence, 24, 3);

        //来源标识（8bits，长度2）
        chars[30] = ch0;
        chars[31] = ch1;
        return new String(chars);
    }

    private static boolean isHex(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f');
    }

    /**
     * 初始化16进制字典
     *
     * @return
     */
    private static char[][] genHexDict() {
        char[] chars = new char[0x10];
        for (int i = 0; i < 0x10; i++) {
            chars[i] = Integer.toHexString(i).charAt(0);
        }
        char[][] dict = new char[0x100][2];
        for (int i = 0; i < 0x100; i++) {
            dict[i][0] = chars[i / 0x10];
            dict[i][1] = chars[i % 0x10];
        }
        return dict;
    }

    /**
     * 获取本地地址
     *
     * @return
     */
    private static char[] getLocalAddress() {
        try {
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration enumeration = ((NetworkInterface) interfaces.nextElement()).getInetAddresses();
                while (enumeration.hasMoreElements()) {
                    InetAddress address = (InetAddress) enumeration.nextElement();
                    if (address == null || !(address instanceof Inet4Address)) {
                        continue;
                    }
                    byte[] bytes = address.getAddress();
                    if (bytes[1] == 0 && bytes[2] == 0) {
                        if (bytes[0] == 0 && bytes[3] == 0) { //0.0.0.0
                            continue;
                        }
                        if (bytes[0] == 127 && bytes[3] == 1) { //127.0.0.1
                            continue;
                        }
                    }
                    return toHex(bytes);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Get LocalAddress Error: " + e.getMessage(), e);
        }
        throw new RuntimeException("Empty LocalAddress");
    }

    /**
     * 初始化初始向量（初始时间 + 线程ID）
     *
     * @return
     */
    private static char[] genStartValue() {
        long currentTime = System.currentTimeMillis() / 1000;
        long currentThread = Thread.currentThread().getId();
        char[] chars = new char[8];
        copyHex(chars, currentTime / 20, 0, 2);
        copyHex(chars, currentThread, 4, 2);
        return chars;
    }

    /**
     * 获取自增序列
     *
     * @return
     */
    private static int getSequence() {
        int sequence = SEQUENCER.incrementAndGet();
        if (sequence < SEQUENCER_LEN) {
            return SEQUENCER_MIN + sequence;
        }
        for (; ; ) {
            int current = SEQUENCER.get();
            if (current < SEQUENCER_LEN) {
                break;
            }
            if (SEQUENCER.compareAndSet(current, current % SEQUENCER_LEN)) {
                break;
            }
        }
        return SEQUENCER_MIN + (sequence % SEQUENCER_LEN);
    }

    /**
     * 转换成16进制字符串
     *
     * @param bytes
     * @return
     */
    private static char[] toHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            System.arraycopy(toHex(bytes[i]), 0, chars, i * 2, 2);
        }
        return chars;
    }

    /**
     * 转换成16进制字符串
     *
     * @param b
     * @return
     */
    private static char[] toHex(byte b) {
        return HEX_DICT[b & 0xff];
    }

    /**
     * 复制16进制字符串
     *
     * @param chars  目标字符串
     * @param src    原始字符串
     * @param pos    初始位置
     * @param length 复制长度
     */
    private static void copyHex(char[] chars, char[] src, int pos, int length) {
        System.arraycopy(src, 0, chars, pos, length);
    }

    /**
     * 复制16进制字符串
     *
     * @param chars 目标字符串
     * @param num   原始数字
     * @param pos   初始位置
     * @param bytes 复制字节数
     */
    private static void copyHex(char[] chars, long num, int pos, int bytes) {
        for (int p = pos + (bytes - 1) * 2; p >= pos; p -= 2, num >>= 8) {
            System.arraycopy(toHex((byte) (num & 0xff)), 0, chars, p, 2);
        }
    }

    public static void main(String[] args) {
        System.out.println(genTraceId("ab"));
    }
}
