package com.turtle.performer.culling;
public class VisibilityGraph{
 private final boolean[][] visible=new boolean[6][6];
 public void connect(int a,int b){visible[a][b]=true;}
 public boolean visible(int a,int b){return visible[a][b];}
}