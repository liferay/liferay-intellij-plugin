/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.projectWizard.ProjectTypeStep;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.LiferayProjectTypeService;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.IntellijUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Ethan Sun
 */
public class LiferayModuleBuilder extends ModuleBuilder implements LiferayWorkspaceSupport {

	public LiferayModuleBuilder() {
		addListener(
			new ModuleBuilderListener() {

				@Override
				public void moduleCreated(@NotNull Module module) {
					Project project = module.getProject();

					if (Objects.isNull(project.getBasePath())) {
						return;
					}

					ProjectType projectType = LiferayProjectTypeService.getProjectType(project);

					if (Objects.equals(projectType.getId(), LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
						ExternalSystemUtil.refreshProject(
							project, GradleConstants.SYSTEM_ID, project.getBasePath(), false,
							ProgressExecutionMode.IN_BACKGROUND_ASYNC);
					}

					removeListener(this);
				}

			});
	}

	@Override
	public ModuleWizardStep[] createFinishingSteps(
		@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {

		return new ModuleWizardStep[] {new LiferayModuleWizardStep(this)};
	}

	@Override
	public ModuleWizardStep[] createWizardSteps(
		@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {

		return new ModuleWizardStep[] {new LiferayProjectSettingsStep(wizardContext)};
	}

	@Override
	public String getBuilderId() {
		Class<?> clazz = getClass();

		return clazz.getName();
	}

	@Override
	public String getDescription() {
		return _LIFERAY_MODULES;
	}

	@NotNull
	public List<Class<? extends ModuleWizardStep>> getIgnoredSteps() {
		List<Class<? extends ModuleWizardStep>> ingoreStepList = new ArrayList<>(super.getIgnoredSteps());

		ingoreStepList.add(ProjectTypeStep.class);
		ingoreStepList.add(ProjectSettingsStep.class);

		return ingoreStepList;
	}

	@SuppressWarnings("rawtypes")
	public ModuleType getModuleType() {
		return StdModuleTypes.JAVA;
	}

	@Override
	public Icon getNodeIcon() {
		return LiferayIcons.LIFERAY_ICON;
	}

	public String getPackageName() {
		return _packageName;
	}

	@Override
	public String getPresentableName() {
		return _LIFERAY_MODULES;
	}

	public String getType() {
		return _type;
	}

	@Nullable
	@Override
	public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
		return null;
	}

	public void setClassName(String className) {
		_className = className;
	}

	public void setContributorType(String contributorType) {
		_contributorType = contributorType;
	}

	public void setLiferayVersion(String liferayVersion) {
		_liferayVersion = liferayVersion;
	}

	public void setPackageName(String packageName) {
		_packageName = packageName;
	}

	public void setServiceName(String serviceName) {
		_serviceName = serviceName;
	}

	public void setType(String type) {
		_type = type;
	}

	@Override
	public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) {
		VirtualFile moduleDir = _createAndGetContentEntry();

		if (Objects.isNull(moduleDir)) {
			return;
		}

		Project project = modifiableRootModel.getProject();

		ProjectType liferayProjectType = LiferayProjectTypeService.getProjectType(project);

		VirtualFile moduleParentDir = moduleDir.getParent();

		StringBuilder sb = new StringBuilder();

		sb.append("create ");
		sb.append("-d \"");
		sb.append(moduleParentDir.getPath());
		sb.append("\" ");
		sb.append("--base \"");
		sb.append(project.getProjectFilePath());
		sb.append("\" ");

		String typeId = liferayProjectType.getId();

		if (Objects.equals(typeId, LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			sb.append("-b ");
			sb.append("maven ");
		}

		String targetPlatformVersion = getTargetPlatformVersion(project);

		if (targetPlatformVersion != null) {
			sb.append("-v ");
			sb.append(targetPlatformVersion);
		}

		sb.append(" ");
		sb.append("-t ");
		sb.append(_type);
		sb.append(" ");

		if (!CoreUtil.isNullOrEmpty(_className)) {
			sb.append("-c ");
			sb.append(_className);
			sb.append(" ");
		}

		if (!CoreUtil.isNullOrEmpty(_packageName)) {
			sb.append("-p ");
			sb.append(_packageName);
			sb.append(" ");
		}

		if (_type.equals("theme-contributor")) {
			sb.append("-C ");

			if (!CoreUtil.isNullOrEmpty(_contributorType)) {
				sb.append(_contributorType);
			}
			else {
				sb.append(moduleDir.getName());
			}

			sb.append(" ");
		}

		if ((_type.equals("service") || _type.equals("service-wrapper")) && !CoreUtil.isNullOrEmpty(_serviceName)) {
			sb.append("-s ");
			sb.append(_serviceName);
			sb.append(" ");
		}

		sb.append("\"");
		sb.append(moduleDir.getName());
		sb.append("\" ");

		BladeCLI.execute(project, sb.toString());

		modifiableRootModel.addContentEntry(moduleDir);

		if (myJdk != null) {
			modifiableRootModel.setSdk(myJdk);
		}
		else {
			modifiableRootModel.inheritSdk();
		}

		_refreshProject(project);
	}

	@Override
	public boolean validateModuleName(@NotNull String moduleName) {
		return IntellijUtil.validateExistingModuleName(moduleName);
	}

	private VirtualFile _createAndGetContentEntry() {
		String contentEntryPath = getContentEntryPath();

		if (Objects.isNull(contentEntryPath)) {
			return null;
		}

		String path = FileUtil.toSystemIndependentName(contentEntryPath);

		FileUtil.createDirectory(new File(path));

		LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

		return localFileSystem.refreshAndFindFileByPath(path);
	}

	private void _refreshProject(Project project) {
		VirtualFile projectDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDir == null) {
			return;
		}

		projectDir.refresh(false, true);
	}

	private static final String _LIFERAY_MODULES = "Liferay Modules";

	private String _className;
	private String _contributorType;
	private String _liferayVersion;
	private String _packageName;
	private String _serviceName;
	private String _type;

}