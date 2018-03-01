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

package au.com.addstar.objects;

import au.com.addstar.SpigotUpdater;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

/**
 * Created for the Ark: Survival Evolved.
 * Created by Narimm on 7/11/2017.
 */
public class Plugin {
    private String Name;
    private File latestFile;
    private Integer resourceID;
    private String version;
    private String latestVersion;
    private String spigotVersion;
    private String pdfVersion;
    private Date lastUpdated;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String url;

    public PluginType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = PluginType.valueOf(type);
    }

    private PluginType type;

    public static Plugin checkDownloadedVer(File file) {
        Plugin plugin = new Plugin();
        plugin.setLatestFile(file);
        plugin.setLatestVer();
        return plugin;
    }

    public String getPdfVersion() {
        return pdfVersion;
    }

    private void setPdfVersion(String pdfVersion) {
        this.pdfVersion = pdfVersion;
    }

    public String getSpigotVersion() {
        return spigotVersion;
    }

    public void setSpigotVersion(String spigotVersion) {
        this.spigotVersion = spigotVersion;
    }

    public File getLatestFile() {
        return latestFile;
    }

    public void setLatestFile(File latestFile) {
        this.latestFile = latestFile;
    }

    public Integer getResourceID() {
        return resourceID;
    }

    public void setResourceID(Integer resourceID) {
        this.resourceID = resourceID;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Plugin setLatestVer() {

        try {
            if (latestFile == null) return this;
            try(
            JarFile jar = new JarFile(latestFile);
            ) {
                String descriptorFileName = "plugin.yml";
                if(type != null) {
                    switch (type) {
                        case BUKKIT:
                            descriptorFileName = "plugin.yml";
                            break;
                        case BUNGEE:
                            descriptorFileName = "bungee.yml";
                            break;
                        default:
                            descriptorFileName = "plugin.yml";
                    }
                }
                JarEntry je = jar.getJarEntry(descriptorFileName);
                if(je == null)je = jar.getJarEntry("plugin.yml");
                JarEntry sv = jar.getJarEntry("spigot.ver");
                if (sv != null) {
                    try (
                            InputStream svstream = jar.getInputStream(sv);
                            InputStreamReader reader = new InputStreamReader(svstream);
                            BufferedReader bs = new BufferedReader(reader);
                    ) {
                        spigotVersion = bs.readLine();
                        bs.close();
                        reader.close();
                        svstream.close();
                    }
                }else{
                    if(spigotVersion!=null)addSpigotVer(spigotVersion);
                }
                    try (
                            InputStream stream = jar.getInputStream(je);
                    ) {
                        PluginDescriptionFile pdf = new PluginDescriptionFile(stream);
                        pdfVersion = pdf.getVersion();
                    }catch (NullPointerException e){
                        System.out.println("Plugin: "+ this.getName() + " File: " + jar.getName() + " does not appear to be a valid plugin (" + latestFile.getAbsolutePath()+")");
                        pdfVersion = null;
                    }
                }

            if (spigotVersion != null && !spigotVersion.equals(pdfVersion)) {
                version = spigotVersion;
            } else {
                version = pdfVersion;
            }

            lastUpdated = new Date(latestFile.lastModified());
        } catch (ZipException e) {

        } catch (IOException | InvalidDescriptionException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return this;
    }

    /**
     * Adds the Spigot.ver file to the jar
     *
     * @param ver
     */
    public void addSpigotVer(String ver) {
        if (latestFile == null) return;
        File newFile = new File(latestFile.getParentFile(),
                SpigotUpdater.getFormat().format(Calendar.getInstance().getTime()) + "-" + ver + "-s.jar");
        File spigotFile = new File(latestFile.getParentFile(), "spigot.ver");
        if (spigotFile.exists()) FileUtils.deleteQuietly(spigotFile);
        try {
            JarFile oldjar = new JarFile(latestFile);
            if(oldjar.getEntry("spigot.ver") != null)return;
            JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(newFile));
            try(
                    Writer wr = new FileWriter(spigotFile);
                    BufferedWriter writer = new BufferedWriter(wr)
            ) {
                writer.write(ver);
                writer.newLine();
            }
            try (FileInputStream stream = new FileInputStream(spigotFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                JarEntry je = new JarEntry(spigotFile.getName());
                tempJarOutputStream.putNextEntry(je);
                while ((bytesRead = stream.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
                stream.close();
            }
            Enumeration jarEntries = oldjar.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry entry = (JarEntry) jarEntries.nextElement();
                InputStream entryInputStream = oldjar.getInputStream(entry);
                tempJarOutputStream.putNextEntry(entry);
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
                entryInputStream.close();
            }
            tempJarOutputStream.close();
            oldjar.close();
            FileUtils.deleteQuietly(latestFile);
            FileUtils.deleteQuietly(spigotFile);
            FileUtils.moveFile(newFile,latestFile);
            latestFile = newFile;

        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLatestFile() {
        File pluginDir = new File(SpigotUpdater.downloadDir, getName());
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
                if (latest != null) setLatestFile(latest);
            }
        }
    }
    private enum PluginType {
        BUKKIT,
        BUNGEE
    }
}
