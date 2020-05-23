package org.dbmslabs.olap4;

import org.dbmslabs.olap4.bytes.BytesReference;

import java.util.*;

public abstract class RestResponse {

    private Map<String, List<String>> customHeaders;

    /**
     * The response content type.
     */
    public abstract String contentType();

    /**
     * The response content. Note, if the content is {@link org.elasticsearch.common.lease.Releasable} it
     * should automatically be released when done by the channel sending it.
     */
    public abstract BytesReference content();

    /**
     * The rest status code.
     */
    public abstract RestStatus status();

    /**
     * Add a custom header.
     */
    public void addHeader(String name, String value) {
        if (customHeaders == null) {
            customHeaders = new HashMap<>(2);
        }
        List<String> header = customHeaders.get(name);
        if (header == null) {
            header = new ArrayList<>();
            customHeaders.put(name, header);
        }
        header.add(value);
    }

    /**
     * Returns custom headers that have been added. This method should not be used to mutate headers.
     */
    public Map<String, List<String>> getHeaders() {
        if (customHeaders == null) {
            return Collections.emptyMap();
        } else {
            return customHeaders;
        }
    }
}

