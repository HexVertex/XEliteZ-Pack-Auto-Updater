package xelitez.pack.updater;

import java.awt.Color;

import net.minecraft.client.gui.GuiScreen;

public class GuiUpdater extends GuiScreen
{
	public byte updatingProgress = 0;
	public String text = "";
	
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(fontRenderer, "Updating the XEliteZ Modpack", this.width / 2, 8, 16777215);
        int startWidth = this.width / 6;
        int endWidth = this.width * 5 / 6;
        drawRect(startWidth, 40, endWidth, 52, Color.RED.getRGB());
        int difference = endWidth - startWidth;
        drawRect(startWidth, 40, startWidth + difference * updatingProgress / 100, 52, Color.RED.getRGB());
        this.drawCenteredString(fontRenderer, text, this.width / 2, 42, 16777215);
    }
}
