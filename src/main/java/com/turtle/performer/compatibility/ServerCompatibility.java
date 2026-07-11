package com.turtle.performer.compatibility;
public class ServerCompatibility{
 private boolean dedicatedServer=false;
 private boolean vanillaServer=true;
 public void initialize(){}
 public void setEnvironment(boolean isDedicatedServer,boolean isVanilla){
  dedicatedServer=isDedicatedServer;
  vanillaServer=isVanilla;
 }
 public boolean isDedicatedServer(){return dedicatedServer;}
 public boolean allowClientOnlyOptimizations(){return !dedicatedServer;}
 public boolean allowServerTickOptimizations(){return vanillaServer||dedicatedServer;}
}
