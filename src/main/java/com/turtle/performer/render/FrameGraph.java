package com.turtle.performer.render;
import java.util.*;
public class FrameGraph{
 private final List<String> passes=new ArrayList<>();
 public void addPass(String p){passes.add(p);}
 public List<String> getPasses(){return passes;}
}