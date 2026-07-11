package com.turtle.performer.compatibility;
import java.util.*;
public class ModCompatibilityLayer{
 private final Map<String,String> knownConflicts=new HashMap<>();
 private final Set<String> loadedModIds=new HashSet<>();
 private final Set<String> disabledOptimizations=new HashSet<>();
 public void initialize(){}
 public void registerLoadedMod(String modId){loadedModIds.add(modId);}
 public void registerConflict(String modId,String featureToDisable){knownConflicts.put(modId,featureToDisable);}
 public void resolveConflicts(){
  for(Map.Entry<String,String> e:knownConflicts.entrySet()){
   if(loadedModIds.contains(e.getKey()))disabledOptimizations.add(e.getValue());
  }
 }
 public boolean isDisabled(String featureId){return disabledOptimizations.contains(featureId);}
 public boolean isModLoaded(String modId){return loadedModIds.contains(modId);}
}
