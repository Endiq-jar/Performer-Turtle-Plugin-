package com.turtle.performer.lighting;
import java.util.*;
public class BlockLightOptimizer{
 private final ArrayDeque<Long> increaseQueue=new ArrayDeque<>();
 private final ArrayDeque<Long> decreaseQueue=new ArrayDeque<>();
 private final Map<Long,Integer> levelCache=new HashMap<>();
 public void initialize(){}
 public void queueIncrease(long posKey){increaseQueue.add(posKey);}
 public void queueDecrease(long posKey){decreaseQueue.add(posKey);}
 public boolean levelChanged(long posKey,int newLevel){
  Integer prev=levelCache.put(posKey,newLevel);
  return prev==null||prev!=newLevel;
 }
 public int pending(){return increaseQueue.size()+decreaseQueue.size();}
 public Long nextDecrease(){return decreaseQueue.poll();}
 public Long nextIncrease(){return increaseQueue.poll();}
}
