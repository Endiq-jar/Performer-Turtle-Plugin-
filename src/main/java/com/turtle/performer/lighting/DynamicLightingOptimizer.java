package com.turtle.performer.lighting;
import java.util.*;
public class DynamicLightingOptimizer{
 private final Map<Long,Integer> lastEmitterLevel=new HashMap<>();
 private int updateIntervalTicks=2;
 public void initialize(){}
 public boolean shouldUpdate(long emitterId,long worldTick,int currentLevel){
  Integer last=lastEmitterLevel.get(emitterId);
  boolean levelChanged=last==null||last!=currentLevel;
  if(!levelChanged&&(worldTick%updateIntervalTicks)!=0)return false;
  lastEmitterLevel.put(emitterId,currentLevel);
  return true;
 }
 public void remove(long emitterId){lastEmitterLevel.remove(emitterId);}
 public void setUpdateInterval(int ticks){updateIntervalTicks=Math.max(1,ticks);}
}
