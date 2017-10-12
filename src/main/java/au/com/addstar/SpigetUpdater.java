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

import org.inventivetalent.update.spiget.SpigetUpdateAbstract;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created for the Ark: Survival Evolved.
 * Created by Narimm on 12/10/2017.
 */
public class SpigetUpdater extends SpigetUpdateAbstract{

    private DownloadFailReason failReason = DownloadFailReason.UNKNOWN;
    private File updateDir;
    private Properties config;

    public SpigetUpdater(String currentVersion, Logger log, Integer resourceID, Properties config) {
        super(resourceID,currentVersion, log);
        this.config = config;
        updateDir = new File(config.getProperty("downloadLocation"));

        setUserAgent("AddstarResourceUpdater");
    }

    @Override
    public SpigetUpdater setUserAgent(String userAgent) {
        super.setUserAgent(userAgent);
        return this;
    }

    @Override
    protected void dispatch(Runnable runnable) {
    }

    public boolean downloadUpdate(){
        if (latestResourceInfo == null) {
            failReason = DownloadFailReason.NOT_CHECKED;
            return false;// Update not yet checked
        }
        if (!isVersionNewer(currentVersion, latestResourceInfo.latestVersion.name)) {
            failReason = DownloadFailReason.NO_UPDATE;
            return false;// Version is no update
        }
        if (latestResourceInfo.external) {
            failReason = DownloadFailReason.NO_DOWNLOAD;
            return false;// No download available
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
        UNKNOWN;
    }

}
