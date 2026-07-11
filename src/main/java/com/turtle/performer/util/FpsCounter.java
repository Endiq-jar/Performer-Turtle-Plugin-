package com.turtle.performer.util;
public class FpsCounter{
 private long last=System.nanoTime(); private int frames;
 public int frame(){frames++; long n=System.nanoTime(); if(n-last>1_000_000_000L){last=n; int f=frames; frames=0; return f;} return -1;}
}