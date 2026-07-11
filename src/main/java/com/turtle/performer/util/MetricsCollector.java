package com.turtle.performer.util;
import java.util.concurrent.atomic.*;public class MetricsCollector{public final AtomicLong frames=new AtomicLong();public void frame(){frames.incrementAndGet();}}