package org.dbmslabs.olap4;

import org.dbmslabs.olap4.radix.RadixMap;
import org.dbmslabs.olap4.radix.RadixTree;

import java.util.HashMap;
import java.util.UUID;


public class InMemoryStore {
    public static InMemoryStore ims = new InMemoryStore();

    private RadixMap storage = new RadixMap();

    public  String set(String table, String value) {
        String sequnce = UUID.randomUUID().toString();
        synchronized(storage) {
            if (storage.containsKey(table)) {
                storage.getTable(table).insert(sequnce,value);
            } else {
                storage.putTable(table);
                storage.getTable(table).insert(sequnce,value);
            }
        }
        return sequnce;
    }
    public boolean isKeyOrTableExists(String table, String id) {
        synchronized(storage) {
            if ( !storage.containsKey(table) ) {
                return false;
            }
            if ( !storage.getTable(table).isExist(id)) {
                return false;
            }
        }
        return true;
    }
    public String get(String table, String id) {
        synchronized(storage) {
            return storage.getTable(table).select(id);
        }
    }
}
