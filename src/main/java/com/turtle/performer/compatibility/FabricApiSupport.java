package com.turtle.performer.compatibility;
import java.util.*;
public class FabricApiSupport{
 private final Set<String> detectedApiModules=new HashSet<>();
 private boolean available=false;
 public void initialize(){}
 public void markModulePresent(String moduleId){
  detectedApiModules.add(moduleId);
  available=true;
 }
 public boolean hasModule(String moduleId){return detectedApiModules.contains(moduleId);}
 public boolean isAvailable(){return available;}
}
