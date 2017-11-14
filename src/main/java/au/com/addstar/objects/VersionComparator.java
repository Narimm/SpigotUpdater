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

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 13/11/2017.
 */
public abstract class VersionComparator {

    public static final VersionComparator MAVEN_VER = new VersionComparator() {
        public boolean isNewer(String var1, String var2) {
            DefaultArtifactVersion current = new DefaultArtifactVersion(var1);
            DefaultArtifactVersion check = new DefaultArtifactVersion(var2);
            if (current.getMajorVersion() + current.getMinorVersion() + current.getIncrementalVersion() == 0) {
                //not maven compat fallback
                return !var1.equals(var2);
            }
            int result = check.compareTo(current);
            return result > 0;
        }
    };
    public static final VersionComparator EQUAL = new VersionComparator() {
        @Override
        public boolean isNewer(String currentVersion, String checkVersion) {
            return !currentVersion.equals(checkVersion);
        }
    };

    /**
     * Compares versions by their Sematic Version (<code>Major.Minor.Patch</code>, <a href="http://semver.org/">semver.org</a>). Removes dots and compares the resulting Integer values
     */
    public static final VersionComparator SEM_VER = new VersionComparator() {
        @Override
        public boolean isNewer(String currentVersion, String checkVersion) {
            currentVersion = currentVersion.replace(".", "");
            checkVersion = checkVersion.replace(".", "");

            try {
                int current = Integer.parseInt(currentVersion);
                int check = Integer.parseInt(checkVersion);

                return check > current;
            } catch (NumberFormatException e) {
                System.err.println("[SpigetUpdate] Invalid SemVer versions specified [" + currentVersion + "] [" + checkVersion + "]");
            }
            return false;
        }
    };

    /**
     * Same as {@link VersionComparator#SEM_VER}, but supports version names with '-SNAPSHOT' prefixes
     */
    public static final VersionComparator SEM_VER_SNAPSHOT = new VersionComparator() {
        @Override
        public boolean isNewer(String currentVersion, String checkVersion) {
            currentVersion = currentVersion.replace("-SNAPSHOT", "");
            checkVersion = checkVersion.replace("-SNAPSHOT", "");

            return SEM_VER.isNewer(currentVersion, checkVersion);
        }
    };

    /**
     * Called to check if a version is newer
     *
     * @param currentVersion Current version of the plugin
     * @param checkVersion   Version to check
     * @return <code>true</code> if the checked version is newer
     */
    public abstract boolean isNewer(String currentVersion, String checkVersion);


    public boolean equals(String var1, String var2) {
        return var1.equals(var2);
    }
}
