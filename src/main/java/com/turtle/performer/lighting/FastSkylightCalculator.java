package com.turtle.performer.lighting;
public class FastSkylightCalculator{
 public void initialize(){}
 public int[] computeHeightmap(int[] topOpaqueYPerColumn){
  return topOpaqueYPerColumn;
 }
 public int fastColumnLight(int topOpaqueY,int y,int worldMaxLight){
  return y>=topOpaqueY?worldMaxLight:0;
 }
 public boolean columnUnchanged(int prevTopY,int newTopY){return prevTopY==newTopY;}
}
