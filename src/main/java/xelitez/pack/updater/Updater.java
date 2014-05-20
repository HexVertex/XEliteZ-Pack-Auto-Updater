package xelitez.pack.updater;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

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
		version = "1.0.0")
public class Updater 
{
	@Instance(value = "XEZPackUpdater")
	public static Updater instance;
	
	public int scenario = 0;
	
	public static final GuiUpdater gui = new GuiUpdater();
	
	@EventHandler
    public void postload(FMLPostInitializationEvent evt)
    { 
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
    }
	
	public static void startUpdating()
	{
		
	}
	
	public void checkVersion()
	{
		new Thread() 
		{
    		public void run()
			{
				gui.text = "Connecting";
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
					gui.text = "Something went wrong";
					e.printStackTrace();
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
								gui.text = "Something went while reading localVersion";
								e.printStackTrace();
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
					gui.text = "This mod is only to update from MultiMC launcher";
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
		gui.text = "Downloading";
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
			long totalBytesRead = 0L;
			int bytesRead = 0;
			int totalSize = connection.getContentLength();


			while ((bytesRead = reader.read(buffer)) > 0) 
			{  
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
				totalBytesRead += bytesRead;
				gui.text = new StringBuilder().append("Downloading - ").append(totalBytesRead*100/totalSize).append("% complete").toString();
				gui.updatingProgress = (byte)(totalBytesRead * 100 / (totalSize * 2));
			}
			reader.close();
			writer.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		this.processZipFile(targetFile, packDir);
		FMLClientHandler.instance().getClient().shutdown();
		
	}
	
	@SuppressWarnings("rawtypes")
	private void processZipFile(File file, File packDir)
	{
		File extractionFile = new File(file.getParentFile(), "extracted");
    	if(!extractionFile.exists())
    	{
    		extractionFile.mkdir();
    	}
    	ZipFile zipFile;
    	Enumeration entries;
    	try {
    		zipFile = new ZipFile(file.getAbsolutePath());

    		entries = zipFile.entries();

    		while(entries.hasMoreElements()) {
    			ZipEntry entry = (ZipEntry)entries.nextElement();

    			if(entry.isDirectory()) {
    				// Assume directories are stored parents first then children.
    				System.err.println("Extracting directory: " + entry.getName());
    				// This is not robust, just for demonstration purposes.
    				(new File(extractionFile, entry.getName())).mkdir();
    				continue;
    			}

    			System.err.println("Extracting file: " + entry.getName());
    			copyInputStream(zipFile.getInputStream(entry),
    					new BufferedOutputStream(new FileOutputStream(new File(extractionFile, entry.getName()))));
    		}

    		zipFile.close();
    	} catch (IOException ioe) {
    		System.err.println("Unhandled exception:");
    		ioe.printStackTrace();
    		return;
    	}
		if(scenario == 1)
		{
			gui.text = "Copying mods";
			if(packDir.exists())
			{
				try {
					FileUtils.deleteDirectory(packDir);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			extractionFile.listFiles()[0].renameTo(packDir);
		}
		if(scenario == 2)
		{
			try {
				gui.text = "Copying mods";
				gui.updatingProgress = (byte)70;
				File versionFile = new File(packDir, "version.txt");
				File modsFolder = new File(packDir, "minecraft/mods");
				File configFolder = new File(packDir, "minecraft/mods");
				versionFile.delete();
				FileUtils.deleteDirectory(modsFolder);
				FileUtils.deleteDirectory(configFolder);
				new File(extractionFile.listFiles()[0], "version.txt").renameTo(versionFile);
				new File(extractionFile.listFiles()[0], "minecraft/mods").renameTo(modsFolder);
				new File(extractionFile.listFiles()[0], "minecraft/mods").renameTo(configFolder);
				gui.updatingProgress = (byte)90;
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		this.cleanup(file.getParentFile(), file);
	}
	
	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		int len;

		while((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}
	
	private void cleanup(File file, File zip)
	{
		gui.text = "Cleaning Up";
		try {
			FileUtils.deleteDirectory(new File(file, "extracted"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		zip.delete();
		gui.updatingProgress = (byte)100;
	}
	
}
