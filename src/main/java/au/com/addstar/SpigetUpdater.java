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

import au.com.addstar.objects.*;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static au.com.addstar.SpigotUpdater.getFormat;
import static au.com.addstar.SpigotUpdater.getSpigotDownloader;

/**
 * Created for the Addstar
 * Created by Narimm on 12/10/2017.
 */
public class SpigetUpdater extends SpigetUpdateAbstract {

    private final File updateDir;
    private DownloadFailReason failReason = DownloadFailReason.UNKNOWN;
    private ResourceInfo latestResourceInfo;
    private String latestVer;
    private boolean external;


    public SpigetUpdater(String currentVersion, Logger log, Integer resourceID, Configuration c) {
        super(resourceID, currentVersion, log);
        Configuration config = c;
        external = false;
        updateDir = config.downloadDir;
        setUserAgent("AddstarResourceUpdater");
        setVersionComparator(VersionComparator.MAVEN_VER);
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public SpigetUpdater setUserAgent(String userAgent) {
        super.setUserAgent(userAgent);
        return this;
    }

    @Override
    protected void dispatch(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void checkForUpdate(UpdateCallback callback) {
        dispatch(() -> {
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) new URL(String.format(RESOURCE_INFO, resourceId, System.currentTimeMillis())).openConnection();
                connection.setRequestProperty("User-Agent", getUserAgent());
                JsonObject jsonObject = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                latestResourceInfo = new Gson().fromJson(jsonObject, ResourceInfo.class);
                if (!latestResourceInfo.premium) {
                    try {
                        connection = (HttpURLConnection) new URL(String.format(RESOURCE_VERSION, resourceId, System.currentTimeMillis())).openConnection();
                        connection.setRequestProperty("User-Agent", getUserAgent());
                        JsonObject json = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                        latestResourceInfo.latestVersion = new Gson().fromJson(json, ResourceVersion.class);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Failed to get version info from spiget.org", e);
                    }
                    if (isVersionNewer(currentVersion, latestResourceInfo.latestVersion.name)) {
                        callback.updateAvailable(latestResourceInfo.latestVersion.name, "https://spigotmc.org/" + latestResourceInfo.file.url, !latestResourceInfo.external);
                    } else {
                        callback.upToDate();
                    }
                } else {
                    latestVer = Utilities.readURL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
                    if (isVersionNewer(currentVersion, latestVer)) {
                        callback.updateAvailable(latestVer, "https://spigotmc.org/" + latestResourceInfo.file.url, !latestResourceInfo.external);
                    } else {
                        callback.upToDate();
                    }
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to get resource info from spiget.org:" + resourceId);
            }
        });
    }

    public boolean downloadUpdate(Plugin p) {
        if (latestResourceInfo == null) {
            failReason = DownloadFailReason.NOT_CHECKED;
            return false;// Update not yet checked
        }

        if (latestResourceInfo.latestVersion != null) {
            latestVer = latestResourceInfo.latestVersion.name;
        }
        if (latestVer == null) {
            failReason = DownloadFailReason.UNKNOWN;
            return false;// Version is no update
        }
        if (!isVersionNewer(currentVersion, latestVer)) {
            failReason = DownloadFailReason.NO_UPDATE;
            return false;// Version is no update
        }

        if (latestResourceInfo.external && !external) {
            failReason = DownloadFailReason.EXTERNAL_DISALLOWED;
            return false;// No download available
        }
        File updateDirectory = new File(updateDir, p.getName());
        if (!updateDirectory.exists()) updateDirectory.mkdir();
        if (!updateDirectory.exists()) {
            failReason = DownloadFailReason.NO_UPDATE_FOLDER;
            return false;
        }
        File updateFile = new File(updateDirectory, getFormat().format(Calendar.getInstance().getTime()) + "-" + latestVer + ".jar");
        if (latestResourceInfo.premium) {
            try {
                if (getSpigotDownloader().downloadUpdate(latestResourceInfo, updateFile)) {
                    p.setVersion(latestVer);
                    p.addSpigotVer(latestVer);
                    p.setLastUpdated(Calendar.getInstance().getTime());
                } else {
                    failReason = DownloadFailReason.PREMIUM;
                    return false;
                }
            } catch (InvalidDownloadException | ConnectionFailedException e) {
                failReason = DownloadFailReason.PREMIUM;
                return false;
            } catch (RuntimeException e){
                failReason = DownloadFailReason.NO_PLUGIN_FILE;
                return false;
            }

        } else if (latestResourceInfo.external) {
            try {
                if (getSpigotDownloader().downloadUpdate(latestResourceInfo, updateFile)) {
                    p.setVersion(latestVer);
                    p.addSpigotVer(latestVer);
                    p.setLastUpdated(Calendar.getInstance().getTime());
                }
            } catch (InvalidDownloadException | ConnectionFailedException e) {
                System.out.println(e.getMessage());
                failReason = DownloadFailReason.NO_DOWNLOAD;
                return false;
            } catch (RuntimeException e) {
                System.out.println(e.getLocalizedMessage());
                failReason = DownloadFailReason.NO_PLUGIN_FILE;
                return false;
            }
        } else {
            try {
                UpdateDownloader.download(latestResourceInfo, updateFile, userAgent);
                p.setSpigotVersion(latestVer);
                Plugin updated = Plugin.checkDownloadedVer(updateFile);
                if(updated.getPdfVersion().equals(p.getPdfVersion()) && updated.getSpigotVersion() != null && !updated.getSpigotVersion().equals(latestVer)){
                    //it was up to date but the existing file did not have a spigot.ver - we will add it delete the old file.
                        FileUtils.deleteQuietly(p.getLatestFile());
                        p.setLatestFile(updateFile);
                        p.addSpigotVer(latestVer);
                        p.setVersion(latestVer);
                        return true;
                }
                if(p.getPdfVersion()!= null && isVersionNewer(p.getPdfVersion(),updated.getPdfVersion())) {
                    //its newer make sure spigotVer is set.
                    if(updated.getSpigotVersion() == null && !updated.getPdfVersion().equals(updated.getSpigotVersion())) {
                        updated.addSpigotVer(latestVer);
                    }
                    return true;
                }
                if (updated.getVersion().equals(p.getVersion())) {
                        updateFile.delete();
                        if (updateFile.exists()) {
                            System.out.println("Could not remove incorrectly Downloaded File: " + updateFile.getAbsolutePath());
                        }
                        failReason = DownloadFailReason.VERSION_MISMATCH;
                        return false;

                }
                p.setVersion(latestVer);
                p.setLastUpdated(Calendar.getInstance().getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public DownloadFailReason getFailReason() {
        return failReason;
    }


    public enum DownloadFailReason {
        NOT_CHECKED,
        NO_UPDATE,
        NO_DOWNLOAD,
        NO_PLUGIN_FILE,
        NO_UPDATE_FOLDER,
        EXTERNAL_DISALLOWED,
        PREMIUM,
        VERSION_MISMATCH,
        UNKNOWN
    }

}
