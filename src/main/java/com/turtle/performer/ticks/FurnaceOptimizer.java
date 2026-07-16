package com.turtle.performer.ticks;
import java.util.*;
public class FurnaceOptimizer{
 private final Set<Long> idle=new HashSet<>();
 public void initialize(){}
 public boolean shouldTick(long posKey,boolean hasFuel,boolean hasInput,boolean isBurning){
  boolean active=isBurning||(hasFuel&&hasInput);
  if(!active){idle.add(posKey);return false;}
  idle.remove(posKey);
  return true;
 }
 public boolean isIdle(long posKey){return idle.contains(posKey);}
}
