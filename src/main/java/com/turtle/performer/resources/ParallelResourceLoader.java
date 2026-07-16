package com.turtle.performer.resources;
import java.util.concurrent.*;
import java.util.*;
public class ParallelResourceLoader{
 private ExecutorService pool;
 private int threads=Math.max(2,Runtime.getRuntime().availableProcessors()-1);
 public void initialize(){
  pool=Executors.newFixedThreadPool(threads,r->{
   Thread t=new Thread(r,"turtle-resource-loader");
   t.setDaemon(true);
   return t;
  });
 }
 public <T> List<Future<T>> loadAll(List<Callable<T>> loadTasks){
  if(pool==null)initialize();
  List<Future<T>> out=new ArrayList<>(loadTasks.size());
  for(Callable<T> task:loadTasks)out.add(pool.submit(task));
  return out;
 }
 public void setThreads(int count){threads=Math.max(1,count);}
 public void shutdown(){if(pool!=null)pool.shutdown();}
}
