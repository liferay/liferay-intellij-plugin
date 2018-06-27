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

package com.liferay.ide.idea.terminal;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.awt.RelativeRectangle;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.docking.DockContainer;
import com.intellij.ui.docking.DockManager;
import com.intellij.ui.docking.DockableContent;
import com.intellij.util.ui.UIUtil;

import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TabbedTerminalWidget;
import com.jediterm.terminal.ui.TerminalWidget;

import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.JBTabbedTerminalWidget;
import org.jetbrains.plugins.terminal.vfs.TerminalSessionVirtualFileImpl;

/**
 * @author Terry Jia
 */
public class GogoShellView {

	public static GogoShellView getInstance(@NotNull Project project) {
		return project.getComponent(GogoShellView.class);
	}

	public GogoShellView(Project project) {
		_project = project;
	}

	public void initTerminal(final ToolWindow toolWindow) {
		GogoShellDirectRunner terminalRunner = new GogoShellDirectRunner(_project);

		toolWindow.setToHideOnEmptyContent(true);

		Content content = _createTerminalInContentPanel(terminalRunner, toolWindow);

		ContentManager manager = toolWindow.getContentManager();

		manager.addContent(content);

		ToolWindowManagerEx managerEx = (ToolWindowManagerEx)ToolWindowManager.getInstance(_project);

		managerEx.addToolWindowManagerListener(
			new ToolWindowManagerListener() {

				@Override
				public void stateChanged() {
					ToolWindowManager windowManager = ToolWindowManager.getInstance(_project);

					ToolWindow window = windowManager.getToolWindow(GogoShellToolWindowFactory.TOOL_WINDOW_ID);

					if (window != null) {
						boolean visible = window.isVisible();

						if (visible) {
							ContentManager contentManager = toolWindow.getContentManager();

							if (contentManager.getContentCount() == 0) {
								initTerminal(window);
							}
						}
					}
				}

				@Override
				public void toolWindowRegistered(@NotNull String id) {
				}

			});

		Disposer.register(
			_project,
			new Disposable() {

				@Override
				public void dispose() {
					if (_terminalWidget != null) {
						_terminalWidget.dispose();

						_terminalWidget = null;
					}
				}

			});

		if (_dockContainer == null) {
			_dockContainer = new TerminalDockContainer(toolWindow);

			Disposer.register(_project, _dockContainer);

			DockManager dockManager = DockManager.getInstance(_project);

			dockManager.register(_dockContainer);
		}
	}

	public void openLocalSession(Project project, ToolWindow terminal) {
		GogoShellDirectRunner terminalRunner = new GogoShellDirectRunner(project);

		_openSession(terminal, terminalRunner);
	}

	public class TerminalDockContainer implements DockContainer {

		public TerminalDockContainer(ToolWindow toolWindow) {
			_terminalToolWindow = toolWindow;
		}

		@Override
		public void add(@NotNull DockableContent content, RelativePoint dropTarget) {
			if (_isTerminalSessionContent(content)) {
				TerminalSessionVirtualFileImpl terminalFile = (TerminalSessionVirtualFileImpl)content.getKey();

				_terminalWidget.addTab(terminalFile.getName(), terminalFile.getTerminal());

				JediTermWidget widget = terminalFile.getTerminal();

				widget.setNextProvider(_terminalWidget);
			}
		}

		@Override
		public void addListener(Listener listener, Disposable parent) {
		}

		@Override
		public void closeAll() {
		}

		@Override
		public void dispose() {
		}

		@Override
		public RelativeRectangle getAcceptArea() {
			return new RelativeRectangle(_terminalToolWindow.getComponent());
		}

		@Override
		public RelativeRectangle getAcceptAreaFallback() {
			return getAcceptArea();
		}

		@Override
		public JComponent getContainerComponent() {
			return _terminalToolWindow.getComponent();
		}

		@NotNull
		@Override
		public ContentResponse getContentResponse(@NotNull DockableContent content, RelativePoint point) {
			if (_isTerminalSessionContent(content)) {
				return ContentResponse.ACCEPT_MOVE;
			}

			return ContentResponse.DENY;
		}

		@Override
		public void hideNotify() {
		}

		@Override
		public boolean isDisposeWhenEmpty() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Nullable
		@Override
		public Image processDropOver(@NotNull DockableContent content, RelativePoint point) {
			return null;
		}

		@Override
		public void resetDropOver(@NotNull DockableContent content) {
		}

		@Override
		public void showNotify() {
		}

		@Nullable
		@Override
		public Image startDropOver(@NotNull DockableContent content, RelativePoint point) {
			return null;
		}

