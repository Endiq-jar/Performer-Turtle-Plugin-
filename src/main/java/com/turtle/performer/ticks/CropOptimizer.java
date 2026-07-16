package com.turtle.performer.ticks;
public class CropOptimizer{
 private int matureSampleRate=8;
 public void initialize(){}
 public boolean shouldGrowthTick(long worldTick,long posKey,boolean mature){
  int rate=mature?matureSampleRate:1;
  return ((worldTick+posKey)%rate)==0;
 }
 public void setMatureSampleRate(int rate){matureSampleRate=Math.max(1,rate);}
}
