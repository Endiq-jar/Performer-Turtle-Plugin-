package com.turtle.performer.lighting;
public class LightingManager{
 private long updatesThisSecond=0;
 private long lastResetNanos=System.nanoTime();
 public void initialize(){lastResetNanos=System.nanoTime();}
 /**
  * Real signal from LevelLightEngine#runLightUpdates() (confirmed against
  * current RelativityMC/ScalableLux source, ver/26.2.0 branch - a Starlight
  * fork actively maintained for this exact Minecraft version - which hooks
  * this identical real vanilla method via a WrapOperation in
  * ThreadedLevelLightEngineMixin#redirectUpdate). Record-only: this project
  * doesn't replace the light engine itself (that's what Starlight/
  * ScalableLux actually do, at a scope far beyond what's safe to hand-copy
  * here), it just makes real light-update volume visible instead of
  * discarding the signal.
  */
 public synchronized void recordLightUpdatesProcessed(int count){
  updatesThisSecond+=count;
  long now=System.nanoTime();
  if(now-lastResetNanos>=1_000_000_000L){
   updatesThisSecond=count;
   lastResetNanos=now;
  }
 }
 public synchronized long updatesThisSecond(){return updatesThisSecond;}
}