		private boolean _isTerminalSessionContent(DockableContent content) {
			return content.getKey() instanceof TerminalSessionVirtualFileImpl;
		}

		private final ToolWindow _terminalToolWindow;

	}

	private static ActionToolbar _createToolbar(
		@Nullable final AbstractTerminalRunner terminalRunner, @NotNull final JBTabbedTerminalWidget terminal,
		@NotNull ToolWindow toolWindow) {

		DefaultActionGroup group = new DefaultActionGroup();

		if (terminalRunner != null) {
			group.add(new NewSession(terminalRunner, terminal));
			group.add(new CloseSession(terminal, toolWindow));
		}

		ActionManager manager = ActionManager.getInstance();

		return manager.createActionToolbar("GogoShellTerminal", group, false);
	}

	private static void _hideIfNoActiveSessions(
		@NotNull final ToolWindow toolWindow, @NotNull JBTabbedTerminalWidget terminal) {

		if (terminal.isNoActiveSessions()) {
			ContentManager manager = toolWindow.getContentManager();

			manager.removeAllContents(true);
		}
	}

	private FocusListener _createFocusListener() {
		return new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				JComponent component = _getComponentToFocus();

				if (component != null) {
					component.requestFocusInWindow();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
			}

		};
	}

	private Content _createTerminalInContentPanel(
		@NotNull AbstractTerminalRunner terminalRunner, @NotNull ToolWindow toolWindow) {

		GogoShellToolWindowPanel panel = new GogoShellToolWindowPanel(
			PropertiesComponent.getInstance(_project), toolWindow);

		ContentFactory factory = ContentFactory.SERVICE.getInstance();

		Content content = factory.createContent(panel, "", false);

		content.setCloseable(true);

		_terminalWidget = terminalRunner.createTerminalWidget(content);

		_terminalWidget.addTabListener(
			new TabbedTerminalWidget.TabListener() {

				@Override
				public void tabClosed(JediTermWidget terminal) {
					UIUtil.invokeLaterIfNeeded(
						() -> {
							if (_terminalWidget != null) {
								_hideIfNoActiveSessions(toolWindow, _terminalWidget);
							}
						});
				}

			});

		panel.setContent(_terminalWidget.getComponent());

		panel.addFocusListener(_createFocusListener());

		ActionToolbar toolbar = _createToolbar(terminalRunner, _terminalWidget, toolWindow);

		JComponent component = toolbar.getComponent();

		component.addFocusListener(_createFocusListener());

		toolbar.setTargetComponent(panel);

		panel.setToolbar(component);

		panel.uiSettingsChanged(null);

		content.setPreferredFocusableComponent(_terminalWidget.getComponent());

		return content;
	}

	private JComponent _getComponentToFocus() {
		if (_terminalWidget != null) {
			return _terminalWidget.getComponent();
		}

		return null;
	}

	private void _openSession(@NotNull ToolWindow toolWindow, @NotNull AbstractTerminalRunner terminalRunner) {
		if (_terminalWidget == null) {
			ContentManager contentManager = toolWindow.getContentManager();

			contentManager.removeAllContents(true);

			contentManager.addContent(_createTerminalInContentPanel(terminalRunner, toolWindow));
		}
		else {
			terminalRunner.openSession(_terminalWidget);
		}

		toolWindow.activate(
			() -> {
			},
			true);
	}

	private TerminalDockContainer _dockContainer;
	private final Project _project;
	private JBTabbedTerminalWidget _terminalWidget;

	private static class CloseSession extends DumbAwareAction {

		public CloseSession(@NotNull JBTabbedTerminalWidget terminal, @NotNull ToolWindow toolWindow) {
			super("Close Session", "Close Terminal Session", AllIcons.Actions.Delete);

			_terminal = terminal;
			_toolWindow = toolWindow;
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			_terminal.closeCurrentSession();

			_hideIfNoActiveSessions(_toolWindow, _terminal);
		}

		private final JBTabbedTerminalWidget _terminal;
		private final ToolWindow _toolWindow;

	}

	private static class NewSession extends DumbAwareAction {

		public NewSession(@NotNull AbstractTerminalRunner terminalRunner, @NotNull TerminalWidget terminal) {
			super("New Session", "Create New Terminal Session", AllIcons.General.Add);

			_terminalRunner = terminalRunner;
			_terminal = terminal;
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			_terminalRunner.openSession(_terminal);
		}

		private final TerminalWidget _terminal;
		private final AbstractTerminalRunner _terminalRunner;

	}

}