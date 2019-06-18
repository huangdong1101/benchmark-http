package com.mamba.benchmark.http.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.net.MediaType;
import com.mamba.benchmark.common.util.TraceIdGenerator;
import com.mamba.benchmark.http.base.body.FileBody;
import com.mamba.benchmark.http.base.body.HttpBody;
import com.mamba.benchmark.http.base.body.MultipartBody;
import com.mamba.benchmark.http.base.body.StringBody;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HttpRequest {

    private final HttpMethod method;

    private final Uri uri;

    private final MediaType mediaType;

    private final HttpBody body;

    private final HttpHeaders headers;

    private final List<Cookie> cookies;

    private static final String TRACE_ID_SIGN = "ab";

    private static final String HEADER_TRACE_ID = "_RPC_TRACE_ID_";

    public HttpRequest(HttpMethod method, Uri uri, MediaType mediaType, HttpBody body, HttpHeaders headers, List<Cookie> cookies) {
        this.method = method;
        this.uri = uri;
        this.mediaType = mediaType;
        this.body = body;
        this.headers = headers;
        this.cookies = cookies;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Uri getUri() {
        return uri;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public HttpBody getBody() {
        return body;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public static HttpRequest parse(String text) {
        JSONObject json = JSONObject.parseObject(text);
        RequestBuilder builder = new RequestBuilder(json.getString("method"));
        builder.setUri(Uri.create(json.getString("url")));
        HttpMethod method = parseMethod(json.getString("method"));
        Uri uri = parseUri(json.getString("url"));
        MediaType mediaType = parseContentType(json.getString("contentType"));
        HttpBody body = parseBody(mediaType, json, "body");
        HttpHeaders headers = parseHeaders(json.getJSONArray("headers"));
        List<Cookie> cookies = parseCookies(json.getJSONArray("cookies"));
        return new HttpRequest(method, uri, mediaType, body, headers, cookies);
    }

    private static HttpMethod parseMethod(String method) {
        if (method == null) {
            return null;
        }
        return HttpMethod.valueOf(method.toUpperCase());
    }

    private static Uri parseUri(String url) {
        if (url == null) {
            return null;
        }
        return Uri.create(url);
    }

    private static MediaType parseContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return null;
        }
        return MediaType.parse(contentType);
    }

    private static HttpBody parseBody(MediaType mediaType, JSONObject json, String field) {
        if (mediaType == null) {
            return null;
        }
        if ("multipart".equals(mediaType.type())) {
            return parseMultipartBody(json.getJSONArray(field));
        }
        Object body = json.get(field);
        if (body == null) {
            return null;
        }
        String bodyStr = body.toString();
        File file = new File(bodyStr);
        if (file.exists() && file.isFile()) {
            return new FileBody(file);
        } else {
            return new StringBody(bodyStr);
        }
    }

    private static MultipartBody parseMultipartBody(JSONArray parts) {
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        MultipartBody body = new MultipartBody();
        for (int i = 0; i < parts.size(); i++) {
            addBodyPart(body, parts.getJSONObject(i));
        }
        return body;
    }

    private static void addBodyPart(MultipartBody body, JSONObject json) {
        String name = json.getString("name");
        String text = json.getString("text");
        String file = json.getString("file");
        String contentType = json.getString("contentType");
        if (name == null) {
            throw new NullPointerException("Empty name of part");
        }
        if (text == null) {
            if (file == null) {
                throw new NullPointerException("Empty text & file of part");
            }
            body.add(name, text, contentType);
        } else {
            if (file != null) {
                throw new IllegalArgumentException("Illegal part, text & file both be set");
            }
            body.add(name, new File(file), contentType);
        }
    }

    private static HttpHeaders parseHeaders(JSONArray headerArr) {
        HttpHeaders headers = new DefaultHttpHeaders(true);
        if (headerArr == null || headerArr.isEmpty()) {
            return headers;
        }
        for (int i = 0; i < headerArr.size(); i++) {
            JSONObject header = headerArr.getJSONObject(i);
            String name = header.getString("name");
            String value = header.getString("value");
            headers.add(name, value);
        }
        return headers;
    }

    private static List<Cookie> parseCookies(JSONArray cookieArr) {
        if (cookieArr == null || cookieArr.isEmpty()) {
            return Collections.emptyList();
        }
        List<Cookie> cookies = new ArrayList<>(cookieArr.size());
        for (int i = 0; i < cookieArr.size(); i++) {
            JSONObject cookie = cookieArr.getJSONObject(i);
            String name = cookie.getString("name");
            String value = cookie.getString("value");
            cookies.add(new DefaultCookie(name, value));
        }
        return cookies;
    }

    public Request toRequest() {
        RequestBuilder builder = new RequestBuilder(this.getMethod().name());
        builder.setUrl(this.getUri().toString());
        HttpBody body = this.getBody();
        if (body != null) {
            if (body instanceof StringBody) {
                builder.setBody(((StringBody) body).get());
            } else if (body instanceof FileBody) {
                builder.setBody(((FileBody) body).get());
            } else if (body instanceof MultipartBody) {
                builder.setBodyParts(((MultipartBody) body).get());
            }
        }
        builder.setHeaders(this.getHeaders());
        builder.setCookies(this.getCookies());
        MediaType mediaType = this.getMediaType();
        if (mediaType != null) {
            builder.addHeader(HttpHeaderNames.CONTENT_TYPE, mediaType.toString());
        }
        builder.addHeader(HEADER_TRACE_ID, TraceIdGenerator.genTraceId(TRACE_ID_SIGN));
        return builder.build();
    }
}
