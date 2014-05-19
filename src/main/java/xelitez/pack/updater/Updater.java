package xelitez.pack.updater;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(
		modid = "XEZPackUpdater",
		name = "XEZPackUpdater",
		version = "1.3.0")
public class Updater 
{
	@Instance(value = "XEZPackUpdater")
	public static Updater instance;
	
	@EventHandler
    public void postload(FMLPostInitializationEvent evt)
    { 
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
    }
}
