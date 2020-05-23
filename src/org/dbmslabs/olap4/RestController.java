package org.dbmslabs.olap4;

import org.dbmslabs.olap4.actions.BaseRestHandler;
import org.dbmslabs.olap4.actions.MethodHandlers;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class RestController {
    private final PathTrie<MethodHandlers> handlers = new PathTrie<>(RestUtils.REST_DECODER);
    private final UnaryOperator<RestHandler> handlerWrapper;

    RestController(UnaryOperator<RestHandler> handlerWrapper) {
        if (handlerWrapper == null) {
            handlerWrapper = h -> h; // passthrough if no wrapper set
        }
        this.handlerWrapper = handlerWrapper;
    }

    public RestController() {
        this.handlerWrapper = null;
    }

    public void dispatchRequest(RestRequest request, SocketChannel channel) {
        try {
            tryAllHandlers(request, channel);
        } catch (Exception e) {
            System.console().printf(" [dispatchRequest] exception %s", e.getMessage());
        }
    }

    Iterator<MethodHandlers> getAllHandlers(@Nullable Map<String, String> requestParamsRef, String rawPath) {
        final Supplier<Map<String, String>> paramsSupplier;
        if (requestParamsRef == null) {
            paramsSupplier = () -> null;
        } else {
            // Between retrieving the correct path, we need to reset the parameters,
            // otherwise parameters are parsed out of the URI that aren't actually handled.
            final Map<String, String> originalParams = Map.copyOf(requestParamsRef);
            paramsSupplier = () -> {
                // PathTrie modifies the request, so reset the params between each iteration
                requestParamsRef.clear();
                requestParamsRef.putAll(originalParams);
                return requestParamsRef;
            };
        }
        // we use rawPath since we don't want to decode it while processing the path resolution
        // so we can handle things like:
        // my_index/my_type/http%3A%2F%2Fwww.google.com
        return handlers.retrieveAll(rawPath, paramsSupplier);
    }

    private void tryAllHandlers(final RestRequest request, final SocketChannel channel) throws RuntimeException {

        final String rawPath = request.rawPath();
        final String uri = request.uri();
        final RestRequest.Method requestMethod;
        try {
            // Resolves the HTTP method and fails if the method is invalid
            requestMethod = request.method();
            // Loop through all possible handlers, attempting to dispatch the request
            Iterator<MethodHandlers> allHandlers = getAllHandlers(request.params(), rawPath);
            while (allHandlers.hasNext()) {
                final RestHandler handler;
                final MethodHandlers handlers = allHandlers.next();
                if (handlers == null) {
                    handler = null;
                } else {
                    handler = handlers.getHandler(requestMethod);
                }
                if (handler == null) {
                    if (handleNoHandlerFound(rawPath, requestMethod, uri, channel)) {
                        return;
                    }
                } else {
                    dispatchRequest(request, channel, handler);
                    return;
                }
            }
        } catch (final IllegalArgumentException e) {
            System.console().printf("[tryAllHandlers] exception " + e.getMessage());
            return;
        }
        // If request has not been handled, fallback to a bad request error.
        handleBadRequest(uri, requestMethod, channel);
    }

    private boolean handleNoHandlerFound(String rawPath, RestRequest.Method method, String uri, SocketChannel channel) throws RuntimeException {
        //System.out.println(" [handleNoHandlerFound] --> empty function --> serving " + rawPath);
        return false;
    }

    private void handleBadRequest(String uri, RestRequest.Method method, SocketChannel channel) {
        try {
            BytesRestResponse bbr = new BytesRestResponse(RestStatus.BAD_REQUEST,
                new Exception( "no handler found for uri [" + uri + "] and method [" + method + "]"));
            channel.write(ByteBuffer.wrap(bbr.content().utf8ToString().getBytes()));
            channel.socket().close();
            channel.close();
        } catch (Exception e) {
            System.out.println(" [handleBadRequest] --> could not write ");
        }
    }

    private void dispatchRequest(RestRequest request, SocketChannel channel, RestHandler handler) throws RuntimeException {
        final int contentLength = request.contentLength();
        if (contentLength > 0) {
            final XContentType xContentType = request.getXContentType();
            if (xContentType == null) {
                System.console().printf("[dispatchRequest] xContentType == null");
                return;
            }
            if (handler.supportsContentStream() && xContentType != XContentType.JSON && xContentType != XContentType.SMILE) {
                System.console().printf("[dispatchRequest] Content-Type [" + xContentType + "] does not support stream parsing. Use JSON or SMILE instead");
                return;
            }
        }
        SocketChannel responseChannel = channel;
        try {
            handler.handleRequest(request, responseChannel);
        } catch (Exception e) {
            System.console().printf("[dispatchRequest] " + e.getMessage());
        }
    }

    public void registerHandler(final RestHandler restHandler) {
        restHandler.routes().forEach(route -> registerHandler(route.getMethod(), route.getPath(), restHandler));
    }

    protected void registerHandler(RestRequest.Method method, String path, RestHandler handler) {

        //final RestHandler maybeWrappedHandler = handlerWrapper.apply(handler);
        handlers.insertOrUpdate(path, new MethodHandlers(path, handler, method),
                (mHandlers, newMHandler) -> mHandlers.addMethods(handler, method));
    }
}


