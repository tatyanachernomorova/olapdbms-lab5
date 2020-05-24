package org.dbmslabs.olap4;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class loadFromProp {
    private InMemoryStore ims;

    public loadFromProp(){
        this.ims = InMemoryStore.ims;
    }
    public void loadAll(){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("data.properties"));
            String nameTable = "exampletable";
            for (String key : properties.stringPropertyNames()) {
                ims.setOldData(nameTable,key,properties.get(key).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
