package com.mamba.benchmark.http.base.body;

import java.io.File;
import java.util.Objects;

public class FileBody implements HttpBody<File> {

    private final File file;

    public FileBody(File file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override
    public File get() {
        return this.file;
    }
}
