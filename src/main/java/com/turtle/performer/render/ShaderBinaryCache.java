package com.turtle.performer.render;
import java.util.concurrent.ConcurrentHashMap;
public class ShaderBinaryCache{private final ConcurrentHashMap<String,byte[]> cache=new ConcurrentHashMap<>();
public void put(String k,byte[] b){cache.put(k,b);} public byte[] get(String k){return cache.get(k);}}