package com.turtle.performer.chunks;
import java.util.concurrent.PriorityBlockingQueue;
public class ChunkRebuildPriorityQueue{public final PriorityBlockingQueue<Runnable> q=new PriorityBlockingQueue<>(11,(a,b)->0);}