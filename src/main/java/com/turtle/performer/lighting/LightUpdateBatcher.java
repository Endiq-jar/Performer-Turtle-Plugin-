package com.turtle.performer.lighting;
import java.util.*;
public class LightUpdateBatcher{
 private final ArrayDeque<long[]> pending=new ArrayDeque<>();
 private int batchSize=256;
 public void initialize(){}
 public void queue(long posKey,int newLevel){pending.add(new long[]{posKey,newLevel});}
 public List<long[]> nextBatch(){
  List<long[]> batch=new ArrayList<>(Math.min(batchSize,pending.size()));
  for(int i=0;i<batchSize&&!pending.isEmpty();i++)batch.add(pending.poll());
  return batch;
 }
 public void setBatchSize(int size){batchSize=Math.max(16,size);}
 public int pending(){return pending.size();}
}
