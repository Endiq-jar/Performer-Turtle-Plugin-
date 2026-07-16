package com.turtle.performer.memory;
import java.util.*;
import java.util.function.*;
public class CacheInvalidationManager{
 private final Map<String,Consumer<String>> invalidators=new HashMap<>();
 private final Map<String,Set<String>> dependencyGraph=new HashMap<>();
 public void initialize(){}
 public void register(String cacheName,Consumer<String> invalidateFn){
  invalidators.put(cacheName,invalidateFn);
 }
 public void addDependency(String key,String dependsOnKey){
  dependencyGraph.computeIfAbsent(dependsOnKey,k->new HashSet<>()).add(key);
 }
 public void invalidate(String cacheName,String key){
  Consumer<String> fn=invalidators.get(cacheName);
  if(fn!=null)fn.accept(key);
  Set<String> dependents=dependencyGraph.get(key);
  if(dependents!=null){
   for(String dep:dependents)invalidate(cacheName,dep);
  }
 }
 public void invalidateAll(String cacheName){
  Consumer<String> fn=invalidators.get(cacheName);
  if(fn!=null)fn.accept(null);
 }
}
