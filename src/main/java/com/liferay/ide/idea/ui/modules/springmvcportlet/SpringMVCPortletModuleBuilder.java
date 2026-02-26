/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules.springmvcportlet;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.projectWizard.ProjectTypeStep;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import com.liferay.ide.idea.core.LiferayIcons;
import com.liferay.ide.idea.core.LiferayProjectTypeService;
import com.liferay.ide.idea.ui.modules.LiferayProjectSettingsStep;
import com.liferay.ide.idea.ui.modules.LiferayProjectType;
import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.IntellijUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceSupport;
import com.liferay.release.util.ReleaseEntry;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * @author Terry Jia
 * @author Simon Jiang
 */
public class SpringMVCPortletModuleBuilder extends ModuleBuilder {

	public SpringMVCPortletModuleBuilder() {
		addListener(
			new ModuleBuilderListener() {

				@Override
				public void moduleCreated(@NotNull Module module) {
					Project project = module.getProject();

					ProjectType projectType = LiferayProjectTypeService.getProjectType(project);

					if (Objects.equals(projectType.getId(), LiferayProjectType.LIFERAY_GRADLE_WORKSPACE)) {
						ExternalSystemUtil.refreshProject(
							project.getBasePath(),
							new ImportSpecBuilder(
								project, GradleConstants.SYSTEM_ID
							).use(
								ProgressExecutionMode.IN_BACKGROUND_ASYNC
							));
					}

					removeListener(this);
				}

			});
	}

	@Override
	public ModuleWizardStep[] createFinishingSteps(
		@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {

		return new ModuleWizardStep[] {new SpringMvcPortletModuleWizardStep(wizardContext, this)};
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
		return "Liferay Spring MVC Portlet";
	}

	@NotNull
	public List<Class<? extends ModuleWizardStep>> getIgnoredSteps() {
		List<Class<? extends ModuleWizardStep>> ingoreStepList = new ArrayList<>(super.getIgnoredSteps());

		ingoreStepList.add(ProjectTypeStep.class);
		ingoreStepList.add(ProjectSettingsStep.class);

		return ingoreStepList;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ModuleType getModuleType() {
		return StdModuleTypes.JAVA;
	}

	@Override
	public Icon getNodeIcon() {
		return LiferayIcons.SPRING_ICON;
	}

	@Override
	public String getPresentableName() {
		return "Liferay Spring MVC Portlet";
	}

	public void setFramework(String framework) {
		_framework = framework;
	}

	public void setFrameworkDependencies(String frameworkDependencies) {
		_frameworkDependencies = frameworkDependencies;
	}

	public void setLiferayProductGroupVersion(String liferayProductGroupVersion) {
		_liferayProductGroupVersion = liferayProductGroupVersion;
	}

	public void setPackageName(String packageName) {
		_packageName = packageName;
	}

	@Override
	public void setupRootModel(ModifiableRootModel modifiableRootModel) {
		VirtualFile virtualFile = _createAndGetContentEntry();

		Project project = modifiableRootModel.getProject();

		ProjectType projectType = LiferayProjectTypeService.getProjectType(project);

		String typeId = projectType.getId();

		_createProject(virtualFile, project, typeId);

		modifiableRootModel.addContentEntry(virtualFile);

		if (myJdk != null) {
			modifiableRootModel.setSdk(myJdk);
		}
		else {
			modifiableRootModel.inheritSdk();
		}

		_refreshProject(project);
	}

	public void setViewType(String viewType) {
		_viewType = viewType;
	}

	@Override
	public boolean validateModuleName(@NotNull String moduleName) {
		return IntellijUtil.validateExistingModuleName(moduleName);
	}

	private VirtualFile _createAndGetContentEntry() {
		String path = FileUtilRt.toSystemIndependentName(getContentEntryPath());

		File file = new File(path);

		file.mkdirs();

		LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

		return localFileSystem.refreshAndFindFileByPath(path);
	}

	private void _createProject(VirtualFile projectRoot, Project project, String typeId) {
		VirtualFile virtualFile = projectRoot.getParent();

		List<String> args = new ArrayList<>();

		args.add("create -d");
		args.add(BladeCLI.quote(virtualFile.getPath()));

		String targetPlatformVersion = LiferayWorkspaceSupport.getTargetPlatformVersion(project);

		if (Objects.equals(typeId, LiferayProjectType.LIFERAY_MAVEN_WORKSPACE)) {
			args.add("-b");
			args.add("maven");

			if (targetPlatformVersion != null) {
				args.add("-v");
				args.add(targetPlatformVersion);
			}
		}
		else {
			args.add("-v");
			args.add(_liferayProductGroupVersion);
		}

		if (targetPlatformVersion != null) {
			ReleaseEntry releaseEntry = LiferayWorkspaceSupport.getReleaseEntry("portal", targetPlatformVersion);

			if (releaseEntry != null) {
				args.add("--liferay-product");
				args.add(releaseEntry.getProduct());
			}
		}

		args.add("--base");
		args.add(BladeCLI.quote(project.getBasePath()));
		args.add("-t");
		args.add("spring-mvc-portlet");
		args.add(BladeCLI.quote(projectRoot.getName()));
		args.add("--framework");
		args.add(_framework);
		args.add("--framework-dependencies");
		args.add(_frameworkDependencies);
		args.add("--view-type");
		args.add(_viewType);

		if (!CoreUtil.isNullOrEmpty(_packageName)) {
			args.add("-p");
			args.add(_packageName);
		}

		BladeCLI.execute(project, args);
	}

	private void _refreshProject(Project project) {
		VirtualFile projectDir = LiferayWorkspaceSupport.getWorkspaceVirtualFile(project);

		if (projectDir == null) {
			return;
		}

		projectDir.refresh(false, true);
	}

	private String _framework;
	private String _frameworkDependencies;
	private String _liferayProductGroupVersion;
	private String _packageName;
	private String _viewType;

}