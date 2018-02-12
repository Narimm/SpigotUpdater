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

import au.com.addstar.objects.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 23/02/2017.
 */
public class SpigotUpdater {

    private static final List<Plugin> plugins = new ArrayList<>();
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    public static File downloadDir;
    private static File datFile;
    private static Boolean externalDownloads;
    private static SpigotDirectDownloader spigotDownloader;

    public static SpigotDirectDownloader getSpigotDownloader() {
        return spigotDownloader;
    }

    public static SimpleDateFormat getFormat() {
        return format;
    }

    public static void main(String[] args) {
        Configuration config = new Configuration();
        final boolean check = (args.length == 1 && args[0].equals("check"));
        if (check) System.out.println(" ONLY CHECKING NO DOWNLOADS WILL BE PERFORMED.");
        downloadDir = config.downloadDir;
        if (!downloadDir.exists()) {
            downloadDir.mkdir();
        }
        datFile = new File(downloadDir, "plugins.dat");
        externalDownloads = config.externalDownloads;
        loadPlugins();
        spigotDownloader = new SpigotDirectDownloader(config);
        int i = 0;
        doOutHeader();
        for (final Plugin p : plugins) {
            SpigetUpdater updater = new SpigetUpdater(p.getVersion(), Logger.getAnonymousLogger(), p.getResourceID(), config);
            updater.setExternal(externalDownloads);
            updater.checkForUpdate(getUpdateCallBack(updater, p, check));
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
        if (datFile.exists()) {
            try {
                BufferedReader b = new BufferedReader(new FileReader(datFile));
                String l;
                while ((l = b.readLine()) != null) {
                    if (!l.startsWith("#")) {
                        String[] lineArray = StringUtils.splitPreserveAllTokens(l, ",");
                        if (lineArray.length != 0) {
                            String pluginName = lineArray[0];
                            String type = lineArray[1];
                            String source = lineArray[2];
                            String url = lineArray[3];
                            if (source.equals("SPIGOT")) {
                                String resourceID = lineArray[4];
                                Plugin plugin = new Plugin();
                                plugin.setName(pluginName);
                                plugin.setType(type);
                                plugin.setUrl(url);
                                plugin.setResourceID(Integer.parseInt(resourceID));
                                plugin.setLatestFile();
                                plugin.setLatestVer();
                                if (plugin.getVersion() == null) plugin.setVersion("");
                                if (plugin.getLastUpdated() == null) plugin.setLastUpdated(new Date(0L));
                                plugins.add(plugin);
                                plugins.sort(getComparator());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //createNewPluginDat();
        }
    }
    private static Comparator<Plugin> getComparator(){
        return Comparator.comparing(Plugin::getName);
    }

    private static UpdateCallback getUpdateCallBack(SpigetUpdater updater, Plugin p, boolean check) {
        return new UpdateCallback() {
            @Override
            public void updateAvailable(String latestVersion, String url, boolean hasDirectDownload) {
                String name = StringUtils.rightPad(p.getName(), 25, " ");
                List<String> out = new ArrayList<>();
                out.add(name);
                out.add(StringUtils.rightPad(p.getResourceID().toString(), 13));
                out.add(StringUtils.rightPad(p.getVersion(), 10));
                out.add(StringUtils.rightPad(latestVersion, 10));
                if ((hasDirectDownload || externalDownloads) && !check) {
                    String result;
                    if (updater.downloadUpdate(p)) {
                        result = "DONE";
                    } else {
                        result = "FAIL";
                    }
                    out.add(StringUtils.rightPad(result, 10));
                    out.add(StringUtils.rightPad(format.format(p.getLastUpdated()), 15));
                    if (result.equals("FAIL")) out.add(" REASON: " + updater.getFailReason() + " - URL: " + url);
                } else {
                    out.add(StringUtils.rightPad("NO", 10));
                    out.add(StringUtils.rightPad(format.format(p.getLastUpdated()), 15));
                    if (!check) {
                        out.add(" REASON: EXTERNAL -  URL: " + url);
                    }
                }
                StringBuilder sb = new StringBuilder();
                String[] message = new String[out.size()];
                out.toArray(message);
                sb.append(StringUtils.join(message, " | "));
                System.out.println(sb.toString());
            }

            @Override
            public void upToDate() {
                String name = StringUtils.rightPad(p.getName(), 25, " ");
                List<String> out = new ArrayList<>();
                out.add(name);
                out.add(StringUtils.rightPad(p.getResourceID().toString(), 13));
                out.add(StringUtils.rightPad(p.getVersion(), 10));
                out.add(StringUtils.rightPad(p.getVersion(), 10));
                out.add(StringUtils.rightPad("YES", 10));
                out.add(StringUtils.rightPad(format.format(p.getLastUpdated()), 15));
                StringBuilder sb = new StringBuilder();
                String[] message = new String[out.size()];
                out.toArray(message);
                sb.append(StringUtils.join(message, " | "));
                System.out.println(sb.toString());
            }
        };
    }

    private static void doOutHeader() {
        List<String> out = new ArrayList<>();
        String name = StringUtils.rightPad("Plugin Name", 25, " ");
        out.add(name);
        out.add(" Resource ID ");
        out.add(StringUtils.rightPad("Version", 10));
        out.add(StringUtils.rightPad("Latest", 10));
        out.add(StringUtils.rightPad("Up to Date", 10));
        out.add("Date Updated");
        out.add("Extra Notes");
        StringBuilder sb = new StringBuilder();
        String[] message = new String[out.size()];
        out.toArray(message);
        sb.append(StringUtils.join(message, " | "));
        System.out.println(sb.toString());
        System.out.println(StringUtils.rightPad("", sb.length(), "-"));
    }

}


