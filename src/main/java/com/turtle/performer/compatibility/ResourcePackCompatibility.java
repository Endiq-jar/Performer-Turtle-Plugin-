package com.turtle.performer.compatibility;
import java.util.*;
public class ResourcePackCompatibility{
 private final Set<String> activePacks=new LinkedHashSet<>();
 private boolean customModelsPresent=false;
 public void initialize(){}
 public void registerActivePack(String packId,boolean hasCustomModels){
  activePacks.add(packId);
  if(hasCustomModels)customModelsPresent=true;
 }
 public void clearPacks(){activePacks.clear();customModelsPresent=false;}
 public boolean shouldBypassAtlasOptimization(){return customModelsPresent;}
 public Set<String> activePacks(){return activePacks;}
}
