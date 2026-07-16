package com.turtle.performer.particles;
import java.util.*;
public class ParticleBatcher{
 private final Map<Integer,List<long[]>> byType=new HashMap<>();
 public void initialize(){}
 public void add(int particleType,long packedPosVel){
  byType.computeIfAbsent(particleType,k->new ArrayList<>()).add(new long[]{packedPosVel});
 }
 public Map<Integer,List<long[]>> drainBatches(){
  Map<Integer,List<long[]>> out=byType;
  return out;
 }
 public void clear(){byType.clear();}
 public int batchCount(){return byType.size();}
}
