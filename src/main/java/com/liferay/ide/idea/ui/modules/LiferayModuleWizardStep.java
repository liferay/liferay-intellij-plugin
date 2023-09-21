/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.ui.modules;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.impl.file.PsiDirectoryFactory;

import com.liferay.ide.idea.util.CoreUtil;

import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 * @author Simon Jiang
 * @author Ethan Sun
 * @author Seiphon Wang
 */
public class LiferayModuleWizardStep extends ModuleWizardStep {

	public LiferayModuleWizardStep(LiferayModuleBuilder builder, WizardContext context) {
		_builder = builder;
		_context = context;
	}

	public String getClassName() {
		if (_className.isEditable()) {
			return _className.getText();
		}

		return null;
	}

	public JComponent getComponent() {
		return _mainPanel;
	}

	public String getContributorType() {
		if (_contributorType.isEditable()) {
			return _contributorType.getText();
		}

		return null;
	}

	public String getPackageName() {
		if (_packageName.isEditable()) {
			return _packageName.getText();
		}

		return null;
	}

	@Nullable
	public String getSelectedType() {
		return _builder.getType();
	}

	public String getServiceName() {
		if (_servcieName.isEditable()) {
			return _servcieName.getText();
		}

		return null;
	}

	@Override
	public void updateDataModel() {
		_builder.setClassName(getClassName());
		_builder.setPackageName(getPackageName());
		_builder.setContributorType(getContributorType());

		if (Objects.equals(getSelectedType(), "service") || Objects.equals(getSelectedType(), "service-wrapper")) {
			_builder.setServiceName(getServiceName());
		}
	}

	@Override
	public void updateStep() {
		String type = _builder.getType();

		if (Objects.equals(type, "theme") || Objects.equals(type, "layout-template")) {
			_packageName.setEditable(false);
			_packageName.setEnabled(false);
			_className.setEditable(false);
			_className.setEnabled(false);
			_servcieName.setEnabled(false);
			_servcieName.setEditable(false);
			_contributorType.setEditable(false);
			_contributorType.setEnabled(false);
		}
		else if (Objects.equals(type, "theme-contributor")) {
			_packageName.setEditable(false);
			_packageName.setEnabled(false);
			_className.setEditable(false);
			_className.setEnabled(false);
			_servcieName.setEnabled(false);
			_servcieName.setEditable(false);
			_contributorType.setEditable(true);
			_contributorType.setEnabled(true);
		}
		else if (Objects.equals(type, "service-builder")) {
			_packageName.setEditable(true);
			_packageName.setEnabled(true);
			_className.setEditable(false);
			_className.setEditable(false);
			_servcieName.setEnabled(false);
			_servcieName.setEditable(false);
			_contributorType.setEditable(false);
			_contributorType.setEnabled(false);
		}
		else if (Objects.equals(type, "service")) {
			_packageName.setEditable(true);
			_packageName.setEnabled(true);
			_className.setEditable(true);
			_className.setEnabled(true);
			_servcieName.setEnabled(true);
			_servcieName.setEditable(true);
			_contributorType.setEditable(false);
			_contributorType.setEnabled(false);
		}
		else if (Objects.equals(type, "service-wrapper")) {
			_packageName.setEditable(true);
			_packageName.setEnabled(true);
			_className.setEditable(true);
			_className.setEnabled(true);
			_servcieName.setEnabled(true);
			_servcieName.setEditable(true);
			_contributorType.setEditable(false);
			_contributorType.setEnabled(false);
		}
		else {
			_packageName.setEditable(true);
			_packageName.setEnabled(true);
			_className.setEditable(true);
			_className.setEnabled(true);
			_servcieName.setEnabled(false);
			_servcieName.setEditable(false);
			_contributorType.setEditable(false);
			_contributorType.setEnabled(false);
		}
	}

	@Override
	public boolean validate() throws ConfigurationException {
		String validationTitle = "Validation Error";

		String type = getSelectedType();

		if (CoreUtil.isNullOrEmpty(type)) {
			throw new ConfigurationException("Please click one of the items to select a template", validationTitle);
		}

		Project project = _context.getProject();

		if (Objects.isNull(project)) {
			throw new ConfigurationException("Can not find valid liferay workspace project", validationTitle);
		}

		PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(project);

		String packageNameValue = getPackageName();

		if (CoreUtil.isNullOrEmpty(packageNameValue)) {
			LiferayModuleBuilder liferayModuleBuilder = _builder;

			String packageName = liferayModuleBuilder.getPackageName();

			if (StringUtil.isEmpty(packageName)) {
				String moduleName = _builder.getName();

				packageName = moduleName.replace('-', '.');

				packageName = packageName.replace(' ', '.');

				packageName = packageName.toLowerCase();

				if (!psiDirectoryFactory.isValidPackageName(packageName)) {
					throw new ConfigurationException(
						"default package name " + packageName + " is not a valid package name", "Validation Error");
				}
			}
		}
		else if (!psiDirectoryFactory.isValidPackageName(packageNameValue)) {
			throw new ConfigurationException(packageNameValue + " is not a valid package name", validationTitle);
		}

		PsiNameHelper psiNameHelper = PsiNameHelper.getInstance(project);

		String classNameValue = getClassName();

		if (!CoreUtil.isNullOrEmpty(classNameValue) && !psiNameHelper.isQualifiedName(classNameValue)) {
			throw new ConfigurationException(classNameValue + " is not a valid java class name", validationTitle);
		}

		String serviceNameValue = getServiceName();

		if ((type.equals("service") || type.equals("service-wrapper")) && CoreUtil.isNullOrEmpty(serviceNameValue)) {
			throw new ConfigurationException("service name can not be null for " + type + " template", validationTitle);
		}

		return true;
	}

	private LiferayModuleBuilder _builder;
	private JTextField _className;
	private WizardContext _context;
	private JTextField _contributorType;
	private JPanel _mainPanel;
	private JTextField _packageName;
	private JTextField _servcieName;

}