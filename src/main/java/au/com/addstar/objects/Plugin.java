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

    public String getPdfVersion() {
        return pdfVersion;
    }

    private void setPdfVersion(String pdfVersion) {
        this.pdfVersion = pdfVersion;
    }

    private String pdfVersion;
    private Date lastUpdated;

    public String getSpigotVersion() {
        return spigotVersion;
    }

    public void setSpigotVersion(String spigotVersion) {
        this.spigotVersion = spigotVersion;
    }

    private File getLatestFile() {
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

    public Plugin setLatestVer(){

        try {
            if(this.getLatestFile() ==  null) return this;
            File file = this.getLatestFile();
            JarFile jar = new JarFile(file);
            JarEntry je = jar.getJarEntry("plugin.yml");
            JarEntry sv = jar.getJarEntry("spigot.ver");
            String spigotVer = null;
            if(sv != null){
                InputStream svstream = jar.getInputStream(sv);
                InputStreamReader reader = new InputStreamReader(svstream);
                BufferedReader bs = new BufferedReader(reader);
                spigotVer = bs.readLine();
                setSpigotVersion(spigotVer);
                bs.close();
                reader.close();
                svstream.close();
            }
            InputStream stream = jar.getInputStream(je);
            PluginDescriptionFile pdf = new PluginDescriptionFile(stream);
            stream.close();
            setPdfVersion(pdf.getVersion());
            if(spigotVer != null && !spigotVer.equals(pdf.getVersion())){
                setVersion(spigotVer);}
            else {
                setVersion(pdf.getVersion());
            }
            setLastUpdated(new Date(file.lastModified()));
        } catch (ZipException e){
            //supress
        } catch (IOException | InvalidDescriptionException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return this;
    }

    /**
     * Adds the Spigot.ver file to the jar
     * @param ver
     */
    public void addSpigotVer(String ver){
        setSpigotVersion(ver);
        if(getLatestFile() == null) return;
        File file = getLatestFile();
        try {
            JarFile oldjar = new JarFile(file);
            File newFile = new File(file.getParentFile(), SpigotUpdater.getFormat().format(Calendar.getInstance().getTime()) + "-"+ver+"-s.jar");
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
                if (latest != null)setLatestFile(latest);
            }
        }
    }
}
