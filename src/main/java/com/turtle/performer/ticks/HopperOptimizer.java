package com.turtle.performer.ticks;
import java.util.*;
public class HopperOptimizer{
 private final Map<Long,Integer> cooldowns=new HashMap<>();
 private int emptyBackoff=8;
 private int fullBackoff=1;
 public void initialize(){}
 public boolean tryTransfer(long posKey,long currentTick){
  Integer next=cooldowns.get(posKey);
  return next==null||currentTick>=next;
 }
 public void recordResult(long posKey,long currentTick,boolean moved,boolean sourceEmpty,boolean destFull){
  int backoff=(!moved&&(sourceEmpty||destFull))?emptyBackoff:fullBackoff;
  cooldowns.put(posKey,(int)(currentTick+backoff));
 }
 public void setEmptyBackoff(int ticks){emptyBackoff=Math.max(1,ticks);}
}
