package com.mamba.benchmark.http.generator;

import com.mamba.benchmark.http.base.HttpRequest;
import com.mamba.benchmark.http.client.NettyHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

public class InvariantTaskGenerator implements IntFunction<List<Runnable>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvariantTaskGenerator.class);

    private final NettyHttpClient httpClient;

    private final HttpRequest request;

    private final boolean async;

    public InvariantTaskGenerator(NettyHttpClient httpClient, HttpRequest request, boolean async) {
        this.httpClient = httpClient;
        this.request = request;
        this.async = async;
    }

    @Override
    public List<Runnable> apply(int num) {
        List<Runnable> tasks = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            Request request = this.request.toRequest();
            tasks.add(() -> execute(this.httpClient, request, this.async));
        }
        return tasks;
    }

    private static void execute(NettyHttpClient httpClient, Request request, boolean async) {
        CompletableFuture<Response> future = httpClient.execute(request);
        if (!async) {
            future.join();
        }
    }

    public static InvariantTaskGenerator newInstance(NettyHttpClient httpClient, HttpRequest request, boolean async) throws Exception {
        return new InvariantTaskGenerator(httpClient, request, async);
    }
}
