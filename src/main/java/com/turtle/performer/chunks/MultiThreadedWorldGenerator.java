package com.turtle.performer.chunks;
import java.util.concurrent.*;
public class MultiThreadedWorldGenerator{
 private ForkJoinPool pool;
 private int parallelism=Math.max(2,Runtime.getRuntime().availableProcessors()-1);
 public void initialize(){
  pool=new ForkJoinPool(parallelism);
 }
 public <T> Future<T> submit(Callable<T> genStageTask){
  if(pool==null)initialize();
  return pool.submit(genStageTask);
 }
 public void invokeAll(Runnable... stages){
  if(pool==null)initialize();
  ForkJoinTask<?>[] tasks=new ForkJoinTask<?>[stages.length];
  for(int i=0;i<stages.length;i++)tasks[i]=pool.submit(stages[i]);
  for(ForkJoinTask<?> t:tasks)t.join();
 }
 public void setParallelism(int count){parallelism=Math.max(1,count);}
 public void shutdown(){if(pool!=null)pool.shutdown();}
}
