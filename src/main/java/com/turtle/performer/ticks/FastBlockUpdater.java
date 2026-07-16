package com.turtle.performer.ticks;
import com.turtle.performer.memory.ObjectPool;
import java.util.*;
public class FastBlockUpdater{
 private final ArrayDeque<Long> queue=new ArrayDeque<>();
 private final Set<Long> queued=new HashSet<>();
 private int perTickBudget=1024;
 // Previously memory/ObjectPool existed unused anywhere in the mod;
 // drainBatch() allocated (and callers discarded) a brand new ArrayList
 // every call. Reusing pooled lists avoids that per-call allocation on
 // what is now a real, regularly-invoked path (DynamicTickRateMixin
 // calls this periodically).
 private final ObjectPool<ArrayList<Long>> batchPool=new ObjectPool<>();
 public void initialize(){}
 public void queueUpdate(long posKey){
  if(queued.add(posKey))queue.add(posKey);
 }
 public List<Long> drainBatch(){
  ArrayList<Long> batch=batchPool.acquire();
  if(batch==null)batch=new ArrayList<>(perTickBudget);
  else batch.clear();
  for(int i=0;i<perTickBudget&&!queue.isEmpty();i++){
   long k=queue.poll();
   queued.remove(k);
   batch.add(k);
  }
  return batch;
 }
 /** Callers done with a list from drainBatch() should return it here so the next drainBatch() can reuse it. */
 public void releaseBatch(List<Long> batch){
  if(batch instanceof ArrayList<Long> al)batchPool.release(al);
 }
 public void setBudget(int budget){perTickBudget=Math.max(64,budget);}
 public int pending(){return queue.size();}
}
