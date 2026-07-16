package com.turtle.performer.culling;
public class FrustumCuller{
 private long visibleCount=0;
 private long culledCount=0;
 public boolean visible(){return true;}
 public void recordResult(boolean isVisible){
  if(isVisible)visibleCount++; else culledCount++;
 }
 public long visibleCount(){return visibleCount;}
 public long culledCount(){return culledCount;}
}
