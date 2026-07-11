package com.turtle.performer.ticks;
import java.util.*;
public class BlockEventOptimizer{
 private final Map<Long,int[]> lastEvent=new HashMap<>();
 public void initialize(){}
 public boolean shouldDispatch(long posKey,int type,int data){
  int[] prev=lastEvent.get(posKey);
  if(prev!=null&&prev[0]==type&&prev[1]==data)return false;
  lastEvent.put(posKey,new int[]{type,data});
  return true;
 }
 public void clear(long posKey){lastEvent.remove(posKey);}
}
