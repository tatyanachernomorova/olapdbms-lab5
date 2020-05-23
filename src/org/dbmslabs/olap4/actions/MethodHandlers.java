package org.dbmslabs.olap4.actions;

import org.dbmslabs.olap4.RestHandler;
import org.dbmslabs.olap4.RestRequest;
import org.elasticsearch.common.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class MethodHandlers {

    private final String path;
    private final Map<RestRequest.Method, RestHandler> methodHandlers;

    public MethodHandlers(String path, RestHandler handler, RestRequest.Method... methods) {
        this.path = path;
        this.methodHandlers = new HashMap<>(methods.length);
        for (RestRequest.Method method : methods) {
            methodHandlers.put(method, handler);
        }
    }

    /**
     * Add a handler for an additional array of methods. Note that {@code MethodHandlers}
     * does not allow replacing the handler for an already existing method.
     */
    public MethodHandlers addMethods(RestHandler handler, RestRequest.Method... methods) {
        for (RestRequest.Method method : methods) {
            RestHandler existing = methodHandlers.putIfAbsent(method, handler);
            if (existing != null) {
                throw new IllegalArgumentException("Cannot replace existing handler for [" + path + "] for method: " + method);
            }
        }
        return this;
    }

    /**
     * Returns the handler for the given method or {@code null} if none exists.
     */
    @Nullable
    public RestHandler getHandler(RestRequest.Method method) {
        return methodHandlers.get(method);
    }

    /**
     * Return a set of all valid HTTP methods for the particular path
     */
    public Set<RestRequest.Method> getValidMethods() {
        return methodHandlers.keySet();
    }
}
