/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.core;

import com.google.gson.annotations.SerializedName;

/**
 * @author Simon Jiang
 */
public class ProductInfo {

	public String getAppServerTomcatVersion() {
		return _appServerTomcatVersion;
	}

	public String getBundleUrl() {
		return _bundleUrl;
	}

	public String getLiferayDockerImage() {
		return _liferayDockerImage;
	}

	public String getLiferayProductVersion() {
		return _liferayProductVersion;
	}

	public String getReleaseDate() {
		return _releaseDate;
	}

	public String getTargetPlatformVersion() {
		return _targetPlatformVersion;
	}

	public boolean isInitialVersion() {
		return _initialVersion;
	}

	@SerializedName("appServerTomcatVersion")
	private String _appServerTomcatVersion;

	@SerializedName("bundleUrl")
	private String _bundleUrl;

	@SerializedName("initialVersion")
	private boolean _initialVersion;

	@SerializedName("liferayDockerImage")
	private String _liferayDockerImage;

	@SerializedName("liferayProductVersion")
	private String _liferayProductVersion;

	@SerializedName("releaseDate")
	private String _releaseDate;

	@SerializedName("targetPlatformVersion")
	private String _targetPlatformVersion;

}