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

import org.inventivetalent.update.spiget.ResourceInfo;
import org.inventivetalent.update.spiget.UpdateCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 23/02/2017.
 */
public class SpigotUpdater {

    File downloadFile;

    public void main(String[] args) {
        Properties config = Configuration.loadConfig();
        String username = config.getProperty("username", "");
        String password = config.getProperty("password", "");
        String downloadLocation = config.getProperty("downloadLocation", "..");
        downloadFile = new File(downloadLocation);
        Boolean downloadJars = Boolean.parseBoolean(config.getProperty("downloadJars", "false"));
        Boolean externalDownloads = Boolean.parseBoolean(config.getProperty("externalJars", "false"));
        SpigetUpdater updater = new SpigetUpdater(args[0], Logger.getAnonymousLogger(), Integer.parseInt(args[1]), config);
            updater.checkForUpdate(new UpdateCallback() {
                @Override
                public void updateAvailable(String s, String s1, boolean hasDirectDownload) {
                    ResourceInfo info = updater.getLatestResourceInfo();
                    System.out.println("Updater found version Name: " + info.latestVersion.name);
                    System.out.println("                        id: " + info.latestVersion.id);
                    System.out.println("                        url: " + info.latestVersion.url);
                    if (hasDirectDownload) {
                        if (updater.downloadUpdate()) {
                            // Update downloaded, will be loaded when the server restarts
                        } else {
                            // Update failed
                            System.out.println("Update download failed, reason is " + updater.getFailReason());
                        }
                    }
                }

                @Override
                public void upToDate() {

                }
            });
            String rPathName = resource.getResourceName().replaceAll("[^\\w\\s]", "").replace(" ", "_");
            String savePath = downloadLocation + "/" + rPathName + "/";
            File saveDir = new File(savePath);
            Long latest = 0L;
            File latestVersion = null;
            File[] files = saveDir.listFiles();
            String lastVersionId = null;
            if (files != null) {
                for (File file : files) {
                    if (latest < file.lastModified()) {
                        latest = file.lastModified();
                        latestVersion = file;
                    }
                }
                if (latestVersion != null) {
                    String[] parts = latestVersion.getName().split("-");
                    lastVersionId = parts[1];
                    System.out.println("Last Downloaded:" + parts[0]);
                    System.out.println("Last Version:" + parts[1]);
                }
            }
            if (lastVersionId != null && lastVersionId.equals(resource.getLastVersion())) {
                System.out.println("No Update Required");
                System.out.println("----------------------------------");
                break;
            }
            System.out.println("***Update Required***");
            System.out.println("----------------------------------");
            if (downloadJars) {
                Date date = new Date(System.currentTimeMillis());
                String dateString = new SimpleDateFormat("YYYYMMdd").format(date);
                String filePath = savePath + dateString + '-' + resource.getLastVersion() + ".jar";
                System.out.println("Saving Resource: " + resource.getResourceName() + " Version: " + resource.getLastVersion() + " to " + filePath);
                File downloadFile = null;
                downloadFile = new File(filePath);
                if (downloadFile.exists()) {
                    downloadFile.delete();
                }
                    File out = resource.downloadResource(user, downloadFile);
                    Long bytes = out.length();
                    double kilobytes = (bytes / 1024);
                    System.out.println("File is : " + kilobytes + " kb. Path: " + out.getAbsolutePath());
                }
            }

        }
    }

