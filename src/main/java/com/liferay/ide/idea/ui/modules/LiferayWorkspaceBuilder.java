/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SdkSettingsStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.MavenUtil;
import com.liferay.release.util.ReleaseEntry;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.maven.model.Model;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Joye Luo
 * @author Simon Jiang
 * @author Ethan Sun
 * @author Seiphon Wang
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
		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			JComboBox liferayProductGroupVersionComboBox = new ComboBox<>();

			for (String liferayProductGroupVersion : LiferayWorkspaceSupport.getProductGroupVersions()) {
				liferayProductGroupVersionComboBox.addItem(liferayProductGroupVersion);
			}

			liferayProductGroupVersionComboBox.setSelectedIndex(0);

			JComboBox targetPlatformComboBox = _getTargetPlatformVersionComboBox(liferayProductGroupVersionComboBox);

			settingsStep.addSettingsField("Liferay version:", liferayProductGroupVersionComboBox);

			settingsStep.addSettingsField("Target platform:", targetPlatformComboBox);

			return ReadAction.compute(
				() -> new SdkSettingsStep(settingsStep, this, this::isSuitableSdkType) {

					@Override
					public boolean validate() throws com.intellij.openapi.options.ConfigurationException {
						if (targetPlatformComboBox.getSelectedIndex() == -1) {
							Messages.showWarningDialog(
								"Create liferay maven workspace project failure, target platform version can not be " +
									"null.",
								"Projects Not Created");

							return false;
						}

						return super.validate();
					}

				});
		}

		JComboBox<String> productVersionComboBox = new ComboBox<>();

		JCheckBox showAllProductVersionCheckBox = new JCheckBox();

		showAllProductVersionCheckBox.setSelected(false);

		showAllProductVersionCheckBox.addActionListener(
			e -> {
				boolean showAllProductVersion = showAllProductVersionCheckBox.isSelected();

				_initProductVersionComBox(productVersionComboBox, showAllProductVersion);
			});

		_initProductVersionComBox(productVersionComboBox, false);

		productVersionComboBox.setEditable(false);

		productVersionComboBox.addActionListener(
			event -> _productVersion = (String)productVersionComboBox.getSelectedItem());

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

		settingsStep.addSettingsField("Product version:", productVersionComboBox);

		settingsStep.addSettingsField("Show All Product Versions", showAllProductVersionCheckBox);

		settingsStep.addSettingsField("Index Sources:", indexSourcesCheckBox);

		settingsStep.addSettingsField("", customLabel);

		return ReadAction.compute(
			() -> new SdkSettingsStep(settingsStep, this, this::isSuitableSdkType) {

				@Override
				public boolean validate() throws com.intellij.openapi.options.ConfigurationException {
					if (productVersionComboBox.getSelectedIndex() == -1) {
						Messages.showWarningDialog(
							"Create liferay gradle workspace project failure, product version can not be null.",
							"Projects Not Created");

						return false;
					}

					return super.validate();
				}

			});
	}

	protected void initWorkspace(Project project) {
		List<String> args = new ArrayList<>();

		args.add("--base");
		args.add(BladeCLI.quote(project.getBasePath()));
		args.add("init");
		args.add("-v");

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			args.add(_targetPlatform);
		}
		else if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
			args.add(_productVersion);
		}

		ReleaseEntry releaseEntry = LiferayWorkspaceSupport.getReleaseEntry("portal", _targetPlatform);

		if (releaseEntry != null) {
			args.add("--liferay-product");
			args.add(releaseEntry.getProduct());
		}

		args.add("-f");

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			args.add("-b");
			args.add("maven");
		}

		PropertiesComponent component = PropertiesComponent.getInstance(project);

		component.setValue(WorkspaceConstants.WIZARD_LIFERAY_VERSION_FIELD, _liferayProductGroupVersion);

		BladeCLI.executeWithLatestBlade(args);

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
			try {
				PropertiesConfiguration config = new PropertiesConfiguration(
					new File(project.getBasePath(), "gradle.properties"));

				config.setProperty(WorkspaceConstants.TARGET_PLATFORM_INDEX_SOURCES_PROPERTY, _indexSources);

				config.save();
			}
			catch (ConfigurationException configurationException) {
				_logger.error(configurationException);
			}
		}
		else if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			Path pomFilePath = FileUtil.pathAppend(project.getBasePath(), "pom.xml");

			if (FileUtil.exists(pomFilePath)) {
				try {
					File pomFile = pomFilePath.toFile();

					Model pomModel = MavenUtil.getMavenModel(pomFile);

					Properties properties = pomModel.getProperties();

					properties.setProperty(WorkspaceConstants.WORKSPACE_BOM_VERSION, _targetPlatform);

					if (Objects.nonNull(releaseEntry)) {
						properties.setProperty(WorkspaceConstants.BUNDLE_URL_PROPERTY, releaseEntry.getBundleURL());

						MavenUtil.updateMavenPom(pomModel, pomFile);

						VirtualFile projectVirtualFile = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

						if (Objects.nonNull(projectVirtualFile)) {
							projectVirtualFile.refresh(true, true);
						}
					}
					else {
						_logger.error("Unable set correct target platform version for project " + project.getName());
					}
				}
				catch (IOException | XmlPullParserException exception) {
					_logger.error(exception);
				}
			}
		}
	}

	@NotNull
	private JComboBox _getTargetPlatformVersionComboBox(JComboBox liferayProductGroupVersionComboBox) {
		JComboBox<String> targetPlatformComboBox = new ComboBox<>();

		String productGroupVersion = (String)liferayProductGroupVersionComboBox.getSelectedItem();

		Application application = ApplicationManager.getApplication();

		application.invokeLater(
			() -> SwingUtilities.invokeLater(
				() -> {
					_getTargetPlatformVersionsStream(
						productGroupVersion
					).forEach(
						targetPlatformComboBox::addItem
					);

					liferayProductGroupVersionComboBox.addActionListener(
						e -> {
							_liferayProductGroupVersion = (String)liferayProductGroupVersionComboBox.getSelectedItem();

							targetPlatformComboBox.removeAllItems();

							_getTargetPlatformVersionsStream(
								_liferayProductGroupVersion
							).forEach(
								targetPlatformComboBox::addItem
							);

							targetPlatformComboBox.setSelectedIndex(0);

							_targetPlatform = (String)targetPlatformComboBox.getSelectedItem();
						});
				}));

		targetPlatformComboBox.addActionListener(
			event -> {
				if (targetPlatformComboBox.equals(event.getSource())) {
					_targetPlatform = (String)targetPlatformComboBox.getSelectedItem();
				}
			});

		return targetPlatformComboBox;
	}

	private Stream<String> _getTargetPlatformVersionsStream(String productGroupVersion) {
		Stream<ReleaseEntry> releaseEntryStream = LiferayWorkspaceSupport.getReleaseEntryStream();

		return releaseEntryStream.filter(
			releaseEntry -> Objects.equals(releaseEntry.getProductGroupVersion(), productGroupVersion)
		).map(
			ReleaseEntry::getTargetPlatformVersion
		);
	}

	private void _initProductVersionComBox(JComboBox<String> productVersionComboBox, boolean showAllProductVersion) {
		Application application = ApplicationManager.getApplication();

		application.executeOnPooledThread(
			() -> {
				productVersionComboBox.setDoubleBuffered(true);

				productVersionComboBox.removeAllItems();

				for (String productVersion : LiferayWorkspaceSupport.getProductVersions(showAllProductVersion)) {
					productVersionComboBox.addItem(productVersion);
				}

				productVersionComboBox.setSelectedIndex(0);

				_productVersion = (String)productVersionComboBox.getSelectedItem();
			});
	}

	private static final Logger _logger = Logger.getInstance(LiferayWorkspaceBuilder.class);

	private boolean _indexSources = false;
	private String _liferayProductGroupVersion;
	private String _liferayProjectType;
	private String _productVersion;
	private String _targetPlatform;

}