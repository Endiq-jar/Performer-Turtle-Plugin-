package com.turtle.performer.compatibility;
public class CompatibilityManager{
 private final FabricApiSupport fabricApi=new FabricApiSupport();
 private final ModCompatibilityLayer modLayer=new ModCompatibilityLayer();
 private final ShaderCompatibility shaders=new ShaderCompatibility();
 private final ResourcePackCompatibility resourcePacks=new ResourcePackCompatibility();
 private final ServerCompatibility server=new ServerCompatibility();
 private final ClientOnlyMode clientOnly=new ClientOnlyMode();
 private final MinecraftVersionSupport versionSupport=new MinecraftVersionSupport();
 public void initialize(){
  fabricApi.initialize();
  modLayer.initialize();
  shaders.initialize();
  resourcePacks.initialize();
  server.initialize();
  clientOnly.initialize();
  versionSupport.initialize();
 }
 public FabricApiSupport fabricApi(){return fabricApi;}
 public ModCompatibilityLayer modLayer(){return modLayer;}
 public ShaderCompatibility shaders(){return shaders;}
 public ResourcePackCompatibility resourcePacks(){return resourcePacks;}
 public ServerCompatibility server(){return server;}
 public ClientOnlyMode clientOnly(){return clientOnly;}
 public MinecraftVersionSupport versionSupport(){return versionSupport;}
 public boolean featureAllowed(String featureId){
  if(modLayer.isDisabled(featureId))return false;
  if(shaders.shouldDisable(featureId))return false;
  return true;
 }
}
