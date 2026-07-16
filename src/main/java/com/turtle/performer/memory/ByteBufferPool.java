package com.turtle.performer.memory;
import java.nio.*;import java.util.concurrent.*;
public class ByteBufferPool{private final ConcurrentLinkedQueue<ByteBuffer> q=new ConcurrentLinkedQueue<>();
public ByteBuffer acquire(int s){ByteBuffer b=q.poll();return b!=null&&b.capacity()>=s?b:ByteBuffer.allocateDirect(s);}
public void release(ByteBuffer b){q.offer(b);}}