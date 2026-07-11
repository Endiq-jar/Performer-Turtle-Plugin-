package com.turtle.performer.render;
import java.util.concurrent.ConcurrentHashMap;
public class PipelineCache{private final ConcurrentHashMap<String,Object> c=new ConcurrentHashMap<>();
public Object get(String k){return c.get(k);} public void put(String k,Object v){c.put(k,v);}}