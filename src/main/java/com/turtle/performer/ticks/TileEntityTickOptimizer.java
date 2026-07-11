package com.turtle.performer.ticks;
public class TileEntityTickOptimizer{
 private int idleThreshold=200;
 private int distanceCutoff=128;
 public void initialize(){}
 public boolean shouldTick(int idleTicks,double distSqToNearestPlayer){
  if(distSqToNearestPlayer>(long)distanceCutoff*distanceCutoff)return false;
  return idleTicks<idleThreshold||idleTicks%4==0;
 }
 public void setIdleThreshold(int ticks){idleThreshold=Math.max(1,ticks);}
 public void setDistanceCutoff(int blocks){distanceCutoff=Math.max(16,blocks);}
}
