package org.dbmslabs.olap4.actions;

import org.dbmslabs.olap4.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.function.Consumer;

import static org.dbmslabs.olap4.RestRequest.Method.PUT;

public class CreateTableAction extends BaseRestHandler {
    public List<RestHandler.Route> routes() {
        return List.of(new RestHandler.Route(PUT, "/table/{table}"));
    }


    public String getName() {
        return "insert_into_table_action";
    }

    protected Consumer<SocketChannel> prepareRequest(RestRequest request) throws IOException
    {

        System.out.println("[CreateTableAction] prepareRequest" + request.param("table"));

        String key = InMemoryStore.ims.set(request.param("table"), request.content().utf8ToString());

        return socketChannel -> {
            try {
                BytesRestResponse brr = new BytesRestResponse(RestStatus.CREATED, new TableActionResult(key));
                socketChannel.write(ByteBuffer.wrap(brr.content().utf8ToString().getBytes()));
                socketChannel.socket().close();

            } catch (Exception e) {
                System.out.println("[prepareRequest] -- exception " + e.getMessage());
            }
        };
    }

    private class TableActionResult{
        private String objectId;
        public TableActionResult(String data) {
            this.objectId = data;
        }
        @Override
        public String toString() {
            return objectId;
        }
    }
}
