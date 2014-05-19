package xelitez.pack.updater;

import java.util.EnumSet;

import net.minecraft.client.gui.GuiMainMenu;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler{

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if(FMLClientHandler.instance().getClient().currentScreen instanceof GuiMainMenu)
		{
			FMLClientHandler.instance().getClient().displayGuiScreen(new GuiUpdater());
		}
		
	}

	@Override
	public EnumSet<TickType> ticks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

}
