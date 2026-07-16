package com.turtle.performer.particles;
import java.util.concurrent.*;
public class ParticlePool<T>{
 private final ConcurrentLinkedQueue<T> pool=new ConcurrentLinkedQueue<>();
 private int maxPooled=4096;
 public void initialize(){}
 public T acquire(){return pool.poll();}
 public void release(T particle){
  if(particle!=null&&pool.size()<maxPooled)pool.offer(particle);
 }
 public void setMaxPooled(int count){maxPooled=Math.max(64,count);}
 public int pooledCount(){return pool.size();}
}
