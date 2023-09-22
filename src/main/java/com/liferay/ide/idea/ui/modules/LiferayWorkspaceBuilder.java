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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.ProductInfo;
import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.ide.idea.util.ListUtil;
import com.liferay.ide.idea.util.MavenUtil;
import com.liferay.ide.idea.util.Pair;
import com.liferay.workspace.bundle.url.codec.BundleURLCodec;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
			JComboBox liferayVersionComboBox = new ComboBox<>();

			for (String liferayVersion : WorkspaceConstants.LIFERAY_VERSIONS) {
				liferayVersionComboBox.addItem(liferayVersion);
			}

			liferayVersionComboBox.setSelectedIndex(0);

			JComboBox targetPlatformComboBox = _getTargetPlatformVersionComboBox(liferayVersionComboBox);

			settingsStep.addSettingsField("Liferay version:", liferayVersionComboBox);

			settingsStep.addSettingsField("Target platform:", targetPlatformComboBox);

			return new SdkSettingsStep(settingsStep, this, this::isSuitableSdkType) {

				@Override
				public boolean validate() throws com.intellij.openapi.options.ConfigurationException {
					if (targetPlatformComboBox.getSelectedIndex() == -1) {
						Messages.showWarningDialog(
							"Create liferay maven workspace project failure, target platform version can not be null.",
							"Projects Not Created");

						return false;
					}

					return super.validate();
				}

			};
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

		return new SdkSettingsStep(settingsStep, this, this::isSuitableSdkType) {

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

		};
	}

	protected void initWorkspace(Project project) {
		StringBuilder sb = new StringBuilder();

		sb.append("--base ");
		sb.append("\"");
		sb.append(project.getBasePath());
		sb.append("\" ");
		sb.append("init ");
		sb.append("-v ");

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			sb.append(_targetPlatform);
		}
		else if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
			sb.append(_productVersion);
		}

		sb.append(" ");
		sb.append("-f ");

		if (_liferayProjectType.equals(LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			sb.append("-b ");
			sb.append("maven");
		}

		PropertiesComponent component = PropertiesComponent.getInstance(project);

		component.setValue(WorkspaceConstants.WIZARD_LIFERAY_VERSION_FIELD, _liferayVersion);

		BladeCLI.executeWithLatestBlade(sb.toString());

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

					Map<String, String> targetPlatformBundleUrlMap = _initMavenPortalBundleUrlMap();

					if (Objects.nonNull(targetPlatformBundleUrlMap)) {
						properties.setProperty(
							WorkspaceConstants.BUNDLE_URL_PROPERTY, targetPlatformBundleUrlMap.get(_targetPlatform));

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

	private String _decodeBundleUrl(ProductInfo productInfo) {
		try {
			return BundleURLCodec.decode(productInfo.getBundleUrl(), productInfo.getReleaseDate());
		}
		catch (Exception exception) {
			_logger.error("Unable to determine bundle URL", exception);
		}

		return null;
	}

	@NotNull
	private JComboBox _getTargetPlatformVersionComboBox(JComboBox liferayVersionComboBox) {
		JComboBox<String> targetPlatformComboBox = new ComboBox<>();

		String version = (String)liferayVersionComboBox.getSelectedItem();

		Application application = ApplicationManager.getApplication();

		application.invokeLater(
			() -> {
				CompletableFuture<Map<String, String[]>> future = CompletableFuture.supplyAsync(
					() -> {
						try {
							return _initMavenTargetPlatform();
						}
						catch (Exception exception) {
							return null;
						}
					});

				future.thenAccept(
					targetPlatformMap -> SwingUtilities.invokeLater(
						() -> {
							try {
								String[] targetPlatformVersions = targetPlatformMap.get(version);

								Stream.of(
									targetPlatformVersions
								).forEach(
									targetPlatformComboBox::addItem
								);

								liferayVersionComboBox.addActionListener(
									e -> {
										_liferayVersion = (String)liferayVersionComboBox.getSelectedItem();

										targetPlatformComboBox.removeAllItems();

										String[] selectedTargetPlatformVersions = targetPlatformMap.get(
											_liferayVersion);

										Stream.of(
											selectedTargetPlatformVersions
										).forEach(
											targetPlatformComboBox::addItem
										);

										targetPlatformComboBox.setSelectedIndex(0);

										_targetPlatform = (String)targetPlatformComboBox.getSelectedItem();
									});
							}
							catch (Exception exception) {
								_logger.error("Unable to configure target platform version", exception);
							}
						}));
			});

		targetPlatformComboBox.addActionListener(
			event -> {
				if (targetPlatformComboBox.equals(event.getSource())) {
					_targetPlatform = (String)targetPlatformComboBox.getSelectedItem();
				}
			});

		return targetPlatformComboBox;
	}

	private Map<String, String> _initMavenPortalBundleUrlMap() {
		String[] workspaceProducts = BladeCLI.getWorkspaceProducts(true);

		if (ListUtil.isEmpty(workspaceProducts)) {
			return new HashMap<>();
		}

		Map<String, ProductInfo> productInfos = LiferayWorkspaceSupport.getProductInfos();

		if (Objects.isNull(productInfos)) {
			return new HashMap<>();
		}

		return Arrays.stream(
			workspaceProducts
		).unordered(
		).filter(
			product -> product.startsWith("portal")
		).map(
			productInfos::get
		).map(
			productInfo -> {
				try {
					String bundleUrl = _decodeBundleUrl(productInfo);

					String targetPlatformVersion = productInfo.getTargetPlatformVersion();

					return new Pair<>(targetPlatformVersion, bundleUrl);
				}
				catch (Exception exception) {
					_logger.error("Failed to decode bundle url", exception);
				}

				return null;
			}
		).filter(
			Objects::nonNull
		).collect(
			Collectors.toMap(Pair::first, Pair::second)
		);
	}

	private Map<String, String[]> _initMavenTargetPlatform() {
		Map<String, String[]> targetPlatformVersionMap = new HashMap<>();

		String[] workspaceProducts = BladeCLI.getWorkspaceProducts(true);

		if (CoreUtil.isNullOrEmpty(workspaceProducts)) {
			return targetPlatformVersionMap;
		}

		Map<String, ProductInfo> productInfos = LiferayWorkspaceSupport.getProductInfos();

		if (Objects.isNull(productInfos)) {
			return targetPlatformVersionMap;
		}

		for (String liferayVersion : WorkspaceConstants.LIFERAY_VERSIONS) {
			String[] targetPlatformVersions = Arrays.stream(
				workspaceProducts
			).unordered(
			).filter(
				product -> product.startsWith("portal")
			).map(
				productInfos::get
			).filter(
				productInfo -> {
					String targetPlatformVersion = productInfo.getTargetPlatformVersion();

					return targetPlatformVersion.startsWith(liferayVersion);
				}
			).map(
				ProductInfo::getTargetPlatformVersion
			).toArray(
				String[]::new
			);

			targetPlatformVersionMap.put(liferayVersion, targetPlatformVersions);
		}

		return targetPlatformVersionMap;
	}

	private void _initProductVersionComBox(JComboBox<String> productVersionComboBox, boolean showAllProductVersion) {
		Application application = ApplicationManager.getApplication();

		application.executeOnPooledThread(
			() -> {
				List<String> allWorkspaceProducts = Arrays.asList(BladeCLI.getWorkspaceProducts(showAllProductVersion));

				productVersionComboBox.setDoubleBuffered(true);

				if (ListUtil.isNotEmpty(allWorkspaceProducts)) {
					productVersionComboBox.removeAllItems();
				}

				allWorkspaceProducts.forEach(productVersionComboBox::addItem);

				productVersionComboBox.setSelectedIndex(0);

				_productVersion = (String)productVersionComboBox.getSelectedItem();
			});
	}

	private static final Logger _logger = Logger.getInstance(LiferayWorkspaceBuilder.class);

	private boolean _indexSources = false;
	private String _liferayProjectType;
	private String _liferayVersion;
	private String _productVersion;
	private String _targetPlatform;

}