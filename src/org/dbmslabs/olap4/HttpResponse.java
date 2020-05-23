package org.dbmslabs.olap4;

public interface HttpResponse {
        void addHeader(String name, String value);
        boolean containsHeader(String name);
}
