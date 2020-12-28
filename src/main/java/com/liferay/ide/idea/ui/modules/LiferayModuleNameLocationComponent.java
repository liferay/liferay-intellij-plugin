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

import com.intellij.ide.IdeBundle;
import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.util.projectWizard.AbstractModuleBuilder;
import com.intellij.ide.util.projectWizard.ProjectWizardUtil;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;

import com.liferay.ide.idea.core.LiferayCore;
import com.liferay.ide.idea.core.MessagesBundle;
import com.liferay.ide.idea.core.WorkspaceConstants;
import com.liferay.ide.idea.core.WorkspaceProvider;
import com.liferay.ide.idea.ui.modules.ext.LiferayModuleExtBuilder;
import com.liferay.ide.idea.ui.modules.springmvcportlet.SpringMVCPortletModuleBuilder;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 */
public class LiferayModuleNameLocationComponent implements LiferayWorkspaceSupport {

	public LiferayModuleNameLocationComponent(@NotNull WizardContext context) {
		_context = context;

		_moduleContentRoot.setEnabled(false);

		_moduleFileLocation.setEnabled(false);
	}

	public void bindModuleSettings(LiferayNamePathComponent namePathComponent) {
		JTextField nameComponent = namePathComponent.getNameComponent();

		Document nameDocument = nameComponent.getDocument();

		nameDocument.addDocumentListener(
			new DocumentAdapter() {

				protected void textChanged(DocumentEvent event) {
					if (!_moduleNameChangedByUser) {
						setModuleName(namePathComponent.getNameValue());
					}
				}

			});

		JTextField pathComponent = namePathComponent.getPathComponent();

		Document pathDocument = pathComponent.getDocument();

		pathDocument.addDocumentListener(
			new DocumentAdapter() {

				protected void textChanged(DocumentEvent event) {
					if (!_contentRootChangedByUser) {
						_setModuleContentRoot(namePathComponent.getPath());
					}
				}

			});

		Document document = _moduleName.getDocument();

		document.addDocumentListener(
			new DocumentAdapter() {

				protected void textChanged(DocumentEvent event) {
					if (_moduleNameDocListenerEnabled) {
						_moduleNameChangedByUser = true;
					}

					String path = _getDefaultBaseDir(_context, namePathComponent);
					String moduleName = _getModuleName();

					if ((path.length() > 0) && !Comparing.strEqual(moduleName, namePathComponent.getNameValue())) {
						String targetFolderName = _getTargetFolderName();

						if (Objects.isNull(targetFolderName)) {
							path += "/" + moduleName;
						}
						else {
							path += "/" + targetFolderName + "/" + moduleName;
						}
					}

					if (!_contentRootChangedByUser) {
						boolean f = _moduleNameChangedByUser;
						_moduleNameChangedByUser = true;
						_setModuleContentRoot(path);
						_moduleNameChangedByUser = f;
					}

					if (!_imlLocationChangedByUser) {
						_setImlFileLocation(path);
					}
				}

			});

		Document moduleContentRootDocument = _moduleContentRoot.getDocument();

		moduleContentRootDocument.addDocumentListener(
			new DocumentAdapter() {

				protected void textChanged(DocumentEvent event) {
					if (_contentRootDocListenerEnabled) {
						_contentRootChangedByUser = true;
					}

					if (!_imlLocationChangedByUser) {
						_setImlFileLocation(_getModuleContentRoot());
					}

					if (!_moduleNameChangedByUser) {
						String path = FileUtil.toSystemIndependentName(_getModuleContentRoot());

						int idx = path.lastIndexOf("/");

						boolean f = _contentRootChangedByUser;
						_contentRootChangedByUser = true;

						boolean i = _imlLocationChangedByUser;
						_imlLocationChangedByUser = true;

						setModuleName((idx >= 0) ? path.substring(idx + 1) : "");

						_contentRootChangedByUser = f;
						_imlLocationChangedByUser = i;
					}
				}

			});

		Document moduleFileLocationDocument = _moduleFileLocation.getDocument();

		moduleFileLocationDocument.addDocumentListener(
			new DocumentAdapter() {

				protected void textChanged(DocumentEvent event) {
					if (_imlLocationDocListenerEnabled) {
						_imlLocationChangedByUser = true;
					}
				}

			});

		JTextField jTextField = namePathComponent.getPathComponent();

		Document namePathDocument = jTextField.getDocument();

		namePathDocument.addDocumentListener(
			new DocumentAdapter() {

				protected void textChanged(DocumentEvent event) {
					if (!_imlLocationChangedByUser) {
						_setImlFileLocation(namePathComponent.getPath());
					}
				}

			});
	}

