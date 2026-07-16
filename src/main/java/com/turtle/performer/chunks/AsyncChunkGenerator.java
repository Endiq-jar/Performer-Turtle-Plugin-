package com.turtle.performer.chunks;
import java.util.concurrent.*;
public class AsyncChunkGenerator{
 private ExecutorService pool;
 private int threads=Math.max(2,Runtime.getRuntime().availableProcessors()-1);
 public void initialize(){
  pool=Executors.newFixedThreadPool(threads,r->{
   Thread t=new Thread(r,"turtle-chunkgen-worker");
   t.setDaemon(true);
   return t;
  });
 }
 public <T> Future<T> generateAsync(Callable<T> genTask){
  if(pool==null)initialize();
  return pool.submit(genTask);
 }
 public void setThreads(int count){threads=Math.max(1,count);}
 public void shutdown(){if(pool!=null)pool.shutdown();}
}
