package com.turtle.performer.ticks;
import java.util.*;
public class FluidTickOptimizer{
 private final Set<Long> scheduled=new HashSet<>();
 private int interval=1;
 public void initialize(){}
 public boolean trySchedule(long posKey){return scheduled.add(posKey);}
 public void flush(){scheduled.clear();}
 public boolean isSettled(int flowDistance,boolean sourceAdjacent){
  return !sourceAdjacent&&flowDistance>=7;
 }
 public void setInterval(int ticks){interval=Math.max(1,ticks);}
 /**
  * Stateless modulo sampling, same pattern as RandomTickOptimizer - safe to
  * call every tick forever without a periodic reset, unlike trySchedule()
  * above (which would permanently freeze a position if never flush()'d).
  * Default interval=1 means this always returns true (vanilla-equivalent)
  * until setInterval() is called with something higher.
  */
 public boolean shouldTick(long worldTick,long posKey){
  if(interval<=1)return true;
  return ((worldTick+posKey)%interval)==0;
 }
}
