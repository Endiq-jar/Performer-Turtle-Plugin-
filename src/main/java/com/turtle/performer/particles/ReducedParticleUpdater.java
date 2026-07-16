package com.turtle.performer.particles;
public class ReducedParticleUpdater{
 private int updateInterval=2;
 public void initialize(){}
 public boolean shouldUpdate(long worldTick,int particleId){
  return ((worldTick+particleId)%updateInterval)==0;
 }
 public void setUpdateInterval(int ticks){updateInterval=Math.max(1,ticks);}
}
