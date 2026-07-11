package com.turtle.performer.util;
public class PerformanceProfiler{
 private long t;
 public void begin(){t=System.nanoTime();}
 public long end(){return System.nanoTime()-t;}
}