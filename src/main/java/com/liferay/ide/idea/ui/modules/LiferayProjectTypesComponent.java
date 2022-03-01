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

import aQute.bnd.version.Version;
import aQute.bnd.version.VersionRange;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.FileUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.IOUtils;

import org.jetbrains.annotations.Nullable;

/**
 * @author Seiphon Wang
 */
public class LiferayProjectTypesComponent extends JPanel implements LiferayWorkspaceSupport {

	public LiferayProjectTypesComponent() {
	}

	public LiferayProjectTypesComponent(WizardContext context) {
		UIUtil.invokeLaterIfNeeded(
			() -> {
				Application application = ApplicationManager.getApplication();

				application.executeOnPooledThread(this::_loadSupportedVersionRanges);
			});
	}

	public LiferayModuleBuilder getModuleBuilder() {
		if (_context.getProjectBuilder() instanceof LiferayModuleBuilder) {
			return (LiferayModuleBuilder)_context.getProjectBuilder();
		}

		return null;
	}

	@Nullable
	public String getSelectedType() {
		Object selectedType = _typesTree.getLastSelectedPathComponent();

		if (selectedType != null) {
			return selectedType.toString();
		}

		return null;
	}

	public void hideComponent() {
		_liferayVersionLabel.setVisible(false);
		_liferayVersionCombo.setVisible(false);
		_typesPanel.setVisible(false);
		_projectTypeLable.setVisible(false);
	}

