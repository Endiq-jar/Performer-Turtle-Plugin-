package com.turtle.performer.particles;
import java.util.*;
public class ParticleLimiter{
 private int perTypeLimit=10;
 private final Map<Integer,Integer> countThisTick=new HashMap<>();
 public void initialize(){}
 public boolean tryAllow(int particleType){
  int c=countThisTick.getOrDefault(particleType,0);
  if(c>=perTypeLimit)return false;
  countThisTick.put(particleType,c+1);
  return true;
 }
 public void resetTick(){countThisTick.clear();}
 public void setPerTypeLimit(int limit){perTypeLimit=Math.max(0,limit);}
 public int perTypeLimit(){return perTypeLimit;}
}
