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

package com.liferay.ide.idea.util;

import com.liferay.ide.idea.server.portal.PortalBundle;
import com.liferay.ide.idea.server.portal.PortalBundleFactory;
import com.liferay.ide.idea.server.portal.PortalTomcatBundleFactory;
import com.liferay.ide.idea.server.portal.PortalWildFlyBundleFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Seiphon Wang
 */
public class ServerUtil {

	public static String getGogoShellPort(String bundleLocation) {
		String gogoShellPortValue = "11311";

		PortalBundle portalBundle = getPortalBundle(FileUtil.getPath(bundleLocation));

		if (portalBundle != null) {
			File[] extPropertiesFiles = _getPortalExtraPropertiesFiles(portalBundle, "portal-ext.properties");
			File[] developerPropertiesFiles = _getPortalExtraPropertiesFiles(
				portalBundle, "portal-developer.properties");
			File[] setupWizardPropertiesFiles = _getPortalExtraPropertiesFiles(
				portalBundle, "portal-setup-wizard.properties");

			Properties portalExtraProperties = new Properties();

			_loadProperties(portalExtraProperties, developerPropertiesFiles);
			_loadProperties(portalExtraProperties, extPropertiesFiles);
			_loadProperties(portalExtraProperties, setupWizardPropertiesFiles);

			String gogoShellConnectString = portalExtraProperties.getProperty(
				"module.framework.properties.osgi.console");

			if (Objects.nonNull(gogoShellConnectString)) {
				String[] gogoShellConnectStrings = gogoShellConnectString.split(":");

				if (Objects.nonNull(gogoShellConnectStrings) && (gogoShellConnectStrings.length > 1)) {
					gogoShellPortValue = gogoShellConnectStrings[1];
				}
			}
		}

		return gogoShellPortValue;
	}

	public static File[] getMarketplaceLpkgFiles(File runtime) {
		File marketplace = new File(new File(runtime, "osgi"), "marketplace");

		return marketplace.listFiles((dir, name) -> name.matches(".*\\.lpkg"));
	}

	public static File getModuleFileFrom70Server(File runtime, String hostOsgiBundle, File temp) {
		File moduleOsgiBundle = null;

		for (String dir : _osgiBundleDirs) {
			moduleOsgiBundle = new File(new File(new File(runtime, "osgi"), dir), hostOsgiBundle);

			if (moduleOsgiBundle.exists()) {
				FileUtil.copyFile(moduleOsgiBundle, new File(temp, hostOsgiBundle));

				return moduleOsgiBundle;
			}
		}

		File f = new File(temp, hostOsgiBundle);

		if (f.exists()) {
			return f;
		}

		File[] files = getMarketplaceLpkgFiles(runtime);

		InputStream in = null;

		try {
			boolean found = false;

			for (File file : files) {
				try (JarFile jar = new JarFile(file)) {
					Enumeration<JarEntry> enu = jar.entries();

					while (enu.hasMoreElements()) {
						JarEntry entry = enu.nextElement();

						String name = entry.getName();

						if (name.contains(hostOsgiBundle)) {
							in = jar.getInputStream(entry);
							found = true;

							FileUtil.writeFile(f, in);

							break;
						}
					}

					if (found) {
						break;
					}
				}
			}
		}
		catch (Exception e) {
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException ioe) {
				}
			}
		}

		return f;
	}

	public static List<String> getModuleFileListFrom70Server(File runtime) {
		List<String> bundles = new ArrayList<>();

		try {
			for (String dir : _osgiBundleDirs) {
				File dirFile = new File(new File(runtime, "osgi"), dir);

				if (dirFile.exists()) {
					File[] files = dirFile.listFiles(
						new FilenameFilter() {

							@Override
							public boolean accept(File dir, String name) {
								return name.matches(".*\\.jar");
							}

						});

					if ((files != null) && (files.length > 0)) {
						for (File file : files) {
							bundles.add(file.getName());
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		File[] files = getMarketplaceLpkgFiles(runtime);

		for (File file : files) {
			try (JarFile jar = new JarFile(file)) {
				Enumeration<JarEntry> enu = jar.entries();

				while (enu.hasMoreElements()) {
					JarEntry entry = enu.nextElement();

					String name = entry.getName();

					if (name.endsWith(".jar")) {
						bundles.add(name);
					}
				}
			}
			catch (IOException ioe) {
			}
		}

		return bundles;
	}

	public static PortalBundle getPortalBundle(Path bundlePath) {
		for (PortalBundleFactory portalBundleFactory : _bundleFactories) {
			Path appServerPath = portalBundleFactory.findAppServerPath(bundlePath);

			if (appServerPath != null) {
				return portalBundleFactory.create(appServerPath);
			}
		}

		return null;
	}

	public static PortalBundleFactory getPortalBundleFactory(String bundleType) {
		for (PortalBundleFactory portalBundleFactory : _bundleFactories) {
			if (bundleType.equals(portalBundleFactory.getType())) {
				return portalBundleFactory;
			}
		}

		return null;
	}

	public static boolean verifyPath(String verifyPath) {
		if (verifyPath == null) {
			return false;
		}

		Path verifyLocation = FileUtil.getPath(verifyPath);

		File verifyFile = verifyLocation.toFile();

		if (FileUtil.exists(verifyFile) && verifyFile.isDirectory()) {
			return true;
		}

		return false;
	}

	private static File[] _getPortalExtraPropertiesFiles(PortalBundle portalBundle, String propertyFileName) {
		File[] retVal = new File[0];

		Path liferayHomePath = portalBundle.getLiferayHome();

		File liferayHomeDir = liferayHomePath.toFile();

		if (liferayHomeDir.exists()) {
			File[] files = liferayHomeDir.listFiles(
				(dir, name) -> dir.equals(liferayHomeDir) && Objects.equals(name, propertyFileName));

			if (files != null) {
				retVal = files;
			}
		}

		return retVal;
	}

	private static void _loadProperties(Properties poralExtraPropertiesFiles, File[] propertyFiles) {
		if (ListUtil.isNotEmpty(propertyFiles)) {
			try (InputStream stream = Files.newInputStream(propertyFiles[0].toPath())) {
				poralExtraPropertiesFiles.load(stream);
			}
			catch (IOException ioe) {
			}
		}
	}

	private static PortalBundleFactory[] _bundleFactories = {
		new PortalTomcatBundleFactory(), new PortalWildFlyBundleFactory()
	};
	private static String[] _osgiBundleDirs = {"core", "modules", "portal", "static"};

}