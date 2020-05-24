package org.dbmslabs.olap4.actions;

import org.dbmslabs.olap4.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static org.dbmslabs.olap4.RestRequest.Method.GET;

public class GetAction extends BaseRestHandler {
    public List<RestHandler.Route> routes() {
        return java.util.List.of(new RestHandler.Route(GET, "/table/{table}/_id/{id}"),new RestHandler.Route(GET, "/table/{table}/checkpoint"));
    }


    public String getName() {
        return "get_id_action";
    }

    protected Consumer<SocketChannel> prepareRequest(RestRequest request) throws IOException {
        InMemoryStore ims = InMemoryStore.ims;
        String rawPath = request.rawPath();
        System.out.println();
        if(rawPath.substring(rawPath.length()-10).equals("checkpoint")) {
            System.out.println("[GetAction] " + request.param("table") + " -- " + request.param("checkpoint"));

            HashMap<String, HashMap<String, String>> storage = ims.getStorage();
            boolean exists = ims.isTableExists(request.param("table"));
            if(exists){
                HashMap<String, String> table = storage.get(request.param("table"));
                for (HashMap.Entry entry : table.entrySet()) {
                    System.out.println("Key: " + entry.getKey() + " Value: "
                            + entry.getValue());
                }

                Properties properties = new Properties();

                for (HashMap.Entry<String,String> entry : table.entrySet()) {
                    properties.put(entry.getKey(), entry.getValue());
                }

                properties.store(new FileOutputStream("data.properties"), null);
            }
            return socketChannel -> {
                try {
                    BytesRestResponse brr;
                    if(exists){
                        brr = new BytesRestResponse(RestStatus.OK, new GetActionResult("success"));
                    } else {
                        brr = new BytesRestResponse(RestStatus.NOT_FOUND, new GetActionResult("table is not found"));
                    }

                    socketChannel.write(ByteBuffer.wrap(brr.content().utf8ToString().getBytes()));
                    socketChannel.socket().close();
                } catch (Exception e) {
                    System.out.println("[GetAction] exception on closing socket");
                }
            };

        } else {
            System.out.println("[GetAction] " + request.param("table") + " -- " + request.param("id"));
            boolean exists = ims.isKeyOrTableExists(request.param("table"), request.param("id"));
            String value = null;
            if (exists) {
                value = ims.get(request.param("table"), request.param("id"));
            }
            String process = value;
            return socketChannel -> {
                try {
                    BytesRestResponse brr;
                    if (process != null) {
                        brr = new BytesRestResponse(RestStatus.FOUND, new GetActionResult(process));
                    } else {
                        brr = new BytesRestResponse(RestStatus.NOT_FOUND, new GetActionResult("key is not found"));
                    }
                    socketChannel.write(ByteBuffer.wrap(brr.content().utf8ToString().getBytes()));
                    socketChannel.socket().close();

                } catch (Exception e) {
                    System.out.println("[GetAction] exception on closing socket");
                }
            };
        }
        /*return socketChannel -> {
            try {
                BytesRestResponse brr;
                brr = new BytesRestResponse(RestStatus.NOT_FOUND, "dsds");
                socketChannel.write(ByteBuffer.wrap(brr.content().utf8ToString().getBytes()));
                socketChannel.socket().close();

            } catch (Exception e) {
                System.out.println("[GetAction] exception on closing socket");
            }
        };*/
    }
    private class GetActionResult{
        private String objectId;
        public GetActionResult(String data) {
            this.objectId = data;
        }
        @Override
        public String toString() {
            return objectId;
        }
    }
}
