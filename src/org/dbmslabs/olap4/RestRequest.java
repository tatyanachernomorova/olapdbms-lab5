package org.dbmslabs.olap4;


import org.dbmslabs.olap4.bytes.BytesReference;
import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentType;

import java.nio.channels.ServerSocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static org.elasticsearch.common.unit.TimeValue.parseTimeValue;

public class RestRequest implements ToXContent.Params {
    // tchar pattern as defined by RFC7230 section 3.2.6
    private static final Pattern TCHAR_PATTERN = Pattern.compile("[a-zA-z0-9!#$%&'*+\\-.\\^_`|~]+");
    private static final AtomicLong requestIdGenerator = new AtomicLong();

    private final NamedXContentRegistry xContentRegistry;
    private final Map<String, String> params;
    private final Map<String, List<String>> headers;
    private final String rawPath;
    private final Set<String> consumedParams = new HashSet<>();
    private final XContentType xContentType;

    private HttpRequest httpRequest;

    private boolean contentConsumed = false;

    private final long requestId;

    public enum Method {
        GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH, TRACE, CONNECT
    }

    protected RestRequest(NamedXContentRegistry xContentRegistry, Map<String, String> params, String path,
                          Map<String, List<String>> headers, HttpRequest httpRequest) {
        this(xContentRegistry, params, path, headers, httpRequest, requestIdGenerator.incrementAndGet());
    }

    private RestRequest(NamedXContentRegistry xContentRegistry, Map<String, String> params, String path,
                        Map<String, List<String>> headers, HttpRequest httpRequest, long requestId) {
        final XContentType xContentType;
        try {
            xContentType = parseContentType(headers.get("Content-Type"));
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        this.xContentType = xContentType;
        this.xContentRegistry = xContentRegistry;
        this.httpRequest = httpRequest;
        this.params = params;
        this.rawPath = path;
        this.headers = Collections.unmodifiableMap(headers);
        this.requestId = requestId;
    }

    public String rawPath() {
        return rawPath;
    }

    private static String path(final String uri) {
        final int index = uri.indexOf('?');
        if (index >= 0) {
            return uri.substring(0, index);
        } else {
            return uri;
        }
    }

    public Map<String, String> params() {
        return params;
    }

    public String uri() {
        return httpRequest.uri();
    }

    public boolean hasContent() {
        return contentLength() > 0;
    }

    public int contentLength() {
        return httpRequest.content().length();
    }

    public BytesReference content() {
        return httpRequest.content();
    }

    public final String path() {
        return RestUtils.decodeComponent(rawPath());
    }

    public Method method() {
        return httpRequest.method();
    }

    public static RestRequest request(NamedXContentRegistry xContentRegistry, HttpRequest httpRequest) {
        Map<String, String> params = params(httpRequest.uri());
        String path = path(httpRequest.uri());
        return new RestRequest(xContentRegistry, params, path, httpRequest.getHeaders(), httpRequest,
                requestIdGenerator.incrementAndGet());
    }

    private static Map<String, String> params(final String uri) {
        final Map<String, String> params = new HashMap<>();
        int index = uri.indexOf('?');
        if (index >= 0) {
            try {
                RestUtils.decodeQueryString(uri, index + 1, params);
            } catch (final IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
        return params;
    }

    public static XContentType parseContentType(List<String> header) {
        if (header == null || header.isEmpty()) {
            return null;
        } else if (header.size() > 1) {
            throw new IllegalArgumentException("only one Content-Type header should be provided");
        }

        String rawContentType = header.get(0);
        final String[] elements = rawContentType.split("[ \t]*;");
        if (elements.length > 0) {
            final String[] splitMediaType = elements[0].split("/");
            if (splitMediaType.length == 2 && TCHAR_PATTERN.matcher(splitMediaType[0]).matches()
                    && TCHAR_PATTERN.matcher(splitMediaType[1].trim()).matches()) {
                return XContentType.fromMediaType(elements[0]);
            } else {
                throw new IllegalArgumentException("invalid Content-Type header [" + rawContentType + "]");
            }
        }
        throw new IllegalArgumentException("empty Content-Type header");
    }

    public final Map<String, List<String>> getHeaders() {
        return headers;
    }

    public final long getRequestId() {
        return requestId;
    }

    public float paramAsFloat(String key, float defaultValue) {
        String sValue = param(key);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(sValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse float parameter [" + key + "] with value [" + sValue + "]", e);
        }
    }

    public int paramAsInt(String key, int defaultValue) {
        String sValue = param(key);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse int parameter [" + key + "] with value [" + sValue + "]", e);
        }
    }

    public long paramAsLong(String key, long defaultValue) {
        String sValue = param(key);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(sValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse long parameter [" + key + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public boolean paramAsBoolean(String key, boolean defaultValue) {
        String rawParam = param(key);
        // Treat empty string as true because that allows the presence of the url parameter to mean "turn this on"
        if (rawParam != null && rawParam.length() == 0) {
            return true;
        } else {
            return Booleans.parseBoolean(rawParam, defaultValue);
        }
    }

    @Override
    public Boolean paramAsBoolean(String key, Boolean defaultValue) {
        return Booleans.parseBoolean(param(key), defaultValue);
    }


    public final boolean hasParam(String key) {
        return params.containsKey(key);
    }

    @Override
    public final String param(String key) {
        consumedParams.add(key);
        return params.get(key);
    }

    @Override
    public final String param(String key, String defaultValue) {
        consumedParams.add(key);
        String value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Nullable
    public final XContentType getXContentType() {
        return xContentType;
    }
}
