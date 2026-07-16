package com.turtle.performer.ticks;
import java.util.*;
public class PistonOptimizer{
 private final Set<Long> moving=new HashSet<>();
 public void initialize(){}
 public boolean tryStartMove(long posKey){return moving.add(posKey);}
 public void finishMove(long posKey){moving.remove(posKey);}
 public boolean isMoving(long posKey){return moving.contains(posKey);}
 public boolean canSkipRecheck(long posKey,boolean extended,boolean lastExtended){
  return !moving.contains(posKey)&&extended==lastExtended;
 }
 public void clearAll(){moving.clear();}
}
