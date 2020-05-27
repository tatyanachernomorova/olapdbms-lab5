package org.dbmslabs.olap4.radix;


public class RadixMap {
    private TableMap[] tableMaps;
    /*
        containsKey() - метод проверяющий существование  таблицы
        tableName - имя искомой таблицы
     */
    public boolean containsKey(String tableName){
        if(tableMaps == null){
            return false;
        }
        for (TableMap tableMap : tableMaps) {
            if(tableMap.nameTable.equals(tableName)){
                return true;
            }
        }
        return false;
    }
    /*
        getTable() - метод, возвращающий ссылку на экземпляр таблицы
        tableName - имя искомой таблицы
     */
    public RadixTree getTable(String tableName){
        for (TableMap tableMap : tableMaps) {
            if(tableMap.nameTable.equals(tableName)){
                return tableMap.radixTree;
            }
        }
        return null;
    }
    /*
        putTable() - метод, создающий новую таблицу
        tableName - имя искомой таблицы
     */
    public void putTable(String tableName){
        if(tableMaps != null) {
            TableMap[] tmpMaps = new TableMap[tableMaps.length + 1];
            for (int i = 0; i < tableMaps.length; i++) {
                tmpMaps[i] = tableMaps[i];
            }
            tmpMaps[tableMaps.length].nameTable = tableName;
            tmpMaps[tableMaps.length].radixTree = new RadixTree();

            tableMaps = tmpMaps.clone();
        } else {
            tableMaps = new TableMap[1];
            TableMap tableMap = new TableMap();
            tableMap.nameTable = tableName;
            tableMap.radixTree = new RadixTree();
            tableMaps[0] = tableMap;
        }
    }
}
/*
    TableMap - структура для хранения данных о таблицах
 */
class TableMap{
    String nameTable;
    RadixTree radixTree;
}

