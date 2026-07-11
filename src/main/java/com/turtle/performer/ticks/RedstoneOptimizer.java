package com.turtle.performer.ticks;
import java.util.*;
public class RedstoneOptimizer{
 private final Map<Long,Integer> lastPower=new HashMap<>();
 private final ArrayDeque<Long> queue=new ArrayDeque<>();
 private final Set<Long> queued=new HashSet<>();
 public void initialize(){}
 public void enqueue(long posKey){if(queued.add(posKey))queue.add(posKey);}
 public Long poll(){Long p=queue.poll();if(p!=null)queued.remove(p);return p;}
 public boolean powerChanged(long posKey,int newPower){
  Integer prev=lastPower.put(posKey,newPower);
  return prev==null||prev!=newPower;
 }
 public int pendingCount(){return queue.size();}
}
