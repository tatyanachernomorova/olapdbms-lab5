package org.dbmslabs.olap4;

import java.util.HashMap;
import java.util.UUID;


public class InMemoryStore {
    public static InMemoryStore ims = new InMemoryStore();

    private HashMap<String, HashMap<String, String>> storage =  new HashMap<>();


    public  String set(String table, String value) {
        String sequnce = UUID.randomUUID().toString();
        synchronized(storage) {
            if (storage.containsKey(table)) {
                storage.get(table).putIfAbsent(sequnce, value);
            } else {
                storage.put(table, new HashMap<>());
                storage.get(table).putIfAbsent(sequnce, value);
            }
        }
        return sequnce;
    }
    public boolean isKeyOrTableExists(String table, String id) {
        synchronized(storage) {
            if ( !storage.containsKey(table) ) {
                return false;
            }
            if ( !storage.get(table).containsKey(id)) {
                return false;
            }
        }
        return true;
    }
    public String get(String table, String id) {
        synchronized(storage) {
            return storage.get(table).get(id);
        }
    }
}
