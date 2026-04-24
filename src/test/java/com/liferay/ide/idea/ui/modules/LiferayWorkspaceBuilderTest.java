/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.platform.workspace.storage.SymbolicEntityId;
import com.intellij.platform.workspace.storage.impl.exceptions.SymbolicIdAlreadyExistsException;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Drew Brokke
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class LiferayWorkspaceBuilderTest extends BasePlatformTestCase {

	@Test
	public void testHandleSdkSettingsStepValidationCatchesDuplicateSdkException() throws Exception {
		SymbolicEntityId testId = new SymbolicEntityId() {

			@Override
			public String getPresentableName() {
				return "zulu-21";
			}

			@Override
			public String toString() {
				return "SdkId(name=zulu-21, type=JavaSDK)";
			}

		};

		Assert.assertTrue(
			"Expected validation to return true when SDK already exists",
			_invokeHandleSdkSettingsStepValidation(
				() -> {
					throw new SymbolicIdAlreadyExistsException(testId);
				}));
	}

	@Test
	public void testHandleSdkSettingsStepValidationPassesThroughSuccess() throws Exception {
		Assert.assertTrue(
			"Expected successful validation to pass through", _invokeHandleSdkSettingsStepValidation(() -> true));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_builder = new LiferayGradleWorkspaceBuilder();

		_method = LiferayWorkspaceBuilder.class.getDeclaredMethod(
			"_handleSdkSettingsStepValidation", ThrowableComputable.class);

		_method.setAccessible(true);
	}

	private boolean _invokeHandleSdkSettingsStepValidation(
			ThrowableComputable<Boolean, ConfigurationException> callable)
		throws Exception {

		return (boolean)_method.invoke(_builder, callable);
	}

	private LiferayWorkspaceBuilder _builder;
	private Method _method;

}