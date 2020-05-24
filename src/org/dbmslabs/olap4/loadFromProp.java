package org.dbmslabs.olap4;

import java.io.File;
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
        String filePath = "data/";
        File myFolder = new File(filePath);
        int i = 0;
        try {
            for (File file : myFolder.listFiles()) {
                String fileName = file.getName();
                String nameTable = fileName.substring(0,fileName.length()-11);
                properties.load(new FileInputStream(filePath+fileName));

                for (String key : properties.stringPropertyNames()) {
                    ims.setOldData(nameTable,key,properties.get(key).toString());
                }

                System.out.println("[loadFromProp] " + nameTable + " -- " + "loading");
                i++;
            }
            System.out.println("[loadFromProp] load " + i + " tables");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
