package com.turtle.performer.ticks;
import java.util.*;
public class ObserverOptimizer{
 private final Map<Long,Long> lastPulse=new HashMap<>();
 private int minPulseInterval=2;
 public void initialize(){}
 public boolean tryPulse(long posKey,long currentTick){
  Long last=lastPulse.get(posKey);
  if(last!=null&&currentTick-last<minPulseInterval)return false;
  lastPulse.put(posKey,currentTick);
  return true;
 }
 public void setMinPulseInterval(int ticks){minPulseInterval=Math.max(1,ticks);}
}
