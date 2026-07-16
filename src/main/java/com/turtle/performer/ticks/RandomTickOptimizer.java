package com.turtle.performer.ticks;
import java.util.concurrent.ThreadLocalRandom;
public class RandomTickOptimizer{
 private int interval=1;
 public void initialize(){}
 public void setInterval(int ticks){interval=Math.max(1,ticks);}
 public boolean shouldTick(long worldTick,int chunkX,int chunkZ){
  if(interval<=1)return true;
  int hash=(chunkX*31+chunkZ)&0x7fffffff;
  return (worldTick+hash)%interval==0;
 }
 public boolean rollChance(int speed,int maxSpeed){
  return ThreadLocalRandom.current().nextInt(maxSpeed)<speed;
 }
}
