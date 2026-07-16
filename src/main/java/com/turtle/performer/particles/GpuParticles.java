package com.turtle.performer.particles;
public class GpuParticles{
 private boolean supported=false;
 private boolean enabled=false;
 private int maxGpuParticles=65536;
 public void initialize(){}
 public void setSupported(boolean computeShadersAvailable){supported=computeShadersAvailable;}
 public boolean tryEnable(){enabled=supported;return enabled;}
 public boolean isEnabled(){return enabled;}
 public int capacity(){return maxGpuParticles;}
 public void setCapacity(int count){maxGpuParticles=Math.max(1024,count);}
}
