package com.turtle.performer.chunks;
import java.util.*;
public class ChunkRebuildPrioritizer{
 private final PriorityQueue<long[]> queue=new PriorityQueue<>((a,b)->Long.compare(a[1],b[1]));
 private final Set<Long> queued=new HashSet<>();
 public void initialize(){}
 public void enqueue(long chunkKey,double distSqToPlayer,boolean visible){
  if(!queued.add(chunkKey))return;
  long score=(long)distSqToPlayer-(visible?1_000_000L:0L);
  queue.add(new long[]{chunkKey,score});
 }
 public Long poll(){
  long[] e=queue.poll();
  if(e==null)return null;
  queued.remove(e[0]);
  return e[0];
 }
 public int pending(){return queue.size();}
}
