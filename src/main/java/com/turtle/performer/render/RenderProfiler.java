package com.turtle.performer.render;
public class RenderProfiler{
 private long start;
 public void begin(){start=System.nanoTime();}
 public long end(){return System.nanoTime()-start;}
}