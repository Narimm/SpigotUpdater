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

import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.resource.ResourceManager;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.UserManager;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 23/02/2017.
 */
public class SpigotUpdater {
    private static Properties config;

    public static void main(String[] args) {
        config = Configuration.loadConfig();

        String username = config.getProperty("username", "");
        String password = config.getProperty("password", "");
        String downloadLocation = config.getProperty("downloadLocation", "..");
        Boolean downloadJars = Boolean.getBoolean(config.getProperty("downloadJars", "false"));
        SpigotSiteCore core = new SpigotSiteCore();
        ResourceManager manager = core.getResourceManager();
        UserManager usermanager = core.getUserManager();
        User user = null;
        try {
            user = usermanager.authenticate(username, password);
        } catch (TwoFactorAuthenticationException | InvalidCredentialsException | ConnectionFailedException e) {
            System.out.print("Could not authenticate with spigot with the provided credentials");
            System.out.print("Will attempt resource download without user");
        }
        for (String arg : args) {
            Resource resource = null;
            try {
                resource = manager.getResourceById(Integer.parseInt(arg), user);
                System.out.println("Checked Resource ID: " + resource.getResourceId());
                System.out.println("Name : " + resource.getResourceName());
                System.out.println("Latest Version: " + resource.getLastVersion());

            } catch (ConnectionFailedException e) {
                e.printStackTrace();
            }
            if(resource==null){
                System.out.println("No Resource found for :" + arg);
                break;
            }
            File saveDir = new File(downloadLocation + "/" + resource.getResourceName() + "/");
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
                if(latestVersion != null) {
                    String[] parts = latestVersion.getName().split("-");
                    lastVersionId = parts[1];
                    System.out.println("Last Downloaded:" + parts[0]);
                    System.out.println("Last Version:" + parts[1]);
                }
            }
            if (lastVersionId != null && lastVersionId.equals(resource.getLastVersion())) {
                System.out.println("No Update Required");
                System.out.println("----------------------------------" );
                break;
            }
            System.out.println("***Update Required***");
            System.out.println("----------------------------------" );
            if (downloadJars) {
                Date date = new Date(System.currentTimeMillis());
                String dateString = new SimpleDateFormat("YYYYMMdd").format(date);
                String filePath = downloadLocation + "/" + resource.getResourceName() + "/" + dateString + '-' + resource.getLastVersion();
                System.out.print("Saving Resource: " + resource.getResourceName() + " Version: " + resource.getLastVersion() + " to " + filePath);
                File downloadFile = null;

                try {
                    downloadFile = File.createTempFile(filePath, ".jar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File out = resource.downloadResource(user, downloadFile);
                Long bytes = out.length();
                double kilobytes = (bytes / 1024);
                System.out.print("File is : " + kilobytes + " kb. Path: " + out.getAbsolutePath());
            }
        }

    }
}
