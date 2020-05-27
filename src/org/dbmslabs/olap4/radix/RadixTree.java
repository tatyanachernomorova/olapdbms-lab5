package org.dbmslabs.olap4.radix;

import static org.dbmslabs.olap4.radix.HelpUtil.getArrayIndex;

public class RadixTree {
    private Node parent;
    //arrayKeys массив всех символов, из которых может состоять ключ. необходим для построения древа
    //на данный момент в uuid не используется
    private char[] arrayKeys = new char[]{
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            '1','2','3','4','5','6','7','8','9','0',
            '-'} ;
    private int lengthKeys;

    public RadixTree(){
        lengthKeys = arrayKeys.length;
        parent = new Node(lengthKeys);
    }
    /*
        insert() - метод для вставки пар значений ключ-значение в древо, перед вызовом необходимо убедиться в отсутствии ключа в древе
        key - уникальный идентификатор(ключ)
        value - символьная последовательность (значение)
     */
    public void insert(String key,String value) {
        Node node = parent;
        for(int i = 0; i < key.length(); i++){
            int index = getArrayIndex(arrayKeys,key.charAt(i));
            if(node.children[index] == null){
                Node tmp = new Node(lengthKeys);
                node.children[index] = tmp;
                node = tmp;
            }else{
                node = node.children[index];
            }
        }
        node.value = value;
    }
    /*
        select() - метод для поиска значения по ключу; в случае отсутствия пары ключ-значение вернет null
        key - уникальный идентификатор(ключ)
     */
    public String select(String key){
        Node node = parent;
        for(int i = 0; i < key.length(); i++){
            int index = getArrayIndex(arrayKeys,key.charAt(i));
            if(node.children[index] != null){
                node = node.children[index];
            }else{
                return null;
            }
        }
        return node.value;
    }
    /*
        isExist() - метод для проверки существования ключа в древе; в случае отсутствия вернет false
        key - уникальный идентификатор(ключ)
     */
    public boolean isExist(String key){
        Node node = parent;
        for(int i = 0; i < key.length(); i++){
            int index = getArrayIndex(arrayKeys,key.charAt(i));
            if(node.children[index] != null){
                node = node.children[index];
            }else{
                return false;
            }
        }
        return true;
    }
}
class Node {
    Node[] children;
    String value;

    public Node(int lengthKeys) {
        this.children = new Node[lengthKeys];
    }
}
