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

import au.com.addstar.objects.Plugin;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.lang3.StringUtils;
import org.inventivetalent.update.spiget.ResourceInfo;
import org.inventivetalent.update.spiget.UpdateCallback;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 23/02/2017.
 */
public class SpigotUpdater {

    static File downloadDir;
    static File datFile;
    static List<Plugin> plugins = new ArrayList<>();
    static Boolean externalDownloads;

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

    public static void main(String[] args) {
        Properties config = Configuration.loadConfig();
        datFile = new File("Spigotplugins.dat");
        final boolean check = (args.length == 1 && args[0].equals("check"));
        String username = config.getProperty("username", "");
        String password = config.getProperty("password", "");
        String downloadLocation = config.getProperty("downloadLocation", ".");
        downloadDir = new File(downloadLocation);
        if(!downloadDir.exists()){
            downloadDir.mkdir();
        }
        Boolean downloadJars = Boolean.parseBoolean(config.getProperty("downloadJars", "false"));
        externalDownloads = Boolean.parseBoolean(config.getProperty("externalJars", "false"));
        loadPlugins();
        int i = 0;
        doOutHeader();
        for(final Plugin p: plugins) {
            SpigetUpdater updater = new SpigetUpdater(p.getVersion(), Logger.getAnonymousLogger(), p.getResourceID(), config);
            updater.setExternal(externalDownloads);
            updater.checkForUpdate(getUpdateCallBack(updater,p,check));
            i++;
        }
        try {
            savePlugins();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Processed:  " + i + " plugins");
    }
        private static void loadPlugins() {
            if(datFile.exists()){
                try {
                BufferedReader b = new BufferedReader(new FileReader(datFile));
                String l;
                    while ((l = b.readLine()) != null) {
                        if (!l.startsWith("#")) {
                            String[] lineArray = StringUtils.split(l,"|");
                            String pluginName = lineArray[0];
                            String resourceID = lineArray[1];
                            String version = lineArray[2];
                            String lastUpdated = null;
                            if (lineArray.length == 4) {
                                lastUpdated = lineArray[3];
                            }
                            Plugin plugin = new Plugin();
                            plugin.setName(pluginName);
                            plugin.setVersion(version);
                            plugin.setResourceID(Integer.parseInt(resourceID));
                            Date date;
                            try {
                                date = format.parse(lastUpdated);
                            } catch (ParseException e) {
                                System.out.print(e.getMessage());
                                date = null;
                            }
                            plugin.setLastUpdated(date);
                            plugins.add(plugin);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{
                createNewPluginDat();
            }
        }
    private static void createNewPluginDat() {
        try {
            datFile.createNewFile();
            Writer writer = new FileWriter(datFile);
            String comment = "# Add new plugins using the following formate.  Comments use #";
            writer.write(comment);
            String layout = "# pluginName|resourceID|version|lastUpdated(yyyy/MM/dd)";
            writer.write(layout);
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private static void savePlugins() throws IOException{
        if(datFile.exists()) {
            datFile.delete();
        }
        createNewPluginDat();
        FileWriter fw = new FileWriter(datFile);
        BufferedWriter writer = new BufferedWriter(fw);
        for(Plugin p: plugins){
            StringBuilder sb = new StringBuilder();
            sb.append(p.getName()).append("|");
            sb.append(p.getResourceID()).append("|");
            sb.append(p.getVersion()).append("|");
            sb.append(format.format(p.getLastUpdated()));
            writer.write(sb.toString());
            writer.newLine();
        }
        writer.close();
    }

    private static UpdateCallback getUpdateCallBack(SpigetUpdater updater, Plugin p, boolean check){
        return new UpdateCallback() {
            @Override
            public void updateAvailable(String s, String s1, boolean hasDirectDownload) {
                ResourceInfo info = updater.getLatestResourceInfo();
                //System.out.println("Updater found version Name: " + info.latestVersion.name);
                //System.out.println("                        id: " + info.latestVersion.id);
                //System.out.println("                        url: " + info.latestVersion.url);
                if ((hasDirectDownload || externalDownloads) && !check){
                    String result;
                    if (updater.downloadUpdate(p)) {
                        result = "DONE";
                        // Update downloaded, will be loaded when the server restarts
                    } else {
                        result = "FAIL";
                        System.out.println("Update download failed, reason is " + updater.getFailReason());
                    }
                    String name = StringUtils.rightPad(p.getName(),30, " ");
                    List<String> out = new ArrayList<>();
                    out.add(name);
                    out.add(StringUtils.rightPad(p.getResourceID().toString(),13));
                    out.add(StringUtils.rightPad(p.getVersion(),10));
                    out.add(StringUtils.rightPad(info.latestVersion.name,10));
                    out.add(StringUtils.rightPad(result,5));
                    out.add(format.format(p.getLastUpdated()));
                    StringBuilder sb = new StringBuilder();
                    String[] message = new String[out.size()];
                    out.toArray(message);
                    sb.append(StringUtils.join(message," | "));
                    System.out.println(sb.toString());
                }else{
                    String name = StringUtils.rightPad(p.getName(),30, " ");
                    List<String> out = new ArrayList<>();
                    out.add(name);
                    out.add(StringUtils.rightPad(p.getResourceID().toString(),13));
                    out.add(StringUtils.rightPad(p.getVersion(),10));
                    out.add(StringUtils.rightPad(info.latestVersion.name,10));
                    out.add(StringUtils.rightPad("YES",5));
                    out.add(format.format(p.getLastUpdated()));
                    StringBuilder sb = new StringBuilder();
                    String[] message = new String[out.size()];
                    out.toArray(message);
                    sb.append(StringUtils.join(message," | "));
                    System.out.println(sb.toString());
                }
            }

            @Override
            public void upToDate() {
                String name = StringUtils.rightPad(p.getName(),30, " ");
                List<String> out = new ArrayList<>();
                out.add(name);
                out.add(StringUtils.rightPad(p.getResourceID().toString(),13));
                out.add(StringUtils.rightPad(p.getVersion(),10));
                out.add(StringUtils.rightPad(p.getVersion(),10));
                out.add(StringUtils.rightPad("NO",5));
                out.add(format.format(p.getLastUpdated()));
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
        String name = StringUtils.rightPad("Plugin Name",30, " ");
        out.add(name);
        out.add(" Resource ID ");
        out.add(StringUtils.rightPad("Version",10));
        out.add(StringUtils.rightPad("Latest",10));
        out.add(StringUtils.rightPad("Update",5));
        out.add("Date Updated");
        StringBuilder sb = new StringBuilder();
        String[] message = new String[out.size()];
        out.toArray(message);
        sb.append(StringUtils.join(message," | "));
        System.out.println(sb.toString());
        System.out.println(StringUtils.rightPad("", sb.length(),"-"));
    }

}


