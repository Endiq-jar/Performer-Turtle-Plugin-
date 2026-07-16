package com.turtle.performer.chunks;
import java.util.concurrent.*;
public class ChunkPriorityScheduler{
 private final PriorityBlockingQueue<Runnable> q=new PriorityBlockingQueue<>(11,(a,b)->0);
 public void submit(Runnable r){q.offer(r);}
}