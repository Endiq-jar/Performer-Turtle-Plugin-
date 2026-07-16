package com.turtle.performer.lighting;
import java.util.*;
public class IncrementalLighting{
 private final Set<Long> dirtySections=new LinkedHashSet<>();
 public void initialize(){}
 public void markDirty(long sectionKey){dirtySections.add(sectionKey);}
 public List<Long> drainDirty(int maxCount){
  List<Long> out=new ArrayList<>(Math.min(maxCount,dirtySections.size()));
  Iterator<Long> it=dirtySections.iterator();
  while(it.hasNext()&&out.size()<maxCount){
   out.add(it.next());
   it.remove();
  }
  return out;
 }
 public int pending(){return dirtySections.size();}
}
