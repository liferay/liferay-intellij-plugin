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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;

import com.liferay.ide.idea.util.BladeCLI;
import com.liferay.ide.idea.util.CoreUtil;
import com.liferay.ide.idea.util.LiferayWorkspaceUtil;

import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jetbrains.annotations.Nullable;

/**
 * @author Terry Jia
 */
public class LiferayModuleWizardStep extends ModuleWizardStep {

	public LiferayModuleWizardStep(LiferayModuleBuilder builder, Project project) {
		_builder = builder;
		_project = project;
		_typesTree = new Tree();

		_typesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		_typesTree.setRootVisible(false);
		_typesTree.setShowsRootHandles(true);

		TreeSelectionModel treeSelectionModel = _typesTree.getSelectionModel();

		treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		treeSelectionModel.addTreeSelectionListener(
			event -> {
				TreePath treePath = event.getNewLeadSelectionPath();

				Object lastPathComponent = treePath.getLastPathComponent();

				String type = lastPathComponent.toString();

				if (Objects.equals("theme-contributor", type) || Objects.equals("theme", type) ||
					Objects.equals("layout-template", type)) {

					_packageName.setEditable(false);
					_packageName.setEnabled(false);
					_className.setEditable(false);
					_className.setEnabled(false);
					_servcieName.setEnabled(false);
					_servcieName.setEditable(false);
				}
				else if (Objects.equals("service-builder", type)) {
					_packageName.setEditable(true);
					_packageName.setEnabled(true);
					_className.setEditable(false);
					_className.setEditable(false);
					_servcieName.setEnabled(false);
					_servcieName.setEditable(false);
				}
				else if (Objects.equals("service", type)) {
					_packageName.setEditable(true);
					_packageName.setEnabled(true);
					_className.setEditable(true);
					_className.setEnabled(true);
					_servcieName.setEnabled(true);
					_servcieName.setEditable(true);
				}
				else if (Objects.equals("service-wrapper", type)) {
					_packageName.setEditable(true);
					_packageName.setEnabled(true);
					_className.setEditable(true);
					_className.setEnabled(true);
					_servcieName.setEnabled(true);
					_servcieName.setEditable(true);
				}
				else {
					_packageName.setEditable(true);
					_packageName.setEnabled(true);
					_className.setEditable(true);
					_className.setEnabled(true);
					_servcieName.setEnabled(false);
					_servcieName.setEditable(false);
				}
			});

		JScrollPane typesScrollPane = ScrollPaneFactory.createScrollPane(_typesTree);

		_typesPanel.add(typesScrollPane, "archetypes");

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);

		String liferayVersion = LiferayWorkspaceUtil.getLiferayVersion(_project);

		for (String type : BladeCLI.getProjectTemplates()) {
			if (Objects.equals("fragment", type) || Objects.equals("modules-ext", type) ||
				(Objects.equals("7.0", liferayVersion) && Objects.equals("social-bookmark", type))) {

				continue;
			}

			DefaultMutableTreeNode node = new DefaultMutableTreeNode(type, true);

			root.add(node);
		}

		TreeModel model = new DefaultTreeModel(root);

		_typesTree.setModel(model);

		_typesTree.setSelectionRow(0);
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

	public String getPackageName() {
		if (_packageName.isEditable()) {
			return _packageName.getText();
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

	public String getServiceName() {
		if (_servcieName.isEditable()) {
			return _servcieName.getText();
		}

		return null;
	}

	@Override
	public void updateDataModel() {
		_builder.setType(getSelectedType());
		_builder.setClassName(getClassName());
		_builder.setPackageName(getPackageName());

		if (getSelectedType().equals("service") || getSelectedType().equals("service-wrapper")) {
			_builder.setServiceName(getServiceName());
		}
	}

	@Override
	public boolean validate() throws ConfigurationException {
		String validationTitle = "Validation Error";

		if (CoreUtil.isNullOrEmpty(getSelectedType())) {
			throw new ConfigurationException("Please click one of the items to select a template", validationTitle);
		}

		ProjectManager projectManager = ProjectManager.getInstance();

		Project workspaceProject = projectManager.getOpenProjects()[0];

		String packageNameValue = getPackageName();
		String classNameValue = getClassName();
		PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(workspaceProject);
		PsiNameHelper psiNameHelper = PsiNameHelper.getInstance(workspaceProject);

		if (!CoreUtil.isNullOrEmpty(packageNameValue) && !psiDirectoryFactory.isValidPackageName(packageNameValue)) {
			throw new ConfigurationException(packageNameValue + " is not a valid package name", validationTitle);
		}

		if (!CoreUtil.isNullOrEmpty(classNameValue) && !psiNameHelper.isQualifiedName(classNameValue)) {
			throw new ConfigurationException(classNameValue + " is not a valid java class name", validationTitle);
		}

		return true;
	}

	private LiferayModuleBuilder _builder;
	private JTextField _className;
	private JPanel _mainPanel;
	private JTextField _packageName;
	private final Project _project;
	private JTextField _servcieName;
	private JPanel _typesPanel;
	private Tree _typesTree;

}