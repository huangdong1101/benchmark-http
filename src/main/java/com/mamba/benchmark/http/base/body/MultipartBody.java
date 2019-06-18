package com.mamba.benchmark.http.base.body;

import org.asynchttpclient.request.body.multipart.FilePart;
import org.asynchttpclient.request.body.multipart.Part;
import org.asynchttpclient.request.body.multipart.StringPart;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MultipartBody implements HttpBody<List<Part>> {

    private final List<Part> parts = new LinkedList<>();

    public MultipartBody() {
    }

    @Override
    public List<Part> get() {
        return Collections.unmodifiableList(this.parts);
    }

    public void add(String name, String value) {
        this.parts.add(new StringPart(name, value));
    }

    public void add(String name, String value, String contentType) {
        this.parts.add(new StringPart(name, value, contentType));
    }

    public void add(String name, File file) {
        this.parts.add(new FilePart(name, file));
    }

    public void add(String name, File file, String contentType) {
        this.parts.add(new FilePart(name, file, contentType));
    }
}
