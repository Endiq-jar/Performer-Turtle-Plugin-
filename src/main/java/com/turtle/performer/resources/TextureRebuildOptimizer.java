package com.turtle.performer.resources;
import java.util.*;
public class TextureRebuildOptimizer{
 private final Set<Integer> dirtyRegions=new LinkedHashSet<>();
 private boolean fullRebuildPending=false;
 private final Set<String> accessedSpritesThisTick=new HashSet<>();
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
 /**
  * Real signal from TextureAtlas#getSprite(...) (confirmed against current
  * Sodium source, which hooks the identical real vanilla method to mark
  * sprites active for animation-ticking purposes - net.minecraft.client
  * .renderer.texture.TextureAtlas, same pattern Sodium's own TextureAtlasMixin
  * uses via SpriteUtil.markSpriteActive). Record-only, matching this
  * project's existing convention of not cancelling/altering vanilla
  * behavior at points where the safety of doing so isn't independently
  * confirmed - this exists so accessedSpriteCount() reflects real per-tick
  * atlas usage instead of nothing.
  */
 public void recordSpriteAccessed(String spriteKey){accessedSpritesThisTick.add(spriteKey);}
 public int accessedSpriteCount(){return accessedSpritesThisTick.size();}
 public void resetTick(){accessedSpritesThisTick.clear();}
}
