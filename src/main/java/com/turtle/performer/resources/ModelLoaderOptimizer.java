package com.turtle.performer.resources;
import java.util.*;
public class ModelLoaderOptimizer{
 private final Map<String,Object> bakedModelCache=new HashMap<>();
 public void initialize(){}
 public Object getOrBake(String modelKey,java.util.function.Supplier<Object> bakeFn){
  return bakedModelCache.computeIfAbsent(modelKey,k->bakeFn.get());
 }
 public void invalidate(String modelKey){bakedModelCache.remove(modelKey);}
 public int cachedCount(){return bakedModelCache.size();}
}
