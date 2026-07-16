package com.turtle.performer.ticks;
import java.util.*;
public class ComparatorOptimizer{
 private final Map<Long,Integer> cachedOutput=new HashMap<>();
 public void initialize(){}
 public boolean outputChanged(long posKey,int computed){
  Integer prev=cachedOutput.put(posKey,computed);
  return prev==null||prev!=computed;
 }
 public int cached(long posKey){return cachedOutput.getOrDefault(posKey,0);}
}
