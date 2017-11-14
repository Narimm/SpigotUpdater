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

import java.io.*;
import java.util.Properties;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/12/2016.
 */
class Configuration {
    private final Properties defaultProps;
    private final File config = new File("config.properties");
    String username = "";
    String password = "";
    Boolean externalDownloads = false;
    File downloadDir = null;
    private boolean downloadJars = false;

    public Configuration() {
        InputStream input = Configuration.class.getResourceAsStream("/config.properties");
        defaultProps = new Properties();
        try {
            defaultProps.load(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Properties prop = new Properties();
        try {
            if (config.exists()) {
                InputStream finput = new FileInputStream(config);
                prop.clear();
                prop.load(finput);
                finput.close();
            } else {
                createConfig();
                prop = defaultProps;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setVariables(prop);
    }


    private void createConfig() {
        try {
            if (config.createNewFile()) {
                OutputStream out = new FileOutputStream(config);
                defaultProps.store(out, "Default Configurations");
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        Properties prop = new Properties();
        try {
            File configFile = new File("config.properties");
            if (configFile.exists()) {
                InputStream input = new FileInputStream(configFile);
                prop.clear();
                prop.load(input);
                input.close();
            } else {
                OutputStream out = new FileOutputStream(configFile);
                defaultProps.store(out, "Default Config");
                prop = defaultProps;
                out.close();
            }
            setVariables(prop);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setVariables(Properties prop) {
        username = prop.getProperty("username", "");
        password = prop.getProperty("password", "");
        downloadDir = new File(prop.getProperty("downloadLocation", "."));
        externalDownloads = Boolean.parseBoolean(prop.getProperty("externalJars", "false"));
        downloadJars = Boolean.parseBoolean(prop.getProperty("downloadJars", "false"));

    }


}
