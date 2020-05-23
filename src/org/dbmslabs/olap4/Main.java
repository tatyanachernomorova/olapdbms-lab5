package org.dbmslabs.olap4;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException,Exception {

        Node node = new Node("127.0.0.1", 5454);
        node.start();
    }
}
