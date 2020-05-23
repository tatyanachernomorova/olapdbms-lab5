package org.dbmslabs.olap4;

import org.dbmslabs.olap4.bytes.BytesArray;
import org.dbmslabs.olap4.bytes.BytesReference;
import org.elasticsearch.common.xcontent.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;


public class BytesRestResponse extends RestResponse {

    public static final String TEXT_CONTENT_TYPE = "text/plain; charset=UTF-8";

    private static final String STATUS = "status";
    private static final String OBJECT = "object";

    private final RestStatus status;
    private final BytesReference content;
    private final String contentType;

    /**
     * Creates a new response based on {@link XContentBuilder}.
     */
    public BytesRestResponse(RestStatus status, XContentBuilder builder) {
        this(status, builder.contentType().mediaType(), BytesReference.bytes(builder));
    }

    public BytesRestResponse(RestStatus status, Object o) throws IOException {
        this.status = status;
        try (XContentBuilder builder = build(status, o)) {
            this.content = BytesReference.bytes(builder);
            this.contentType = builder.contentType().mediaType();
        }
    }

    /**
     * Creates a new plain text response.
     */
    public BytesRestResponse(RestStatus status, String content) {
        this(status, TEXT_CONTENT_TYPE, new BytesArray(content));
    }

    /**
     * Creates a new plain text response.
     */
    public BytesRestResponse(RestStatus status, String contentType, String content) {
        this(status, contentType, new BytesArray(content));
    }

    /**
     * Creates a binary response.
     */
    public BytesRestResponse(RestStatus status, String contentType, byte[] content) {
        this(status, contentType, new BytesArray(content));
    }

    /**
     * Creates a binary response.
     */
    public BytesRestResponse(RestStatus status, String contentType, BytesReference content) {
        this.status = status;
        this.content = content;
        this.contentType = contentType;
    }

    public BytesRestResponse(SocketChannel channel, Exception e) throws IOException {
        this(channel, RestStatus.BAD_REQUEST, e);
    }

    public BytesRestResponse(SocketChannel channel, RestStatus status, Exception e) throws IOException {
        this.status = status;
        try (XContentBuilder builder = build(channel, status, e)) {
            this.content = BytesReference.bytes(builder);
            this.contentType = builder.contentType().mediaType();
        }
    }

    @Override
    public String contentType() {
        return this.contentType;
    }

    @Override
    public BytesReference content() {
        return this.content;
    }

    @Override
    public RestStatus status() {
        return this.status;
    }

    private static XContentBuilder build(SocketChannel channel, RestStatus status, Exception e) throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject();
        builder.field(STATUS, status.name());
        builder.endObject();
        return builder;
    }

    private static XContentBuilder build( RestStatus status, Object o) throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject();
        builder.field(STATUS, status.name());
        builder.field(OBJECT, o.toString());
        builder.endObject();
        return builder;
    }


}

