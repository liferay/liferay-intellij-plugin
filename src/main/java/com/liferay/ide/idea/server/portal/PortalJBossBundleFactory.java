/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.idea.server.portal;

import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.JavaUtil;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Seiphon Wang
 */
public class PortalJBossBundleFactory extends AbstractPortalBundleFactory {
    @Override
    protected boolean detectAppServerPath(Path path) {
        if (FileUtil.notExists(path)) {
            return false;
        }

        Path bundlesPath = FileUtil.pathAppend(path, "bundles");
        Path modulesPath = FileUtil.pathAppend(path, "modules");
        Path standalonePath = FileUtil.pathAppend(path, "standalone");
        Path binPath = FileUtil.pathAppend(path, "bin");

        if (FileUtil.exists(bundlesPath) && FileUtil.exists(modulesPath) && FileUtil.exists(standalonePath) &&
                FileUtil.exists(binPath)) {

            Path mainFolderPath = new File("modules/org/jboss/as/server/main").toPath();

            String mainFolder =mainFolderPath.toString();

            return JavaUtil.scanFolderJarsForManifestProp(path.toFile(), mainFolder, _JBAS7_RELEASE_VERSION, "7.");
        }

        return false;
    }

    @Override
    public PortalBundle create(Path location) {
        return new PortalJBossBundle(location);
    }

    @Override
    public String getType() {
        return "jboss";
    }

    private static final String _JBAS7_RELEASE_VERSION = "JBossAS-Release-Version";

}
