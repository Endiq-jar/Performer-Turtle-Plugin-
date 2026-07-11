package com.turtle.performer.lighting;
import java.util.*;
public class SectionLightCache{
 private static final long MAX_BYTES=1L*1024*1024;
 private long currentBytes=0;
 private final LinkedHashMap<Long,byte[]> map=new LinkedHashMap<Long,byte[]>(64,0.75f,true){
  protected boolean removeEldestEntry(Map.Entry<Long,byte[]> e){return false;}
 };
 public void initialize(){}
 public synchronized void put(long sectionKey,byte[] nibbleArray){
  byte[] old=map.put(sectionKey,nibbleArray);
  currentBytes+=nibbleArray.length-(old!=null?old.length:0);
  evictIfNeeded();
 }
 public synchronized byte[] get(long sectionKey){return map.get(sectionKey);}
 public synchronized void invalidate(long sectionKey){
  byte[] old=map.remove(sectionKey);
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
