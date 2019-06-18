package com.mamba.benchmark.http.client;

import com.mamba.benchmark.common.executor.impl.CustomThreadFactory;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.request.NettyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class NettyHttpClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpClient.class);

    private final AsyncHttpClient httpClient;

    public NettyHttpClient(AsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<Response> execute(Request request) {
        return this.httpClient.executeRequest(request, new ProgressHandler()).toCompletableFuture();
    }

    @Override
    public void close() throws IOException {
        this.httpClient.close();
    }

    private static class ProgressHandler extends AsyncCompletionHandler<Response> {

        private long beginTime;

        @Override
        public void onRequestSend(NettyRequest request) {
            this.beginTime = System.currentTimeMillis();
            request.getHttpRequest().headers();
            LOGGER.info("Send");
        }

        @Override
        public Response onCompleted(Response response) throws Exception {
            long endTime = System.currentTimeMillis();
            long latency = endTime - beginTime;
            LOGGER.info("Response {} ms, status: {}", latency, response.getStatusCode());
            return response;
        }

        @Override
        public void onThrowable(Throwable t) {
            LOGGER.info("Error: {} {}", t.getClass().getSimpleName(), t.getMessage());
        }
    }

    public static class Builder {

        private final DefaultAsyncHttpClientConfig.Builder builder = Dsl.config();

        private Builder() {
            this.builder.setUserAgent("Benchmark/1.0");
            this.builder.setThreadFactory(new CustomThreadFactory("NHC"));
            this.builder.setIoThreadsCount(Math.max(1, Runtime.getRuntime().availableProcessors() * 3 / 4));
            this.builder.setMaxConnections(50);
            this.builder.setMaxConnectionsPerHost(5);
            this.builder.setConnectTimeout(1000);
        }

        public static NettyHttpClient.Builder custom() {
            return new NettyHttpClient.Builder();
        }

        public NettyHttpClient.Builder ioThreadsCount(int nThreads) {
            this.builder.setIoThreadsCount(nThreads);
            return this;
        }

        public NettyHttpClient.Builder maxConnections(int maxConnections) {
            this.builder.setMaxConnections(maxConnections);
            return this;
        }

        public NettyHttpClient.Builder maxConnectionsPerHost(int maxConnectionsPerHost) {
            this.builder.setMaxConnectionsPerHost(maxConnectionsPerHost);
            return this;
        }

        public NettyHttpClient.Builder keepAlive(boolean keepAlive) {
            this.builder.setKeepAlive(keepAlive);
            return this;
        }

        public NettyHttpClient.Builder timeout(int timeout) {
            this.builder.setConnectTimeout(Math.min(timeout, 1000)).setRequestTimeout(timeout).setReadTimeout(timeout);
            return this;
        }

        public NettyHttpClient.Builder requestTimeout(int requestTimeout) {
            this.builder.setRequestTimeout(requestTimeout);
            return this;
        }

        public NettyHttpClient.Builder readTimeout(int readTimeout) {
            this.builder.setReadTimeout(readTimeout);
            return this;
        }

        public NettyHttpClient.Builder maxRedirects(int maxRedirects) {
            if (maxRedirects > 0) {
                this.builder.setFollowRedirect(true).setMaxRedirects(maxRedirects);
            } else {
                this.builder.setFollowRedirect(false);
            }

            return this;
        }

        public NettyHttpClient build() {
            DefaultAsyncHttpClient httpClient = new DefaultAsyncHttpClient(this.builder.build());
            return new NettyHttpClient(httpClient);
        }
    }
}
