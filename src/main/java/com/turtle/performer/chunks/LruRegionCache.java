package com.turtle.performer.chunks;
import java.util.*;
public class LruRegionCache{
 private static final long MAX_BYTES=7L*1024*1024;
 private long currentBytes=0;
 private final LinkedHashMap<Long,byte[]> map=new LinkedHashMap<Long,byte[]>(64,0.75f,true){
  protected boolean removeEldestEntry(Map.Entry<Long,byte[]> e){return false;}
 };
 public synchronized void put(long regionKey,byte[] data){
  byte[] old=map.put(regionKey,data);
  currentBytes+=data.length-(old!=null?old.length:0);
  evictIfNeeded();
 }
 public synchronized byte[] get(long regionKey){return map.get(regionKey);}
 public synchronized void invalidate(long regionKey){
  byte[] old=map.remove(regionKey);
  if(old!=null)currentBytes-=old.length;
 }
 private void evictIfNeeded(){
  Iterator<Map.Entry<Long,byte[]>> it=map.entrySet().iterator();
  while(currentBytes>MAX_BYTES&&it.hasNext()){
   currentBytes-=it.next().getValue().length;
   it.remove();
  }
 }
 public synchronized long usedBytes(){return currentBytes;}
 public static long maxBytes(){return MAX_BYTES;}
}
