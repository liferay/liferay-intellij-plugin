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
package com.liferay.ide.idea.bnd;

import aQute.bnd.osgi.Constants;

/**
 * @author Dominik Marks
 */
public class LiferayBndConstants {

    public static final String PLUGIN_BUNDLE = "-plugin.bundle";
    public static final String PLUGIN_JSP = "-plugin.jsp";
    public static final String PLUGIN_NPM = "-plugin.npm";
    public static final String PLUGIN_RESOURCEBUNDLE = "-plugin.resourcebundle";
    public static final String PLUGIN_SASS = "-plugin.sass";
    public static final String PLUGIN_SERVICE = "-plugin.service";
    public static final String PLUGIN_SPRING = "-plugin.spring";

    public static final String[] CLASS_REFERENCE_PROPERTIES = {
            PLUGIN_BUNDLE, PLUGIN_JSP, PLUGIN_NPM, PLUGIN_RESOURCEBUNDLE,
            PLUGIN_SASS, PLUGIN_SERVICE, PLUGIN_SPRING, Constants.TESTCASES
    };
}
