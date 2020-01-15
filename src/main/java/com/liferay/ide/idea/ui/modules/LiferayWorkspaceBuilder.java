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

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SdkSettingsStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Condition;

import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.util.BladeCLI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Joye Luo
 * @author Simon Jiang
 */
@SuppressWarnings("rawtypes")
public abstract class LiferayWorkspaceBuilder extends ModuleBuilder {

	public LiferayWorkspaceBuilder(String liferayProjectType) {
		_liferayProjectType = liferayProjectType;
	}

	@Nullable
	@Override
	public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
		return super.getCustomOptionsStep(context, parentDisposable);
	}

	@Override
	public ModuleType getModuleType() {
		return StdModuleTypes.JAVA;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
		JComboBox liferayVersionComboBox = new ComboBox<>();

		for (String liferayVersion : WorkspaceConstants.LIFERAY_VERSIONS) {
			liferayVersionComboBox.addItem(liferayVersion);
		}

		liferayVersionComboBox.setSelectedIndex(0);

		settingsStep.addSettingsField("Liferay version:", liferayVersionComboBox);

		JCheckBox enableTargetPlatformCheckBox = new JCheckBox();

		enableTargetPlatformCheckBox.setSelected(_enableTargetPlatform);

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
			settingsStep.addSettingsField("Enable target platform:", enableTargetPlatformCheckBox);
		}

		JComboBox targetPlatformComboBox = new ComboBox<>();

		for (String targetPlatformVersion : WorkspaceConstants.TARGET_PLATFORM_VERSIONS) {
			String version = (String)liferayVersionComboBox.getSelectedItem();

			if (targetPlatformVersion.startsWith(version)) {
				targetPlatformComboBox.addItem(targetPlatformVersion);
			}
		}

		targetPlatformComboBox.addActionListener(
			new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					if (targetPlatformComboBox.equals(event.getSource())) {
						_targetPlatform = (String)targetPlatformComboBox.getSelectedItem();
					}
				}

			});

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
			settingsStep.addSettingsField("Target platform:", targetPlatformComboBox);
		}

		liferayVersionComboBox.addActionListener(
			e -> {
				_liferayVersion = (String)liferayVersionComboBox.getSelectedItem();

				targetPlatformComboBox.removeAllItems();

				for (String targetPlatformVersion : WorkspaceConstants.TARGET_PLATFORM_VERSIONS) {
					String version = (String)liferayVersionComboBox.getSelectedItem();

					if (targetPlatformVersion.startsWith(version)) {
						targetPlatformComboBox.addItem(targetPlatformVersion);
					}
				}

				targetPlatformComboBox.setSelectedIndex(0);

				_targetPlatform = (String)targetPlatformComboBox.getSelectedItem();
			});

		JCheckBox indexSourcesCheckBox = new JCheckBox();

		JLabel customLabel = new JLabel();

		indexSourcesCheckBox.addActionListener(
			e -> {
				_indexSources = indexSourcesCheckBox.isSelected();

				if (_indexSources) {
					customLabel.setText(
						"This will cause all of the BOM artifacts jars and their java sources to be indexed by " +
							"IntelliJ. Note: this process can slow down your project's synchronization.");
				}
				else {
					customLabel.setText("");
				}
			});

		enableTargetPlatformCheckBox.addActionListener(
			e -> {
				_enableTargetPlatform = enableTargetPlatformCheckBox.isSelected();

				targetPlatformComboBox.setEnabled(_enableTargetPlatform);
				indexSourcesCheckBox.setEnabled(_enableTargetPlatform);
			});

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
			settingsStep.addSettingsField("Index Sources:", indexSourcesCheckBox);
			settingsStep.addSettingsField("", customLabel);
		}

		return new SdkSettingsStep(
			settingsStep, this,
			new Condition<SdkTypeId>() {

				@Override
				public boolean value(SdkTypeId sdkType) {
					return isSuitableSdkType(sdkType);
				}

			});
	}

	protected void initWorkspace(Project project) {
		StringBuilder sb = new StringBuilder();

		sb.append("--base ");
		sb.append("\"");
		sb.append(project.getBasePath());
		sb.append("\" ");
		sb.append("init ");
		sb.append("-v ");
		sb.append(_liferayVersion);
		sb.append(" ");
		sb.append("-f ");

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			sb.append("-b ");
			sb.append("maven");
		}

		PropertiesComponent component = PropertiesComponent.getInstance(project);

		component.setValue(WorkspaceConstants.WIZARD_LIFERAY_VERSION_FIELD, _liferayVersion);

		BladeCLI.execute(sb.toString());

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE) && _enableTargetPlatform) {
			try {
				PropertiesConfiguration config = new PropertiesConfiguration(
					new File(project.getBasePath(), "gradle.properties"));

				config.setProperty(WorkspaceConstants.TARGET_PLATFORM_VERSION_PROPERTY, _targetPlatform);
				config.setProperty(WorkspaceConstants.TARGET_PLATFORM_INDEX_SOURCES_PROPERTY, _indexSources);

				config.save();
			}
			catch (ConfigurationException ce) {
			}
		}
	}

	private boolean _enableTargetPlatform = true;
	private boolean _indexSources = false;
	private String _liferayProjectType;
	private String _liferayVersion = WorkspaceConstants.LIFERAY_VERSIONS[0];
	private String _targetPlatform = WorkspaceConstants.TARGET_PLATFORM_VERSIONS[0];

}