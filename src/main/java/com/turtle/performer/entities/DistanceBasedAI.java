package com.turtle.performer.entities;
public class DistanceBasedAI{
 private int interval=1;
 public void initialize(){}
 public void setInterval(int ticks){interval=Math.max(1,ticks);}
 /**
  * Stateless modulo sampling identical in spirit to
  * ticks.RandomTickOptimizer/ticks.FluidTickOptimizer - safe to call every
  * tick forever. Default interval=1 means vanilla-equivalent (every mob
  * runs its full AI step every tick) until setInterval() is raised. No real
  * distance-to-player signal is wired in here (no confirmed accessor for
  * that), so this throttles uniformly by entity id rather than genuinely
  * "distance-based" - the class name reflects intent, not current scope.
  */
 public boolean shouldRunAiStep(long worldTick,int entityId){
  if(interval<=1)return true;
  return ((worldTick+entityId)%interval)==0;
 }
}
