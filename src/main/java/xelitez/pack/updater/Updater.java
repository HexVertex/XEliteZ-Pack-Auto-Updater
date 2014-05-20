package xelitez.pack.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import com.google.common.io.Files;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.Side;

@Mod(
		modid = "XEZPackUpdater",
		name = "XEZPackUpdater",
		version = "1.3.0")
public class Updater 
{
	@Instance(value = "XEZPackUpdater")
	public static Updater instance;
	
	public int scenario = 0;
	
	@EventHandler
    public void postload(FMLPostInitializationEvent evt)
    { 
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
    }
	
	public static void startUpdating()
	{
		
	}
	
	@SuppressWarnings("unused")
	private void checkVersion()
	{
		new Thread() 
		{
			public void Run()
			{
				String remoteVersion = "";
				String localVersion = "";
				String UpdateURL = "";

				try
				{
					List<String> strings = new ArrayList<String>();
					URL url = new URL("https://raw.githubusercontent.com/XEZKalvin/XEliteZ-Pack-Auto-Updater/master/version.txt");
					URLConnection connect = url.openConnection();
					connect.setConnectTimeout(5000);
					connect.setReadTimeout(5000);
					BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
					String str;

					while ((str = in.readLine()) != null)
					{
						strings.add(str);
					}
					remoteVersion = strings.get(0);
					UpdateURL = strings.get(1);

					in.close();
				}
				catch(Exception e)
				{

				}
				File mcDir = (File)FMLInjectionData.data()[6];
				File instancesFolder = mcDir.getParentFile().getParentFile();
				if(instancesFolder.getName().matches("instances"))
				{
					File packDir = new File(instancesFolder, "XEliteZ Modpack");
					if(!packDir.exists())
					{
						scenario = 1;
						downloadPack(UpdateURL, instancesFolder, packDir);
						return;
					}
					else
					{
						File localVersionFile = new File(packDir, "version.txt");
						if(!localVersionFile.exists())
						{
							scenario = 2;
							downloadPack(UpdateURL, instancesFolder, packDir);
							return;
						}
						else
						{
							try {
								localVersion = Files.readFirstLine(localVersionFile, StandardCharsets.UTF_8);
							} 
							catch (IOException e) {

							}
						}
						if(localVersion.isEmpty())
						{
							scenario = 2;
							downloadPack(UpdateURL, instancesFolder, packDir);
							return;
						}
						else if(!localVersion.matches(remoteVersion))
						{
							scenario = 2;
							downloadPack(UpdateURL, instancesFolder, packDir);
							return;
						}		
					}

				}
				else
				{
					GuiUpdater.text = "This mod is only to update from MultiMC launcher";
				}
			}
		}.start();
	}
	
	private void downloadPack(String loc, File file, File packDir)
	{
		if(loc == null || loc.isEmpty() || !file.exists()) return;
		URL url = null;
		try {
			url = new URL(loc);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		GuiUpdater.text = "connecting";
		String strr = url.getFile()
				.substring(url.getFile().lastIndexOf("/"))
				.replaceAll("%5B", "[")
				.replaceAll("%5D", "]")
				.replaceAll("%20", " ");
		if(strr.contains("?"))
		{	
			strr = strr.substring(0, strr.indexOf("?"));
		}
		File targetFile = new File(file, strr);
		try {

			URLConnection connection = url.openConnection();
			InputStream reader = url.openStream();
			FileOutputStream writer = new FileOutputStream(targetFile);
			byte[] buffer = new byte[153600];
			int totalBytesRead = 0;
			int bytesRead = 0;
			int totalSize = connection.getContentLength();


			while ((bytesRead = reader.read(buffer)) > 0) 
			{  
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
				totalBytesRead += bytesRead;
				GuiUpdater.text = new StringBuilder().append("Downloading - ").append(totalBytesRead*100/totalSize).append("% complete").toString();
				GuiUpdater.updatingProgress = (byte)(totalBytesRead * 100 / (totalSize * 2));
				reader.close();
				writer.close();
			}
		}
		catch(Exception e) {
			
		}
		this.processZipFile(targetFile, packDir);
		FMLClientHandler.instance().getClient().shutdown();
		
	}
	
	@SuppressWarnings("resource")
	private void processZipFile(File file, File packDir)
	{
		File extractionFile = new File(file.getParentFile(), "extracted");
		try {
			GuiUpdater.text = "Extracting " + file.getName();
			GuiUpdater.updatingProgress = (byte)60;
			ZipFile zip = new ZipFile(file);
			while(zip.entries().hasMoreElements())
			{
				InputStream reader = zip.getInputStream(zip.entries().nextElement());;
				FileOutputStream writer = new FileOutputStream(extractionFile);
				byte[] buffer = new byte[153600];
				int bytesRead = 0;


				while ((bytesRead = reader.read(buffer)) > 0) 
				{  
					writer.write(buffer, 0, bytesRead);
					buffer = new byte[153600];
					reader.close();
					writer.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(scenario == 1)
		{
			GuiUpdater.text = "Copying mods";
			extractionFile.listFiles()[0].renameTo(packDir);
		}
		if(scenario == 2)
		{
			GuiUpdater.text = "Copying mods";
			GuiUpdater.updatingProgress = (byte)70;
			File versionFile = new File(packDir, "version.txt");
			File modsFolder = new File(packDir, "minecraft/mods");
			File configFolder = new File(packDir, "minecraft/mods");
			versionFile.delete();
			modsFolder.delete();
			configFolder.delete();
			new File(extractionFile.listFiles()[0], "version.txt").renameTo(versionFile);
			new File(extractionFile.listFiles()[0], "minecraft/mods").renameTo(modsFolder);
			new File(extractionFile.listFiles()[0], "minecraft/mods").renameTo(configFolder);
			GuiUpdater.updatingProgress = (byte)90;
		}
		this.cleanup(file.getParentFile(), file);
	}
	
	private void cleanup(File file, File zip)
	{
		GuiUpdater.text = "Cleaning Up";
		new File(file, "extracted").delete();
		file.delete();
		GuiUpdater.updatingProgress = (byte)100;
	}
	
}
