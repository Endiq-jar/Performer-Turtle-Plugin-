package com.turtle.performer.chunks;
import java.io.*;
import java.util.zip.*;
public class ChunkSerializer{
 public void initialize(){}
 public byte[] serialize(byte[] rawChunkData) throws IOException{
  ByteArrayOutputStream bos=new ByteArrayOutputStream(rawChunkData.length/2+64);
  try(DeflaterOutputStream dos=new DeflaterOutputStream(bos,new Deflater(Deflater.BEST_SPEED))){
   dos.write(rawChunkData);
  }
  return bos.toByteArray();
 }
 public byte[] deserialize(byte[] compressed) throws IOException{
  ByteArrayOutputStream bos=new ByteArrayOutputStream(compressed.length*2+64);
  try(InflaterOutputStream ios=new InflaterOutputStream(bos)){
   ios.write(compressed);
  }
  return bos.toByteArray();
 }
}
