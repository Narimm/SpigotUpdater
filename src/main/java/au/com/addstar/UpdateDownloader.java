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

/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

import au.com.addstar.objects.ResourceInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


class UpdateDownloader {
    private static final String RESOURCE_DOWNLOAD = "http://api.spiget.org/v2/resources/%s/download";

    public UpdateDownloader() {
    }

    public static Runnable downloadAsync(final ResourceInfo info, final File file, final String userAgent, final DownloadCallback callback) {
        return new Runnable() {
            public void run() {
                try {
                    UpdateDownloader.download(info, file, userAgent);
                    callback.finished();
                } catch (Exception e) {
                    callback.error(e);
                }

            }
        };
    }

    private static void download(ResourceInfo info, File file) {
        download(info, file, "SPIGOTUPDATER-JAVA");
    }

    public static void download(ResourceInfo info, File file, String userAgent) {
        if (info.external) {
            throw new IllegalArgumentException("Cannot download external resource #" + info.id);
        } else {
            ReadableByteChannel channel;
            try {
                HttpURLConnection connection = (HttpURLConnection) (new URL(String.format(RESOURCE_DOWNLOAD, info.id))).openConnection();
                connection.setRequestProperty("User-Agent", userAgent);
                if (connection.getResponseCode() != 200) {
                    throw new RuntimeException("Download returned status #" + connection.getResponseCode());
                }

                channel = Channels.newChannel(connection.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException("Download failed", e);
            }

            try {
                FileOutputStream output = new FileOutputStream(file);
                output.getChannel().transferFrom(channel, 0L, 9223372036854775807L);
                output.flush();
                output.close();
                channel.close();
            } catch (IOException e) {
                throw new RuntimeException("Could not save file", e);
            }
        }
    }
}