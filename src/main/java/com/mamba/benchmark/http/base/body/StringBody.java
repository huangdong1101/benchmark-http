package com.mamba.benchmark.http.base.body;

import java.util.Objects;

public class StringBody implements HttpBody<String> {

    private final String text;

    public StringBody(String text) {
        this.text = Objects.requireNonNull(text);
    }

    @Override
    public String get() {
        return this.text;
    }
}
