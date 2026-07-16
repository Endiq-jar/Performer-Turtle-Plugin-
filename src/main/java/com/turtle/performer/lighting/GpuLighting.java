package com.turtle.performer.lighting;
public class GpuLighting{
 private boolean supported=false;
 private boolean enabled=false;
 public void initialize(){}
 public void setSupported(boolean gpuComputeAvailable){supported=gpuComputeAvailable;}
 public boolean tryEnable(){
  enabled=supported;
  return enabled;
 }
 public boolean isEnabled(){return enabled;}
 public void disable(){enabled=false;}
}
