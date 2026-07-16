package com.turtle.performer.ticks;
public class DistanceBasedTicking{
 private int nearRadius=32;
 private int midRadius=96;
 private int farRadius=192;
 public void initialize(){}
 public int tickInterval(double distToNearestPlayer){
  if(distToNearestPlayer<=nearRadius)return 1;
  if(distToNearestPlayer<=midRadius)return 2;
  if(distToNearestPlayer<=farRadius)return 4;
  return 8;
 }
 public boolean shouldTick(long worldTick,long entityId,double distToNearestPlayer){
  int interval=tickInterval(distToNearestPlayer);
  return interval<=1||((worldTick+entityId)%interval)==0;
 }
 public void setRadii(int near,int mid,int far){nearRadius=near;midRadius=mid;farRadius=far;}
}
