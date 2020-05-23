package org.dbmslabs.olap4.actions;

import org.dbmslabs.olap4.RestHandler;
import org.dbmslabs.olap4.RestRequest;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

public abstract class BaseRestHandler implements RestHandler {

    private final LongAdder usageCount = new LongAdder();

    public final long getUsageCount() {
        return usageCount.sum();
    }

    public abstract String getName();

    @Override
    public abstract List<Route> routes();

    @Override
    public final void handleRequest(RestRequest request, SocketChannel channel) throws Exception {
        final Consumer<SocketChannel> action = prepareRequest(request);

        usageCount.increment();
        action.accept(channel);
    }

    protected abstract Consumer<SocketChannel> prepareRequest(RestRequest request) throws IOException;
}