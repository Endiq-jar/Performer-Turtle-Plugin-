package com.turtle.performer.resources;
import java.util.*;
public class TextureRebuildOptimizer{
 private final Set<Integer> dirtyRegions=new LinkedHashSet<>();
 private boolean fullRebuildPending=false;
 public void initialize(){}
 public void markRegionDirty(int spriteId){
  if(!fullRebuildPending)dirtyRegions.add(spriteId);
 }
 public void requestFullRebuild(){
  fullRebuildPending=true;
  dirtyRegions.clear();
 }
 public boolean isFullRebuild(){return fullRebuildPending;}
 public List<Integer> drainDirtyRegions(){
  List<Integer> out=new ArrayList<>(dirtyRegions);
  dirtyRegions.clear();
  fullRebuildPending=false;
  return out;
 }
 public boolean canSkipRebuild(){return !fullRebuildPending&&dirtyRegions.isEmpty();}
}
