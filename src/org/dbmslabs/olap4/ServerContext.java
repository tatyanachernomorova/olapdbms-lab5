package org.dbmslabs.olap4;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ServerContext {
    public ByteBuffer full_request;
    public SelectionKey sk;

    ServerContext(ByteBuffer b, SelectionKey sk) {
        this.full_request = b;
        this.sk = sk;
        put(b);
    }

    void put(ByteBuffer b) {
        this.full_request = b;
    }
}
