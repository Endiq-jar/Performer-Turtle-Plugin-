package com.turtle.performer.compatibility;
public class ClientOnlyMode{
 private boolean enabled=false;
 public void initialize(){}
 public void setEnabled(boolean clientOnly){enabled=clientOnly;}
 public boolean isEnabled(){return enabled;}
 public boolean shouldLoadServerModules(){return !enabled;}
}
