package com.turtle.performer.lighting;
import java.util.*;
public class SkyLightOptimizer{
 private final Set<Long> dirtyColumns=new HashSet<>();
 public void initialize(){}
 public void markDirty(long columnKey){dirtyColumns.add(columnKey);}
 public boolean isDirty(long columnKey){return dirtyColumns.contains(columnKey);}
 public Iterable<Long> drainDirty(){
  List<Long> out=new ArrayList<>(dirtyColumns);
  dirtyColumns.clear();
  return out;
 }
 public boolean canSkipColumn(boolean hasCeiling,int heightUnchanged){
  return hasCeiling&&heightUnchanged>0;
 }
}
