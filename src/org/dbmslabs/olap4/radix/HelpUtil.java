package org.dbmslabs.olap4.radix;
public class HelpUtil {
    /*
        getArrayIndex() - метод для поиска элемента внутри массива
     */
    public static int getArrayIndex(char[] array,int elem) {
        int index = 0;
        for(int i = 0;i < array.length; i++){
            if(array[i] == elem){
                index = i;
                break;
            }
        }
        return index;
    }
}
