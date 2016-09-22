package com.bluetoothtest1.DataTools;

/**
 * Created by Administrator on 2016/9/19.
 */
public class DataLink {
    private class Node{
        private Object data ;
        private Node next ;
        public Node(Object data){
            this.data = data ;
        }

        public void addNode(Node newNode){
            if(this.next == null){
                this.next = newNode ;
            }else this.next.addNode(newNode);
        }


        public boolean containsNode(Object data){
            if(data.equals(this.data)){
                return true ;
            }else{
                if(this.next != null){
                    return this.next.containsNode(data);
                }else return false ;
            }
        }
        public void removeNode(Node previous ,Object data){
            if(this.data.equals(data)){
                previous.next = this.next ;
            }else
                this.next.removeNode(this,data);
        }
        public void toArrayNode(){
            DataLink.this.retData[DataLink.this.foot ++] = this.data ;
            if(this.next != null){
                this.next.toArrayNode();
            }
        }
        public Object getNode(int index){
            if(DataLink.this.foot ++ == index ){
                return this.data;
            }
            else
                return this.next.getNode(index) ;
        }


        public void printNode(){
            System.out.print(this.data + "	");
            if(this.next != null){
                this.next.printNode() ;
            }
        }
    }
    private Node root ;
    private int count ;
    private int foot = 0 ;
    private Object [] retData ;
    private boolean changFlag = true ;
    public void add(Object data){
        if(data == null) return ;
        Node newNode = new Node(data) ;
        if(this.root == null){
            this.root = newNode ;
        }else this.root.addNode(newNode);
        this.count ++;
        this.changFlag = true ;
    }

    public void addAll(Object data[]){
        for(int x = 0 ;x < data.length ;x ++){
            this.add(data[x]);
        }
    }
    public int size(){
        return this.count ;
    }
    public boolean isObjectty(){
        return this.count == 0 ;
    }
    public boolean contains(Object data){
        if(this.root == null || data == null){
            return false ;
        }
        return this.root.containsNode(data);
    }
    public void remove(Object data){
        if(!this.contains(data)){
            return ;
        }
        if(data.equals(this.root.data)){
            this.root = this.root.next ;
        }else{
            this.root.next.removeNode(this.root,data);
        }
        this.count --;
        this.changFlag = true ;
    }
    public Object[] toArray(){
        if(this.count == 0){
            return null ;
        }
        this.foot = 0 ;
        if(this.changFlag == true){
            this.retData = new Object [this.count] ;
            this.root.toArrayNode();

        }
        return this.retData ;
    }



    public Object get(int index){
        if(index > this.count){
            return null ;
        }
        this.foot = 0 ;
        return this.root.getNode(index);
    }

    public void clear(){
        this.root = null ;
        this.count = 0 ;
    }

    public void print(){
        if(this.root != null){
            this.root.printNode();
        }
    }
}
