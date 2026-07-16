package com.turtle.performer.memory;
import java.util.*;
public class LruResourceCache<K,V> extends LinkedHashMap<K,V>{
 public LruResourceCache(){super(16,0.75f,true);}
 protected boolean removeEldestEntry(Map.Entry<K,V>e){return size()>128;}
}