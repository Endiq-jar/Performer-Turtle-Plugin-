package com.turtle.performer.chunks;
import java.io.*;
import java.util.zip.*;
public class ChunkCompressor{
 private int level=Deflater.BEST_SPEED;
 public void initialize(){}
 public void setLevel(int deflaterLevel){level=Math.max(0,Math.min(9,deflaterLevel));}
 public byte[] compress(byte[] data){
  Deflater deflater=new Deflater(level);
  deflater.setInput(data);
  deflater.finish();
  ByteArrayOutputStream bos=new ByteArrayOutputStream(Math.max(64,data.length/2));
  byte[] buf=new byte[4096];
  while(!deflater.finished()){
   int n=deflater.deflate(buf);
   bos.write(buf,0,n);
  }
  deflater.end();
  return bos.toByteArray();
 }
 public byte[] decompress(byte[] compressed,int expectedSize) throws Exception{
  Inflater inflater=new Inflater();
  inflater.setInput(compressed);
  byte[] out=new byte[expectedSize];
  int n=inflater.inflate(out);
  inflater.end();
  return n==expectedSize?out:java.util.Arrays.copyOf(out,n);
 }
}
