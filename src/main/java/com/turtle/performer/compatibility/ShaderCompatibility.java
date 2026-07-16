package com.turtle.performer.compatibility;
import java.util.*;
public class ShaderCompatibility{
 private boolean shaderPackActive=false;
 private final Set<String> incompatibleOptimizations=new HashSet<>(Arrays.asList(
  "fastChunkRenderer","multiDrawIndirect","gpuInstancing"
 ));
 public void initialize(){}
 public void setShaderPackActive(boolean active){shaderPackActive=active;}
 public boolean shouldDisable(String featureId){
  return shaderPackActive&&incompatibleOptimizations.contains(featureId);
 }
 public boolean isShaderActive(){return shaderPackActive;}
}
