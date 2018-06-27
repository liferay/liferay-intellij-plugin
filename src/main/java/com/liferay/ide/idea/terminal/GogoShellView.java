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

	public void init(final ToolWindow toolWindow) {
		GogoShellLocalRunner gogoShellLocalRunner = new GogoShellLocalRunner(_project);

		toolWindow.setToHideOnEmptyContent(true);

		Content content = _createGogoShellContent(gogoShellLocalRunner, toolWindow);

		ContentManager contentManager = toolWindow.getContentManager();

		contentManager.addContent(content);

		ToolWindowManagerEx toolWindowManagerEx = (ToolWindowManagerEx)ToolWindowManager.getInstance(_project);

		toolWindowManagerEx.addToolWindowManagerListener(
			new ToolWindowManagerListener() {

				@Override
				public void stateChanged() {
					ToolWindowManager windowManager = ToolWindowManager.getInstance(_project);

					ToolWindow gogoShellToolWindow = windowManager.getToolWindow(
						GogoShellToolWindowFactory.TOOL_WINDOW_ID);

					if (gogoShellToolWindow != null) {
						boolean visible = gogoShellToolWindow.isVisible();

						if (visible) {
							ContentManager contentManager = toolWindow.getContentManager();

							if (contentManager.getContentCount() == 0) {
								init(gogoShellToolWindow);
							}
						}
					}
				}

				@Override
				public void toolWindowRegistered(@NotNull String id) {
				}

			});
	}

	public void openLocalSession(Project project, ToolWindow toolWindow) {
		GogoShellLocalRunner gogoShellLocalRunner = new GogoShellLocalRunner(project);

		_openSession(toolWindow, gogoShellLocalRunner);
	}

	public class TerminalDockContainer implements DockContainer {

		public TerminalDockContainer(ToolWindow toolWindow, JBTabbedTerminalWidget terminalWidget) {
			_terminalToolWindow = toolWindow;
			_terminalWidget = terminalWidget;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void add(@NotNull DockableContent content, RelativePoint dropTarget) {
			if (_isTerminalSessionContent(content)) {
				TerminalSessionVirtualFileImpl terminalFile = (TerminalSessionVirtualFileImpl)content.getKey();

				_terminalWidget.addTab(terminalFile.getName(), terminalFile.getTerminal());

				JediTermWidget jediTermWidget = terminalFile.getTerminal();

				jediTermWidget.setNextProvider(_terminalWidget);
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
		@SuppressWarnings("rawtypes")
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
		@SuppressWarnings("rawtypes")
		public Image processDropOver(@NotNull DockableContent content, RelativePoint point) {
			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void resetDropOver(@NotNull DockableContent content) {
		}

		@Override
		public void showNotify() {
		}

		@Nullable
		@Override
		@SuppressWarnings("rawtypes")
		public Image startDropOver(@NotNull DockableContent content, RelativePoint point) {
			return null;
		}

		@SuppressWarnings("rawtypes")
		private boolean _isTerminalSessionContent(DockableContent dockableContent) {
			return dockableContent.getKey() instanceof TerminalSessionVirtualFileImpl;
		}

		private final ToolWindow _terminalToolWindow;
		private JBTabbedTerminalWidget _terminalWidget;

	}

	private static ActionToolbar _createToolbar(
		@Nullable final GogoShellLocalRunner gogoShellLocalRunner, @NotNull final JBTabbedTerminalWidget terminalWidget,
		@NotNull ToolWindow toolWindow) {

		DefaultActionGroup group = new DefaultActionGroup();

		if (gogoShellLocalRunner != null) {
			group.add(new NewSession(gogoShellLocalRunner, terminalWidget));
			group.add(new CloseSession(terminalWidget, toolWindow));
		}

		ActionManager manager = ActionManager.getInstance();

		return manager.createActionToolbar("GogoShellTerminal", group, false);
	}

	private static void _hideIfNoActiveSessions(
		@NotNull final ToolWindow toolWindow, @NotNull JBTabbedTerminalWidget terminalWidget) {

		if (terminalWidget.isNoActiveSessions()) {
			ContentManager contentManager = toolWindow.getContentManager();

			contentManager.removeAllContents(true);
		}
	}

	private Content _createGogoShellContent(
		@NotNull GogoShellLocalRunner gogoShellLocalRunner, @NotNull ToolWindow toolWindow) {

		GogoShellToolWindowPanel gogoShellToolWindowPanel = new GogoShellToolWindowPanel(
			PropertiesComponent.getInstance(_project), toolWindow);

		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

		Content content = contentFactory.createContent(gogoShellToolWindowPanel, "", false);

		content.setCloseable(true);

		JBTabbedTerminalWidget terminalWidget = gogoShellLocalRunner.createTerminalWidget(content);

		terminalWidget.addTabListener(
			new TabbedTerminalWidget.TabListener() {

				@Override
				public void tabClosed(JediTermWidget terminal) {
					UIUtil.invokeLaterIfNeeded(
						() -> {
							if (terminalWidget != null) {
								_hideIfNoActiveSessions(toolWindow, terminalWidget);
							}
						});
				}

			});

		Disposer.register(
			_project,
			new Disposable() {

				@Override
				public void dispose() {
					if (terminalWidget != null) {
						terminalWidget.dispose();
					}
				}

			});

		gogoShellToolWindowPanel.setContent(terminalWidget.getComponent());

		FocusListener focusListener = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if (terminalWidget != null) {
					JComponent component = terminalWidget.getComponent();

					component.requestFocusInWindow();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
			}

		};

		gogoShellToolWindowPanel.addFocusListener(focusListener);

		ActionToolbar actionToolbar = _createToolbar(gogoShellLocalRunner, terminalWidget, toolWindow);

		JComponent component = actionToolbar.getComponent();

		component.addFocusListener(focusListener);

		actionToolbar.setTargetComponent(gogoShellToolWindowPanel);

		gogoShellToolWindowPanel.setToolbar(component);

		gogoShellToolWindowPanel.uiSettingsChanged(null);

		content.setPreferredFocusableComponent(terminalWidget.getComponent());

		TerminalDockContainer terminalDockContainer = new TerminalDockContainer(toolWindow, terminalWidget);

		Disposer.register(_project, terminalDockContainer);

		DockManager dockManager = DockManager.getInstance(_project);

		dockManager.register(terminalDockContainer);

		return content;
	}

	private void _openSession(@NotNull ToolWindow toolWindow, @NotNull GogoShellLocalRunner terminalRunner) {
		JBTabbedTerminalWidget terminalWidget = terminalRunner.getTerminalWidget();

		if (terminalWidget == null) {
			ContentManager contentManager = toolWindow.getContentManager();

			contentManager.removeAllContents(true);

			contentManager.addContent(_createGogoShellContent(terminalRunner, toolWindow));
		}
		else {
			terminalRunner.openSession(terminalWidget);
		}

		toolWindow.activate(null, true);
	}

	private final Project _project;

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

		public NewSession(@NotNull GogoShellLocalRunner terminalRunner, @NotNull TerminalWidget terminal) {
			super("New Session", "Create New Terminal Session", AllIcons.General.Add);

			_terminalRunner = terminalRunner;
			_terminal = terminal;
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			_terminalRunner.openSession(_terminal);
		}

		private final TerminalWidget _terminal;
		private final GogoShellLocalRunner _terminalRunner;

	}

}