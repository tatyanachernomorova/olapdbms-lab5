package org.dbmslabs.olap4;

import org.elasticsearch.common.xcontent.NamedXContentRegistry;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collections;

public class HttpToActionProcessing {

    HttpToActionProcessing(SelectionKey sc, RestController rc) throws Exception {

        HttpParser parser = new HttpParser(((ServerContext)sc.attachment()).full_request);
        System.out.println("[new request] " + parser.method() + ":" + parser.uri());
        parser.getHeaders().forEach((k,v) -> {
            System.out.println("key= " + k + " ; v= " + v.get(0));
        });
        NamedXContentRegistry registry = new NamedXContentRegistry(Collections.emptyList());
        RestRequest r = RestRequest.request(registry, (HttpRequest) parser);

        rc.dispatchRequest(r, (SocketChannel) sc.channel());

    }


}