	@Nullable
	public AbstractModuleBuilder getModuleBuilder() {
		return (AbstractModuleBuilder)_context.getProjectBuilder();
	}

	public JTextField getModuleNameField() {
		return _moduleName;
	}

	public JPanel getModulePanel() {
		return _modulePanel;
	}

	public void setModuleName(String moduleName) {
		_moduleNameDocListenerEnabled = false;
		_moduleName.setText(moduleName);
		_moduleNameDocListenerEnabled = true;
	}

	public void updateDataModel() {
		AbstractModuleBuilder moduleBuilder = getModuleBuilder();

		if (moduleBuilder == null) {
			return;
		}

		String moduleName = _getModuleName();

		moduleBuilder.setName(moduleName);
		moduleBuilder.setModuleFilePath(
			FileUtil.toSystemIndependentName(_moduleFileLocation.getText()) + "/" + moduleName +
				ModuleFileType.DOT_DEFAULT_EXTENSION);

		moduleBuilder.setContentEntryPath(FileUtil.toSystemIndependentName(_getModuleContentRoot()));
	}

	public void updateLocations() {
		Project project = _context.getProject();

		assert project != null;

		VirtualFile baseDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (baseDir != null) {
			String baseDirPath = baseDir.getPath();

			String moduleName = ProjectWizardUtil.findNonExistingFileName(baseDirPath, "untitled", "");

			setModuleName(moduleName);

			String contentRoot = null;

			if (Objects.isNull(_getTargetFolderName())) {
				contentRoot = baseDirPath + "/" + moduleName;
			}
			else {
				contentRoot = baseDirPath + "/" + _getTargetFolderName() + "/" + moduleName;
			}

			_setModuleContentRoot(contentRoot);
			_setImlFileLocation(contentRoot);

			_moduleName.select(0, moduleName.length());
		}
	}

	public boolean validate() throws ConfigurationException {
		AbstractModuleBuilder builder = getModuleBuilder();

		if ((builder != null) && !builder.validateModuleName(_getModuleName())) {
			return false;
		}

		if (!_validateModulePaths()) {
			return false;
		}

		_validateExistingModuleName();

		return true;
	}

	private static String _getDefaultBaseDir(WizardContext wizardContext, LiferayNamePathComponent namePathComponent) {
		if (wizardContext.isCreatingNewProject()) {
			return namePathComponent.getPath();
		}

		Project project = wizardContext.getProject();

		assert project != null;

		VirtualFile baseDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (baseDir != null) {
			return baseDir.getPath();
		}

		return "";
	}

	private String _getModuleContentRoot() {
		return _moduleContentRoot.getText();
	}

	private String _getModuleName() {
		String moduleName = _moduleName.getText();

		return moduleName.trim();
	}

