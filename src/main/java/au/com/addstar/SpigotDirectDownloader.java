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
import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSiteAPI;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;
import be.maximvdw.spigotsite.user.SpigotUser;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;
import org.inventivetalent.update.spiget.ResourceInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created for the Addstar
 * Created by Narimm on 8/11/2017.
 */
public class SpigotDirectDownloader {
    private final WebClient webClient;
    private User spigotUser;
    private final SpigotSiteAPI api;

    public SpigotDirectDownloader(Configuration config) {
        api = new SpigotSiteCore();
        this.webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setTimeout(15000);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        spigotUser = null;
        try {
            spigotUser = api.getUserManager().authenticate(config.username, config.password);
        } catch (TwoFactorAuthenticationException | ConnectionFailedException | InvalidCredentialsException e) {
            e.printStackTrace();
        }
        if(spigotUser != null){
            Map<String, String> cookies = ((SpigotUser) spigotUser).getCookies();
            for (Map.Entry<String, String> entry : cookies.entrySet())
                webClient.getCookieManager().addCookie(new Cookie("spigotmc.org", entry.getKey(), entry.getValue()));
        }
    }

    public boolean downloadUpdate(ResourceInfo info, File file){
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        try {
            Page page = webClient.getPage(api.getResourceManager().getResourceById(info.id, spigotUser).getDownloadURL());
            //HtmlPage htmlPage = (HtmlPage) page;
            webClient.waitForBackgroundJavaScript(10_000);
            BufferedInputStream in = new java.io.BufferedInputStream(page.getEnclosingWindow().getEnclosedPage().getWebResponse().getContentAsStream());
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] data = new byte[1024];
            int x;
            while ((x = in.read(data, 0, 1024)) >= 0) {
                bout.write(data,0,x);
            }
            bout.close();
            in.close();
            fos.close();
            Plugin plugin = SpigotUpdater.checkDownloadedVer(file);
            if(plugin == null || plugin.getVersion() == null){
                file.delete();
                return false;
            }
            return true;
        } catch (IOException | ConnectionFailedException | ClassCastException e) {
            e.printStackTrace();
        }
        return false;
    }

}
