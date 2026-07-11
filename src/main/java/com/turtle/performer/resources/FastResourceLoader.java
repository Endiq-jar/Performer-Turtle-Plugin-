package com.turtle.performer.resources;
import java.util.*;
public class FastResourceLoader{
 private final Map<String,Long> lastModified=new HashMap<>();
 public void initialize(){}
 public boolean needsReload(String path,long currentModifiedTime){
  Long prev=lastModified.get(path);
  return prev==null||prev!=currentModifiedTime;
 }
 public void markLoaded(String path,long modifiedTime){lastModified.put(path,modifiedTime);}
 public void forget(String path){lastModified.remove(path);}
}
