/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import aQute.bnd.version.Version;
import aQute.bnd.version.VersionRange;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.IOUtils;

import org.jetbrains.annotations.Nullable;

/**
 * @author Seiphon Wang
 */
public class LiferayProjectTypesComponent extends JPanel {

	public LiferayProjectTypesComponent() {
	}

	public LiferayProjectTypesComponent(WizardContext context) {
		UIUtil.invokeLaterIfNeeded(
			() -> {
				Application application = ApplicationManager.getApplication();

				application.executeOnPooledThread(() -> _loadSupportedVersionRanges(context.getProject()));
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
		_typesPanel.setVisible(false);
		_projectTypeLabel.setVisible(false);
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

		_liferayVersion = LiferayWorkspaceSupport.getLiferayVersion(_project);

		_projectTypeLabel.setVisible(true);

		_mainPanel.repaint();

		Application application = ApplicationManager.getApplication();

		application.runWriteAction(
			new Runnable() {

				@Override
				public void run() {
					SwingUtilities.invokeLater(
						() -> {
							DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);

							DefaultTreeModel model = new DefaultTreeModel(root);

							root.add(new DefaultMutableTreeNode());

							DefaultMutableTreeNode loadingNode = (DefaultMutableTreeNode)root.getChildAt(0);

							loadingNode.setUserObject("Loading Module Project Template Types......");

							_typesTree.setModel(model);

							model.nodeStructureChanged(root);

							CompletableFuture<String[]> future = CompletableFuture.supplyAsync(
								() -> BladeCLI.getProjectTemplates(_project));

							future.thenAccept(
								new Consumer<>() {

									@Override
									public void accept(String[] projectTemplates) {
										SwingUtilities.invokeLater(
											new Runnable() {

												@Override
												public void run() {
													root.removeAllChildren();

													for (String type : projectTemplates) {
														if (Objects.equals(type, "fragment") ||
															Objects.equals(type, "modules-ext") ||
															Objects.equals(type, "spring-mvc-portlet") ||
															Objects.equals(type, "client-extension")) {

															continue;
														}

														DefaultMutableTreeNode node = new DefaultMutableTreeNode(
															type, true);

														root.add(node);
													}

													_typesTree.setModel(model);

													model.nodeStructureChanged(root);

													_typesTree.setSelectionRow(0);
												}

											});
									}

								});
						});
				}

			});
	}

	public void updateDataModel() {
		LiferayModuleBuilder liferayModuleBuilder = getModuleBuilder();

		if (Objects.isNull(liferayModuleBuilder)) {
			return;
		}

		liferayModuleBuilder.setType(getSelectedType());
	}

	public boolean validateComponent() throws ConfigurationException {
		String validationTitle = "Validation Error";

		if (Objects.isNull(LiferayWorkspaceSupport.getTargetPlatformVersion(_context.getProject()))) {
			throw new ConfigurationException(
				"Please set correct target platform version for liferay workspace project", validationTitle);
		}

		String type = getSelectedType();

		if (Objects.isNull(type)) {
			throw new ConfigurationException(
				"The module project type is invalid. Please choose a valid project type.", validationTitle);
		}

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

	private void _loadSupportedVersionRanges(Project project) {
		File bladeJar = BladeCLI.getBladeJar(BladeCLI.getBladeJarVersion(project));

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
				_logger.error(ioException);
			}
		}
	}

	private static Logger _logger = Logger.getInstance(LiferayProjectTypesComponent.class);

	private static Map<String, VersionRange> _projectTemplateVersionRangeMap = new HashMap<>();

	private WizardContext _context;
	private String _liferayVersion;
	private JPanel _mainPanel;
	private Project _project;
	private JLabel _projectTypeLabel;
	private JPanel _typesPanel;
	private Tree _typesTree;

}