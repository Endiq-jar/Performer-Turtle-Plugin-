package com.turtle.performer.chunks;
import java.util.concurrent.*;
public class ChunkTaskScheduler{
 private final ExecutorService ex=Executors.newFixedThreadPool(Math.max(2,Runtime.getRuntime().availableProcessors()/2));
 public Future<?> submit(Runnable r){return ex.submit(r);}
 public void shutdown(){ex.shutdown();}
}