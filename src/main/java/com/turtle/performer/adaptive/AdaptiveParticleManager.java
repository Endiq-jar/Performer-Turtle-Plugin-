package com.turtle.performer.adaptive;
public class AdaptiveParticleManager{
 private double scale=1.0;
 private double minScale=0.1;
 private double maxScale=1.0;
 public void initialize(){}
 public void update(double currentFps,double targetFps){
  if(currentFps<targetFps*0.85)scale=Math.max(minScale,scale-0.05);
  else if(currentFps>targetFps*0.98)scale=Math.min(maxScale,scale+0.02);
 }
 public int scaledCount(int baseCount){
  return Math.max(0,(int)Math.round(baseCount*scale));
 }
 public double scale(){return scale;}
 public void setBounds(double min,double max){minScale=min;maxScale=max;}
}
