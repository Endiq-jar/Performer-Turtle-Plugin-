package com.turtle.performer.ticks;
import java.util.*;
public class ScheduledTickOptimizer{
 private final Map<Long,Integer> pendingByPos=new HashMap<>();
 private int perTickBudget=4096;
 public void initialize(){}
 public void setBudget(int budget){perTickBudget=Math.max(64,budget);}
 public boolean tryQueue(long posKey){
  Integer c=pendingByPos.get(posKey);
  if(c!=null&&c>0)return false;
  pendingByPos.put(posKey,1);
  return true;
 }
 public void released(long posKey){pendingByPos.remove(posKey);}
 public int budget(){return perTickBudget;}
}
