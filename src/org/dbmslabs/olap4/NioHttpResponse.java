package org.dbmslabs.olap4;

import java.util.Map;

public class NioHttpResponse implements HttpResponse{

    private final RestStatus status;
    private Map<String, String> headers;

    NioHttpResponse(RestStatus status) {
        this.status = status;
    }

    @Override
    public void addHeader(String name, String value) {
        headers.putIfAbsent(name, value);
    }

    @Override
    public boolean containsHeader(String name) {

        return headers.containsKey(name);
    }

}
