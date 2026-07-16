package com.turtle.performer.resources;
import java.util.*;
public class FontLoadOptimizer{
 private final Set<String> loadedProviders=new HashSet<>();
 private final Set<String> pendingGlyphRanges=new LinkedHashSet<>();
 public void initialize(){}
 public boolean tryMarkLoaded(String providerId){return loadedProviders.add(providerId);}
 public void queueGlyphRange(String rangeKey){pendingGlyphRanges.add(rangeKey);}
 public List<String> drainPendingRanges(){
  List<String> out=new ArrayList<>(pendingGlyphRanges);
  pendingGlyphRanges.clear();
  return out;
 }
 public boolean isLoaded(String providerId){return loadedProviders.contains(providerId);}
}
