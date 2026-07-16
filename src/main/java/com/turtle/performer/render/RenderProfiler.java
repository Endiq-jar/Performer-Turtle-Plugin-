package com.turtle.performer.render;
public class RenderProfiler{
 private long start;
 private long lastFrameNanos;
 private long frameCount;
 private long terrainResets;
 public void begin(){start=System.nanoTime();}
 public long end(){return System.nanoTime()-start;}
 public void frameEnd(){
  long now=System.nanoTime();
  if(start!=0)lastFrameNanos=now-start;
  frameCount++;
  start=now;
 }
 public void markTerrainDirty(){terrainResets++;}
 public long lastFrameNanos(){return lastFrameNanos;}
 public long frameCount(){return frameCount;}
 public long terrainResets(){return terrainResets;}
}
