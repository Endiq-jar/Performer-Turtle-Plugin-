package com.turtle.performer.ticks;
public class DynamicTickRate{
 private int baseTickMs=50;
 private int currentTickMs=50;
 private int minTickMs=50;
 private int maxTickMs=100;
 public void initialize(){}
 public void update(double serverTps,int loadedEntities){
  if(serverTps<15.0)currentTickMs=Math.min(maxTickMs,currentTickMs+5);
  else if(serverTps>19.5)currentTickMs=Math.max(minTickMs,currentTickMs-2);
 }
 public int currentTickMs(){return currentTickMs;}
 public void setBounds(int min,int max){minTickMs=min;maxTickMs=max;}
 public void reset(){currentTickMs=baseTickMs;}
}
