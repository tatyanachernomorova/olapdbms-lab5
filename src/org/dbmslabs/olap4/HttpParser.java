package org.dbmslabs.olap4;

import org.dbmslabs.olap4.bytes.ByteBufferReference;
import org.dbmslabs.olap4.bytes.BytesReference;
import java.nio.ByteBuffer;
import java.util.*;


public class HttpParser implements HttpRequest {
    final Map<String, List<String>> headers = new HashMap<>();
    private final ByteBuffer buffer;
    private final ByteBufferReference bbr;
    private String method;
    private String uri;
    private BytesReference content;

    HttpParser(ByteBuffer b) {
        this.buffer = clone(b);
        this.bbr = new ByteBufferReference(buffer);
        parse();
    }

    private static ByteBuffer clone(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }
    protected void parse() {

        int window_ping = bbr.indexOf((byte)32, 0);
        this.method = bbr.slice(0, window_ping).utf8ToString();
        int window_pong = bbr.indexOf((byte)32, window_ping + 1);
        this.uri = bbr.slice(window_ping + 1, window_pong - 1 - window_ping).utf8ToString();
        window_ping = bbr.indexOf((byte)13, window_pong);
        window_ping += 2;
        //headers
        int nafnaf = 0;
        while(true)
        {
            window_pong = bbr.indexOf((byte)32, window_ping);

            String key = bbr.slice(window_ping, window_pong - window_ping - 1).utf8ToString();
            nafnaf = bbr.indexOf((byte)13, window_pong);
            String value =  bbr.slice(window_pong + 1, nafnaf - window_pong - 1).utf8ToString();

            this.headers.put(key,
                    Arrays.asList(value));

            if (bbr.get(nafnaf + 1) == 10 && bbr.get(nafnaf + 2) == 13 && bbr.get(nafnaf + 3) == 10) {
                break;
            } else {
                window_ping = nafnaf + 2;
            }
        }
        //content
        nafnaf += 4;
        if (this.headers.containsKey("Content-Length")) {
            this.content = bbr.slice(nafnaf, Integer.parseInt(this.headers.get("Content-Length").get(0)));
        } else {
            this.content = bbr.slice(0,0);
        }
    }
    public RestRequest.Method method() {
        return RestRequest.Method .valueOf(method);
    }

    /**
     * The uri of the rest request, with the query string.
     */
    public String uri() {
        return uri;
    }

    public BytesReference content() {
        return content;
    }

    /**
     * Get all of the headers and values associated with the headers. Modifications of this map are not supported.
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> strictCookies() {
        return null;
    }

    public HttpVersion protocolVersion() {
        return  HttpVersion.HTTP_1_1;
    }

    public HttpRequest removeHeader(String header) {
        throw new RuntimeException("[HttpParser] unsupported");
    }

    /**
     * Create an http response from this request and the supplied status and content.
     */
    public HttpResponse createResponse(RestStatus status, BytesReference content) {
        return new NioHttpResponse(status);
    }

    /**
     * Release any resources associated with this request. Implementations should be idempotent. The behavior of {@link #content()}
     * after this method has been invoked is undefined and implementation specific.
     */
    public void release() {
        throw new RuntimeException("[HttpParser] unsupported");
    }

    /**
     * If this instances uses any pooled resources, creates a copy of this instance that does not use any pooled resources and releases
     * any resources associated with this instance. If the instance does not use any shared resources, returns itself.
     * @return a safe unpooled http request
     */
    public HttpRequest releaseAndCopy() {
        throw new RuntimeException("[HttpParser] unsupported");
    }

}