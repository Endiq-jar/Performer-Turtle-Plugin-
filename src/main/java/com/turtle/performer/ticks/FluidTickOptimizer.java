package com.turtle.performer.ticks;
import java.util.*;
public class FluidTickOptimizer{
 private final Set<Long> scheduled=new HashSet<>();
 public void initialize(){}
 public boolean trySchedule(long posKey){return scheduled.add(posKey);}
 public void flush(){scheduled.clear();}
 public boolean isSettled(int flowDistance,boolean sourceAdjacent){
  return !sourceAdjacent&&flowDistance>=7;
 }
}