	private String _getTargetFolderName() {
		Project project = _context.getProject();

		WorkspaceProvider workspaceProvider = LiferayCore.getWorkspaceProvider(project);

		if (Objects.isNull(workspaceProvider)) {
			return WorkspaceConstants.MODULES_DIR_DEFAULT;
		}

		AbstractModuleBuilder builder = getModuleBuilder();

		String targetFolderName = null;
		boolean warProject = false;

		if (builder instanceof LiferayModuleBuilder) {
			LiferayModuleBuilder liferayModuleBuilder = (LiferayModuleBuilder)builder;

			String templateType = liferayModuleBuilder.getType();

			for (String projectType : _WAR_TYPE_PROJECT) {
				if (projectType.equals(templateType)) {
					warProject = true;

					break;
				}
			}

			if (Objects.equals("js-theme", templateType)) {
				targetFolderName = getWorkspaceProperty(
					project, WorkspaceConstants.THEMES_DIR_PROPERTY, WorkspaceConstants.THEMES_DIR_DEFAULT);
			}
			else if (Objects.equals("war-core-ext", templateType)) {
				targetFolderName = getWorkspaceProperty(
					project, WorkspaceConstants.EXT_DIR_PROPERTY, WorkspaceConstants.EXT_DIR_DEFAULT);
			}
			else if (!warProject) {
				String[] defaultModuleDirs = workspaceProvider.getWorkspaceModuleDirs();

				if (Objects.nonNull(defaultModuleDirs)) {
					targetFolderName = defaultModuleDirs[0];
				}
			}
		}
		else if (builder instanceof LiferayModuleExtBuilder) {
			targetFolderName = workspaceProvider.getWorkspaceProperty(
				WorkspaceConstants.EXT_DIR_PROPERTY, WorkspaceConstants.EXT_DIR_DEFAULT);
		}
		else if (builder instanceof SpringMVCPortletModuleBuilder) {
			warProject = true;
		}

		if (warProject) {
			String[] defaultWarDirs = workspaceProvider.getWorkspaceWarDirs();

			if (Objects.nonNull(defaultWarDirs)) {
				if (!workspaceProvider.isGradleWorkspace()) {
					targetFolderName = Stream.of(
						defaultWarDirs
					).filter(
						warDir -> Files.exists(Paths.get(project.getBasePath(), warDir))
					).findAny(
					).orElseGet(
						project::getBasePath
					);
				}
				else {
					targetFolderName = defaultWarDirs[0];
				}
			}
		}

		return targetFolderName;
	}

	private void _setImlFileLocation(String path) {
		_imlLocationDocListenerEnabled = false;
		_moduleFileLocation.setText(FileUtil.toSystemDependentName(path));
		_imlLocationDocListenerEnabled = true;
	}

	private void _setModuleContentRoot(String path) {
		_contentRootDocListenerEnabled = false;
		_moduleContentRoot.setText(FileUtil.toSystemDependentName(path));
		_contentRootDocListenerEnabled = true;
	}

	private void _validateExistingModuleName() throws ConfigurationException {
		Project project = _context.getProject();

		if (project == null) {
			return;
		}

		String moduleName = _getModuleName();

		ModuleManager moduleManager = ModuleManager.getInstance(project);

		Module module = moduleManager.findModuleByName(moduleName);

		if (module != null) {
			String msg = "Module \'" + moduleName + "\' already exists in project. Please specify another name.";

			throw new ConfigurationException(msg);
		}
	}

	private boolean _validateModulePaths() throws ConfigurationException {
		String moduleName = _getModuleName();

		String moduleFileDirectory = _moduleFileLocation.getText();

		if (moduleFileDirectory.length() == 0) {
			throw new ConfigurationException("Enter module file location");
		}

		if (moduleName.length() == 0) {
			throw new ConfigurationException("Enter a module name");
		}

		File moduleFile = new File(moduleFileDirectory, moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION);

		if (moduleFile.exists()) {
			String identification = IdeBundle.message("project.new.wizard.module.identification");
			String existsTitle = IdeBundle.message("title.file.already.exists");

			String filePrompt = MessagesBundle.message(
				"prompt.overwrite.project.file", moduleFile.getAbsolutePath(), identification);

			int answer = Messages.showYesNoDialog(filePrompt, existsTitle, Messages.getQuestionIcon());

			if (answer != Messages.YES) {
				return false;
			}
		}

		return true;
	}

	private static final String[] _WAR_TYPE_PROJECT = {"layout-template", "theme", "war-hook", "war-mvc-portlet"};

	private boolean _contentRootChangedByUser = false;
	private boolean _contentRootDocListenerEnabled = true;
	private WizardContext _context;
	private boolean _imlLocationChangedByUser = false;
	private boolean _imlLocationDocListenerEnabled = true;
	private JTextField _moduleContentRoot;
	private JTextField _moduleFileLocation;
	private JTextField _moduleName;
	private boolean _moduleNameChangedByUser = false;
	private boolean _moduleNameDocListenerEnabled = true;
	private JPanel _modulePanel;

}