package com.turtle.performer.chunks;
import java.util.concurrent.*;
public class AsyncChunkUploadQueue{
 private final ExecutorService ex=Executors.newSingleThreadExecutor();
 public Future<?> upload(Runnable r){return ex.submit(r);}
}