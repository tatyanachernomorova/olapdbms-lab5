package org.dbmslabs.olap4;

import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;

public interface RestHandler {


    void handleRequest(RestRequest request, SocketChannel channel) throws Exception;

    default boolean canTripCircuitBreaker() {
        return true;
    }


    default boolean supportsContentStream() {
        return false;
    }


    default boolean allowsUnsafeBuffers() {
        return false;
    }

    /**
     * The list of {@link Route}s that this RestHandler is responsible for handling.
     */
    default List<Route> routes() {
        return Collections.emptyList();
    }


    class Route {

        private final String path;
        private final RestRequest.Method method;

        public Route(RestRequest.Method method, String path) {
            this.path = path;
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public RestRequest.Method getMethod() {
            return method;
        }
    }


}
