package com.turtle.performer.chunks;
public class ChunkManager{
 private long accessibleThisSecond=0;
 private long total=0;
 private long lastResetNanos=System.nanoTime();
 public void tick(){}
 public void initialize(){lastResetNanos=System.nanoTime();}
 /**
  * Real signal from LevelChunk#setFullStatus(Supplier) (confirmed against
  * current CaffeineMC/lithium source, mixin.util.chunk_status_tracking
  * .LevelChunkMixin, which hooks this identical real vanilla method to
  * detect when a chunk becomes fully accessible). Record-only: this
  * project doesn't reschedule chunk loading/generation the way Lithium's
  * ChunkStatusTracker or C2ME's concurrent chunk engine do (that needs
  * threading-model changes far beyond a telemetry hook), it just makes
  * real chunk-accessibility volume visible instead of discarding it.
  */
 public synchronized void recordChunkAccessible(){
  total++;
  accessibleThisSecond++;
  long now=System.nanoTime();
  if(now-lastResetNanos>=1_000_000_000L){
   accessibleThisSecond=0;
   lastResetNanos=now;
  }
 }
 public synchronized long accessibleThisSecond(){return accessibleThisSecond;}
 public synchronized long totalAccessible(){return total;}
}