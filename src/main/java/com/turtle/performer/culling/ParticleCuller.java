package com.turtle.performer.culling;
import java.util.*;
public class ParticleCuller{
 private final Map<String,Integer> countThisTick=new HashMap<>();
 private int perTypeLimit=64;
 public void initialize(){}
 public boolean trySpawn(String particleTypeKey){
  int c=countThisTick.getOrDefault(particleTypeKey,0);
  if(c>=perTypeLimit)return false;
  countThisTick.put(particleTypeKey,c+1);
  return true;
 }
 public void resetTick(){countThisTick.clear();}
 public void setPerTypeLimit(int limit){perTypeLimit=Math.max(1,limit);}
}
