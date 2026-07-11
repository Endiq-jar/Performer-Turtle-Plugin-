package com.turtle.performer.ticks;
import java.util.*;
public class FastBlockUpdater{
 private final ArrayDeque<Long> queue=new ArrayDeque<>();
 private final Set<Long> queued=new HashSet<>();
 private int perTickBudget=1024;
 public void initialize(){}
 public void queueUpdate(long posKey){
  if(queued.add(posKey))queue.add(posKey);
 }
 public List<Long> drainBatch(){
  List<Long> batch=new ArrayList<>(Math.min(perTickBudget,queue.size()));
  for(int i=0;i<perTickBudget&&!queue.isEmpty();i++){
   long k=queue.poll();
   queued.remove(k);
   batch.add(k);
  }
  return batch;
 }
 public void setBudget(int budget){perTickBudget=Math.max(64,budget);}
 public int pending(){return queue.size();}
}
