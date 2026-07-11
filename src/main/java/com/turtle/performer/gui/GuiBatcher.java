package com.turtle.performer.gui;
import java.util.*;
public class GuiBatcher{
 private final Map<Integer,List<long[]>> byTexture=new HashMap<>();
 public void initialize(){}
 public void addQuad(int textureId,long packedVerts){
  byTexture.computeIfAbsent(textureId,k->new ArrayList<>()).add(new long[]{packedVerts});
 }
 public Map<Integer,List<long[]>> drainBatches(){
  Map<Integer,List<long[]>> out=byTexture;
  return out;
 }
 public void clear(){byTexture.clear();}
 public int drawCallsSaved(int totalQuads){return Math.max(0,totalQuads-byTexture.size());}
}
