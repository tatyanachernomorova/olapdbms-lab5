package org.dbmslabs.olap4;

import io.netty.channel.ChannelHandlerContext;
import org.apache.lucene.util.SetOnce;
import org.dbmslabs.olap4.actions.ActionModule;
import org.dbmslabs.olap4.actions.CreateTableAction;
import org.dbmslabs.olap4.actions.GetAction;
import org.dbmslabs.olap4.actions.endPoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class Node {
    static private int BUFFER_SIZE = 2048;

    private final Selector selector;
    private final String hostname;
    private final int port;

    private final NamedThreadPool processors;
    private final ActionModule actionModule;
    private ArrayList<Supplier<RestHandler>> actionPlugins;
    private final InMemoryStore ims;

    private Map<String, ServerContext> ctx = new HashMap<>();

    Node(String hostname, int port ) throws IOException {
         this.selector = Selector.open();
         this.hostname = hostname;
         this.port = port;
         this.processors = new NamedThreadPool("Processors", 5);

         this.actionPlugins = new ArrayList<>();
         this.actionPlugins.add(() -> (new CreateTableAction()));
         this.actionPlugins.add(() -> (new GetAction()));
        this.actionPlugins.add(() -> (new endPoint()));


         this.actionModule = new ActionModule(actionPlugins);

         this.ims = new InMemoryStore();
    }

    public void start() throws IOException,Exception  {

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(hostname, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT );
        ByteBuffer buffer = ByteBuffer.allocate(Node.BUFFER_SIZE);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {

                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    register(selector, serverSocket);
                }

                if (key.isReadable()) {
                    //ServerContext sc = (ServerContext)key.attachment();
                    handleBuffer(buffer, key);
                }
                iter.remove();
            }
        }
    }

    private void handleBuffer(ByteBuffer buffer, SelectionKey key)
            throws IOException,Exception {

        SocketChannel client = (SocketChannel) key.channel();
        client.read(buffer);

        ServerContext sc = (ServerContext)key.attachment();
        if (sc != null) {
            sc.put(buffer);
        } else {
            key.attach((Object)(new ServerContext(buffer, key)));
        }

        processors.execute(() -> {

            try {
                HttpToActionProcessing p = new HttpToActionProcessing(key, this.actionModule.getRestController() );
            } catch(Exception e ) {
                e.printStackTrace();
                System.out.println("[handleBuffer] -- exception -- " + e.getMessage() + " -- " + e.getStackTrace()[0].toString());
            }
            return true;
        } );



        buffer.clear();
    }


    private void register(Selector selector, ServerSocketChannel serverSocket)
            throws IOException {

        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }
}
