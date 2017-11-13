/*
 * MIT License
 *
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package au.com.addstar;
;

import au.com.addstar.objects.InvalidDescriptionException;
import au.com.addstar.objects.Plugin;
import au.com.addstar.objects.PluginDescriptionFile;
import com.sun.prism.shader.DrawPgram_LinearGradient_PAD_AlphaTest_Loader;
import org.apache.commons.lang3.StringUtils;
import org.inventivetalent.update.spiget.UpdateCallback;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.zip.ZipException;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 23/02/2017.
 */
public class SpigotUpdater {

    static File downloadDir;
    static File datFile;
    static List<Plugin> plugins = new ArrayList<>();
    static Boolean externalDownloads;

    public static SpigotDirectDownloader getSpigotDownloader() {
        return spigotDownloader;
    }

    private static SpigotDirectDownloader spigotDownloader;

    public static SimpleDateFormat getFormat() {
        return format;
    }

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) {
        Configuration config = new Configuration();
        final boolean check = (args.length == 1 && args[0].equals("check"));
        if(check)System.out.println(" ONLY CHECKING NO DOWNLOADS WILL BE PERFORMED.");
        downloadDir = config.downloadDir;
        if(!downloadDir.exists()){
            downloadDir.mkdir();
        }
        datFile = new File(downloadDir,"plugins.dat");
        externalDownloads = config.externalDownloads;
        loadPlugins();
        spigotDownloader = new SpigotDirectDownloader(config);
        int i = 0;
        doOutHeader();
        for(final Plugin p: plugins) {
            SpigetUpdater updater = new SpigetUpdater(p.getVersion(), Logger.getAnonymousLogger(), p.getResourceID(), config);
            updater.setExternal(externalDownloads);
            updater.checkForUpdate(getUpdateCallBack(updater,p,check));
            i++;
        }
        /*try {
            savePlugins();
        }catch (IOException e){
            e.printStackTrace();
        }*/
        System.out.println("Processed:  " + i + " plugins");
    }
        private static void loadPlugins() {
            if(datFile.exists()){
                try {
                BufferedReader b = new BufferedReader(new FileReader(datFile));
                String l;
                    while ((l = b.readLine()) != null) {
                        if (!l.startsWith("#")) {
                            String[] lineArray = StringUtils.split(l,",");
                            if(lineArray.length!=0) {
                                String pluginName = lineArray[0];
                                String type = lineArray[1];
                                String source = lineArray[2];
                                if (source.equals("SPIGOT")) {
                                    String resourceID = lineArray[3];
                                    Plugin plugin = new Plugin();
                                    plugin.setName(pluginName);
                                    plugin.setResourceID(Integer.parseInt(resourceID));
                                    setLatestFile(plugin);
                                    setLatestVer(plugin);
                                    if(plugin.getVersion() == null )plugin.setVersion("");
                                    if(plugin.getLastUpdated()== null)plugin.setLastUpdated(new Date(0L));
                                    plugins.add(plugin);
                                }
                            }
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{
                //createNewPluginDat();
            }
        }

    /**
     *
     * @param plugin
     * @return updated Plugin ref.
     */
    public static void setLatestFile(Plugin plugin) {
        File pluginDir = new File(downloadDir, plugin.getName());
        File latest = null;
        if (pluginDir.exists()) {
            File[] files = pluginDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (latest != null) {
                        if (file.lastModified() > latest.lastModified()) {
                            latest = file;
                        }
                    } else {
                        latest = file;
                    }
                }
                if (latest != null)plugin.setLatestFile(latest);
            }
        }
    }
    static Plugin checkDownloadedVer(File file){
        Plugin plugin = new Plugin();
        plugin.setLatestFile(file);
        setLatestVer(plugin);
        return plugin;
    }
    /**
     *
     * @param name
     * @param check
     * @return a new Plugin Instance
     */
    protected static Plugin setLatestFile(String name, File check){
        File latest = check;
        Plugin plugin = new Plugin();
        plugin.setLatestFile(check);
        plugin.setName(name);
        return plugin;
    }

    /**
     *
     * @param plugin
     * @return the same Plugin ref that was a param updated.
     */
    private static Plugin setLatestVer(Plugin plugin){

        try {
            if(plugin.getLatestFile() ==  null) return plugin;
            File file = plugin.getLatestFile();
            JarFile jar = new JarFile(file);
            JarEntry je = jar.getJarEntry("plugin.yml");
            JarEntry sv = jar.getJarEntry("spigot.ver");
            String spigotVer = null;
            if(sv != null){
                InputStream svstream = jar.getInputStream(sv);
                InputStreamReader reader = new InputStreamReader(svstream);
                BufferedReader bs = new BufferedReader(reader);
                spigotVer = bs.readLine();
                bs.close();
                reader.close();
                svstream.close();
            }
            InputStream stream = jar.getInputStream(je);
            PluginDescriptionFile pdf = new PluginDescriptionFile(stream);
            stream.close();
            if(spigotVer != null && !spigotVer.equals(pdf.getVersion())){
                plugin.setVersion(spigotVer);}
                else {
                plugin.setVersion(pdf.getVersion());
            }
            plugin.setLastUpdated(new Date(file.lastModified()));
        } catch (ZipException e){
            //supress
        } catch (IOException | InvalidDescriptionException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return plugin;
    }

    /**
     * Adds a entry to the jar that contains the spigotmc version from the website
     *
     * @param plugin
     */
    static void addSpigotVer(Plugin plugin, String ver){
        if(plugin.getLatestFile() == null) return;
        File file = plugin.getLatestFile();
        try {
            JarFile oldjar = new JarFile(file);
            File newFile = new File(file.getParentFile(),getFormat().format(Calendar.getInstance().getTime()) + "-"+ver+"-s.jar");
            JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(newFile));
            File spigotver = new File(file.getParentFile(),"spigot.ver");
            if(spigotver.exists())spigotver.delete();
            spigotver.createNewFile();
            Writer wr = new FileWriter(spigotver);
            BufferedWriter writer = new BufferedWriter(wr);
            writer.write(ver);
            writer.newLine();
            writer.close();
            wr.close();
            try (FileInputStream stream = new FileInputStream(spigotver)) {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                JarEntry je = new JarEntry(spigotver.getName());
                tempJarOutputStream.putNextEntry(je);
                while ((bytesRead = stream.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
            Enumeration jarEntries = oldjar.entries();
            while(jarEntries.hasMoreElements()) {
                JarEntry entry = (JarEntry) jarEntries.nextElement();
                InputStream entryInputStream = oldjar.getInputStream(entry);
                tempJarOutputStream.putNextEntry(entry);
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
            tempJarOutputStream.close();
            file.delete();
            spigotver.delete();
            newFile.renameTo(file);
        }catch (ZipException e){

        }catch (IOException e){

        }

    }

    private static UpdateCallback getUpdateCallBack(SpigetUpdater updater, Plugin p, boolean check){
        return new UpdateCallback() {
            @Override
            public void updateAvailable(String latestVersion, String url, boolean hasDirectDownload) {
                String name = StringUtils.rightPad(p.getName(),25, " ");
                List<String> out = new ArrayList<>();
                out.add(name);
                out.add(StringUtils.rightPad(p.getResourceID().toString(),13));
                out.add(StringUtils.rightPad(p.getVersion(),10));
                out.add(StringUtils.rightPad(latestVersion,10));
                if ((hasDirectDownload || externalDownloads) && !check){
                    String result;
                    if (updater.downloadUpdate(p)) {
                        result = "DONE";
                    } else {
                        result = "FAIL";
                    }
                    out.add(StringUtils.rightPad(result,10));
                    out.add(StringUtils.rightPad(format.format(p.getLastUpdated()),15));
                    if(result.equals("FAIL"))out.add(" REASON: " + updater.getFailReason() + " - URL: " + url);
                }else{
                    out.add(StringUtils.rightPad("NO",10));
                    out.add(StringUtils.rightPad(format.format(p.getLastUpdated()),15));
                    if(!check) {
                        out.add(" REASON: EXTERNAL -  URL: " + url);
                    }
                }
                StringBuilder sb = new StringBuilder();
                String[] message = new String[out.size()];
                out.toArray(message);
                sb.append(StringUtils.join(message," | "));
                System.out.println(sb.toString());
            }

            @Override
            public void upToDate() {
                String name = StringUtils.rightPad(p.getName(),25, " ");
                List<String> out = new ArrayList<>();
                out.add(name);
                out.add(StringUtils.rightPad(p.getResourceID().toString(),13));
                out.add(StringUtils.rightPad(p.getVersion(),10));
                out.add(StringUtils.rightPad(p.getVersion(),10));
                out.add(StringUtils.rightPad("YES",10));
                out.add(StringUtils.rightPad(format.format(p.getLastUpdated()),15));
                StringBuilder sb = new StringBuilder();
                String[] message = new String[out.size()];
                out.toArray(message);
                sb.append(StringUtils.join(message," | "));
                System.out.println(sb.toString());
            }
    };
    }
    private static void doOutHeader(){
        List<String> out = new ArrayList<>();
        String name = StringUtils.rightPad("Plugin Name",25, " ");
        out.add(name);
        out.add(" Resource ID ");
        out.add(StringUtils.rightPad("Version",10));
        out.add(StringUtils.rightPad("Latest",10));
        out.add(StringUtils.rightPad("Up to Date",10));
        out.add("Date Updated");
        out.add("Extra Notes");
        StringBuilder sb = new StringBuilder();
        String[] message = new String[out.size()];
        out.toArray(message);
        sb.append(StringUtils.join(message," | "));
        System.out.println(sb.toString());
        System.out.println(StringUtils.rightPad("", sb.length(),"-"));
    }

}


