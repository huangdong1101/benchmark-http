package com.mamba.benchmark.http;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void test_get() throws Exception {
        Main.main("-req", MainTest.class.getClassLoader().getResource("request_get.json").getFile(),
                "-t",
                "-quantity", "10",
                "-duration", "60"
        );
        System.out.println(1);
    }

    @Test
    void test_post() throws Exception {
        Main.main("-req", MainTest.class.getClassLoader().getResource("request_post.json").getFile(),
                "-t",
                "-quantity", "10",
                "-duration", "60"
        );
        System.out.println(1);
    }
}
