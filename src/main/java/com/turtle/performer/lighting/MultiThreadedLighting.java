package com.turtle.performer.lighting;
import java.util.concurrent.*;
public class MultiThreadedLighting{
 private ExecutorService pool;
 private int threads=Math.max(2,Runtime.getRuntime().availableProcessors()/2);
 public void initialize(){
  pool=Executors.newFixedThreadPool(threads,r->{
   Thread t=new Thread(r,"turtle-lighting-worker");
   t.setDaemon(true);
   return t;
  });
 }
 public Future<?> submit(Runnable lightUpdate){
  if(pool==null)initialize();
  return pool.submit(lightUpdate);
 }
 public void setThreads(int count){threads=Math.max(1,count);}
 public void shutdown(){
  if(pool!=null)pool.shutdown();
 }
}
