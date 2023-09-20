/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager;
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.externalSystem.service.notification.NotificationSource;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VfsUtil;

import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.ListUtil;

import java.awt.GridLayout;

import java.io.File;
import java.io.FileNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Ethan Sun
 */
public class LiferayWorkspaceProductDialog extends DialogWrapper {

	protected LiferayWorkspaceProductDialog(@Nullable Project project) {
		super(true);

		_project = project;

		init();

		setTitle("Configure Product For Liferay Workspace");
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		JPanel dialogPanel = new JPanel(new GridLayout(2, 2));

		JLabel productVersionLabel = new JLabel("Product Version:");

		_productVersionComboBox = new ComboBox<>();

		JLabel showAllProductVersionLabel = new JLabel("Show All Product Version:");

		JCheckBox showAllProductVersionCheckBox = new JCheckBox();

		showAllProductVersionCheckBox.setSelected(false);

		showAllProductVersionCheckBox.addActionListener(
			e -> {
				boolean showAll = showAllProductVersionCheckBox.isSelected();

				rendererProductVersion(showAll);
			});

		dialogPanel.add(productVersionLabel);

		dialogPanel.add(_productVersionComboBox);

		dialogPanel.add(showAllProductVersionLabel);

		dialogPanel.add(showAllProductVersionCheckBox);

		rendererProductVersion(false);

		return dialogPanel;
	}

	@Override
	protected void doOKAction() {
		Application application = ApplicationManager.getApplication();

		application.invokeAndWait(
			() -> {
				try {
					if (Objects.nonNull(_project)) {
						Path projectPath = Paths.get(Objects.requireNonNull(_project.getBasePath()));

						Path gradlePropertiesPath = projectPath.resolve("gradle.properties");

						File propertyFile = gradlePropertiesPath.toFile();

						if (FileUtil.notExists(propertyFile)) {
							throw new FileNotFoundException();
						}

						final String productKey = (String)_productVersionComboBox.getSelectedItem();

						PropertiesConfiguration config = new PropertiesConfiguration(propertyFile);

						config.setProperty(WorkspaceConstants.WORKSPACE_PRODUCT_PROPERTY, productKey);

						config.save();

						ProjectRootManager projectRootManager = ProjectRootManager.getInstance(_project);

						VfsUtil.markDirtyAndRefresh(true, true, true, projectRootManager.getContentRoots());
					}
				}
				catch (ConfigurationException | FileNotFoundException exception) {
					Class<?> clazz = exception.getClass();

					String exceptionMessage = "";

					if (clazz.isInstance(FileNotFoundException.class)) {
						exceptionMessage = "<b>File gradle.properties does not exist.</b>";
					}
					else if (clazz.isInstance(ConfigurationException.class)) {
						exceptionMessage = "<b>File gradle.properties is not writable</b>";
					}

					NotificationData notificationData = new NotificationData(
						exceptionMessage, "<i>" + _project.getName() + "</i> \n" + exception.getMessage(),
						NotificationCategory.WARNING, NotificationSource.TASK_EXECUTION);

					notificationData.setBalloonNotification(true);

					ExternalSystemNotificationManager externalSystemNotificationManager =
						ExternalSystemNotificationManager.getInstance(_project);

					externalSystemNotificationManager.showNotification(GradleConstants.SYSTEM_ID, notificationData);
				}
			});

		super.doOKAction();
	}

	@Nullable
	@Override
	protected ValidationInfo doValidate() {
		if (_productVersionComboBox.getSelectedIndex() == -1) {
			return new ValidationInfo("Please configure product version");
		}

		return super.doValidate();
	}

	protected void rendererProductVersion(boolean showAll) {
		Application application = ApplicationManager.getApplication();

		application.executeOnPooledThread(
			() -> {
				List<String> allWorkspaceProducts = Arrays.asList(BladeCLI.getWorkspaceProducts(showAll));

				if (ListUtil.isNotEmpty(allWorkspaceProducts)) {
					_productVersionComboBox.removeAllItems();
				}

				allWorkspaceProducts.forEach(productVersion -> _productVersionComboBox.addItem(productVersion));

				_productVersionComboBox.setSelectedIndex(0);
			});
	}

	private JComboBox<String> _productVersionComboBox;
	private final Project _project;

}