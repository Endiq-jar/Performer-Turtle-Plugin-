package com.turtle.performer.memory;
import java.util.*;
public class ResourceCache{
 private static final long MAX_BYTES=10L*1024*1024;
 private long currentBytes=0;
 private final LinkedHashMap<String,byte[]> map=new LinkedHashMap<String,byte[]>(64,0.75f,true){
  protected boolean removeEldestEntry(Map.Entry<String,byte[]> e){return false;}
 };
 public void initialize(){}
 public synchronized void put(String key,byte[] data){
  byte[] old=map.put(key,data);
  currentBytes+=data.length-(old!=null?old.length:0);
  evictIfNeeded();
 }
 public synchronized byte[] get(String key){return map.get(key);}
 public synchronized void invalidate(String key){
  byte[] old=map.remove(key);
  if(old!=null)currentBytes-=old.length;
 }
 private void evictIfNeeded(){
  Iterator<Map.Entry<String,byte[]>> it=map.entrySet().iterator();
  while(currentBytes>MAX_BYTES&&it.hasNext()){
   currentBytes-=it.next().getValue().length;
   it.remove();
  }
 }
 public synchronized long usedBytes(){return currentBytes;}
 public static long maxBytes(){return MAX_BYTES;}
}