	public void initProjectTypeComponent(
		LiferayModuleNameLocationComponent moduleNameLocationComponent, WizardContext context) {

		_context = context;

		_project = context.getProject();

		_typesTree = new Tree();

		_typesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		_typesTree.setRootVisible(false);
		_typesTree.setShowsRootHandles(true);

		TreeSelectionModel treeSelectionModel = _typesTree.getSelectionModel();

		treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		treeSelectionModel.addTreeSelectionListener(
			event -> {
				LiferayModuleBuilder builder = getModuleBuilder();

				if (Objects.nonNull(builder)) {
					TreePath treePath = event.getNewLeadSelectionPath();

					JTextField moduleNameField = moduleNameLocationComponent.getModuleNameField();

					builder.setType(String.valueOf(treePath.getLastPathComponent()));

					moduleNameLocationComponent.updateLocations(moduleNameField.getText());
				}
			});

		JScrollPane typesScrollPane = ScrollPaneFactory.createScrollPane(_typesTree);

		_typesPanel.add(typesScrollPane, "archetypes");

		_liferayVersion = getLiferayVersion(_project);

		if (Objects.isNull(_liferayVersion)) {
			_liferayVersion = WorkspaceConstants.DEFAULT_LIFERAY_VERSION;

			_liferayVersionCombo.removeAllItems();

			for (String liferayVersionItem : WorkspaceConstants.LIFERAY_VERSIONS) {
				_liferayVersionCombo.addItem(liferayVersionItem);
			}

			_liferayVersionCombo.setSelectedItem(_liferayVersion);

			_liferayVersionCombo.addActionListener(
				e -> _liferayVersion = (String)_liferayVersionCombo.getSelectedItem());
		}
		else {
			_mainPanel.remove(_liferayVersionLabel);

			_mainPanel.remove(_liferayVersionCombo);

			_mainPanel.repaint();
		}

		SwingUtilities.invokeLater(
			() -> {
				DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);

				for (String type : BladeCLI.getProjectTemplates()) {
					if (Objects.equals("fragment", type) || Objects.equals("modules-ext", type) ||
						Objects.equals("spring-mvc-portlet", type)) {

						continue;
					}

					DefaultMutableTreeNode node = new DefaultMutableTreeNode(type, true);

					root.add(node);
				}

				TreeModel model = new DefaultTreeModel(root);

				_typesTree.setModel(model);

				_typesTree.setSelectionRow(0);
			});
	}

	public void updateDataModel() {
		LiferayModuleBuilder liferayModuleBuilder = getModuleBuilder();

		if (Objects.isNull(liferayModuleBuilder)) {
			return;
		}

		liferayModuleBuilder.setType(getSelectedType());
		liferayModuleBuilder.setLiferayVersion(_liferayVersion);
	}

	public boolean validatComponent() throws ConfigurationException {
		String validationTitle = "Validation Error";

		String type = getSelectedType();

		if (type.equals("js-theme") || type.equals("js-widget")) {
			throw new ConfigurationException(
				"This wizard does not support creating this type of module. Create it using the CLI first and then " +
					"import here.",
				validationTitle);
		}

		if (LiferayWorkspaceSupport.isValidMavenWorkspaceProject(_project)) {
			if (Objects.equals(type, "form-field")) {
				VersionRange requiredVersionRange = new VersionRange(
					true, new Version("7.0"), new Version("7.2"), false);

				if (!requiredVersionRange.includes(new Version(_liferayVersion))) {
					throw new ConfigurationException(
						"Form Field project is only supported for versions 7.0 and 7.1 with Maven", validationTitle);
				}
			}

			if (Objects.equals(type, "war-core-ext")) {
				throw new ConfigurationException(
					"Creating war-core-ext project with Maven is not supported", validationTitle);
			}
		}

		if (type.equals("war-core-ext")) {
			Version notSupportFromPortalVersion = new Version("7.3");

			Version currentVersion = Version.parseVersion(_liferayVersion);

			if (currentVersion.compareTo(notSupportFromPortalVersion) >= 0) {
				throw new ConfigurationException(
					"War Core Ext project is only supported starting from portal 7.0 to 7.2", validationTitle);
			}
		}

		String projectTemplateName = type.replaceAll("-", ".");

		VersionRange versionRange = _projectTemplateVersionRangeMap.get(projectTemplateName);

		if (versionRange != null) {
			boolean include = versionRange.includes(new Version(_liferayVersion));

			if (!include) {
				boolean npm = type.startsWith("npm");

				if (npm) {
					throw new ConfigurationException(
						"NPM portlet project templates generated from this tool are not supported for specified " +
							"Liferay version. See LPS-97950 for full details.",
						validationTitle);
				}

				throw new ConfigurationException(
					"Specified Liferay version is invalid. Must be in range " + versionRange, validationTitle);
			}
		}
		else {
			throw new ConfigurationException("Unable to get supported Liferay version", validationTitle);
		}

		return true;
	}

	private void _loadSupportedVersionRanges() {
		File bladeJar = BladeCLI.getBladeJar(BladeCLI.getBladeJarVersion());

		if (bladeJar != null) {
			try (ZipFile zipFile = new ZipFile(bladeJar)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();

					String entryName = entry.getName();

					if (entryName.endsWith(".jar") && entryName.startsWith("com.liferay.project.templates.")) {
						try (InputStream in = zipFile.getInputStream(entry)) {
							Path tempDirectory = Files.createTempDirectory("template-directory-for-blade");

							File stateFile = tempDirectory.toFile();

							File tempFile = new File(stateFile, entryName);

							FileUtil.writeFile(tempFile, in);

							try (ZipFile tempZipFile = new ZipFile(tempFile)) {
								Enumeration<? extends ZipEntry> tempEntries = tempZipFile.entries();

								while (tempEntries.hasMoreElements()) {
									ZipEntry tempEntry = tempEntries.nextElement();

									String tempEntryName = tempEntry.getName();

									if (tempEntryName.equals("META-INF/MANIFEST.MF")) {
										try (InputStream manifestInput = tempZipFile.getInputStream(tempEntry)) {
											List<String> lines = IOUtils.readLines(
												manifestInput, Charset.defaultCharset());

											for (String line : lines) {
												String liferayVersionString = "Liferay-Versions:";

												if (line.startsWith(liferayVersionString)) {
													String versionRangeValue = line.substring(
														liferayVersionString.length());

													String projectTemplateName = entryName.substring(
														"com.liferay.project.templates.".length(),
														entryName.indexOf("-"));

													_projectTemplateVersionRangeMap.put(
														projectTemplateName, new VersionRange(versionRangeValue));

													break;
												}
											}
										}

										break;
									}
								}
							}

							tempFile.delete();

							stateFile.delete();
						}
					}
				}
			}
			catch (IOException ioException) {
			}
		}
	}

	private static Map<String, VersionRange> _projectTemplateVersionRangeMap = new HashMap<>();

	private WizardContext _context;
	private String _liferayVersion;
	private JComboBox<String> _liferayVersionCombo;
	private JLabel _liferayVersionLabel;
	private JPanel _mainPanel;
	private Project _project;
	private JLabel _projectTypeLable;
	private JPanel _typesPanel;
	private Tree _typesTree;

}