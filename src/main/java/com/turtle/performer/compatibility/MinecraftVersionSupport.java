package com.turtle.performer.compatibility;
import java.util.*;
public class MinecraftVersionSupport{
 public enum JreTier{JRE_8,JRE_21,JRE_25}
 private String currentVersion="unknown";
 private static final int[] MIN_SUPPORTED={1,0,0};
 public void initialize(){}
 public void setCurrentVersion(String mcVersion){currentVersion=mcVersion;}
 public String currentVersion(){return currentVersion;}
 public boolean isSupported(String mcVersion){
  int[] v=parse(mcVersion);
  return v!=null&&compare(v,MIN_SUPPORTED)>=0;
 }
 public JreTier jreTierFor(String mcVersion){
  int[] v=parse(mcVersion);
  if(v==null)return JreTier.JRE_21;
  if(v[0]>=26)return JreTier.JRE_25;
  if(compare(v,new int[]{1,20,5})>=0)return JreTier.JRE_21;
  if(compare(v,new int[]{1,16,5})<=0)return JreTier.JRE_8;
  return JreTier.JRE_21;
 }
 public int compareVersions(String a,String b){
  int[] va=parse(a),vb=parse(b);
  if(va==null||vb==null)return 0;
  return compare(va,vb);
 }
 private int[] parse(String version){
  if(version==null)return null;
  String cleaned=version.trim();
  int dash=cleaned.indexOf('-');
  if(dash>0)cleaned=cleaned.substring(0,dash);
  String[] parts=cleaned.split("\\.");
  int[] out=new int[3];
  try{
   for(int i=0;i<3&&i<parts.length;i++)out[i]=Integer.parseInt(parts[i].replaceAll("[^0-9]",""));
  }catch(NumberFormatException e){return null;}
  return out;
 }
 private int compare(int[] a,int[] b){
  for(int i=0;i<3;i++){
   if(a[i]!=b[i])return Integer.compare(a[i],b[i]);
  }
  return 0;
 }
}
