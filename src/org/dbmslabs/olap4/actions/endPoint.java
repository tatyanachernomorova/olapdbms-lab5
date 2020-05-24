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

import static org.dbmslabs.olap4.RestRequest.Method.PUT;

public class endPoint  extends BaseRestHandler{
    static int i = 0;
    @Override
    public String getName() {
        return "endpoint";
    }

    @Override
    public List<RestHandler.Route> routes() {
        return List.of(new RestHandler.Route(PUT, "/checkpoint"));
    }

    @Override
    protected Consumer<SocketChannel> prepareRequest(RestRequest request) throws IOException {

        InMemoryStore ims = InMemoryStore.ims;
            String filePath = "data/";

            HashMap<String, HashMap<String, String>> storage = ims.getStorage();
            i=0;
            for (HashMap.Entry<String, HashMap<String, String>> entry : storage.entrySet()) {
                    HashMap<String, String> table = entry.getValue();

                    Properties properties = new Properties();

                    for (HashMap.Entry<String, String> entryD : table.entrySet()) {
                        properties.put(entryD.getKey(), entryD.getValue());
                    }

                    properties.store(new FileOutputStream(filePath + entry.getKey().toString() + ".properties"), null);
                    i++;
            }
            return socketChannel -> {
                try {
                    BytesRestResponse brr;
                    String response = "Save " + i + " tables";
                    brr = new BytesRestResponse(RestStatus.OK, new endPoint.GetActionResult(response));

                    socketChannel.write(ByteBuffer.wrap(brr.content().utf8ToString().getBytes()));
                    socketChannel.socket().close();
                } catch (Exception e) {
                    System.out.println("[EndPoint] exception on closing socket");
                }
            };
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
