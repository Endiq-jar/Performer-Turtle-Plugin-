package com.turtle.performer.gui;
import java.util.*;
public class InventoryRenderOptimizer{
 private final Map<Integer,Integer> lastSlotHash=new HashMap<>();
 public void initialize(){}
 public boolean slotChanged(int slotIndex,int itemHash){
  Integer prev=lastSlotHash.put(slotIndex,itemHash);
  return prev==null||prev!=itemHash;
 }
 public void invalidateAll(){lastSlotHash.clear();}
}
