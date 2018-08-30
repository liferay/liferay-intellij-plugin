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

package com.liferay.ide.idea.language.osgi;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiAnnotationParameterList;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

import icons.LiferayIcons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * Completion Contributor for the @Component annotation
 *
 * @author Dominik Marks
 */
public class ComponentPropertiesCompletionContributor extends CompletionContributor {

	public ComponentPropertiesCompletionContributor() {
		_addCompletions(_createKeywordLookups());
	}

	private static String _getServiceClassName(PsiElement psiElement) {
		PsiAnnotationParameterList annotationParameterList = PsiTreeUtil.getParentOfType(
			psiElement, PsiAnnotationParameterList.class);

		if (annotationParameterList == null) {
			return null;
		}

		return Stream.of(
			annotationParameterList
		).map(
			list -> PsiTreeUtil.getChildrenOfType(list, PsiNameValuePair.class)
		).filter(
			Objects::nonNull
		).flatMap(
			Stream::of
		).filter(
			pair -> {
				String name = pair.getName();

				return name.equals("service");
			}
		).map(
			PsiNameValuePair::getValue
		).filter(
			value -> value instanceof PsiClassObjectAccessExpression
		).map(
			value -> {
				PsiTypeElement psiTypeElement = ((PsiClassObjectAccessExpression)value).getOperand();

				return psiTypeElement.getInnermostComponentReferenceElement();
			}
		).filter(
			Objects::nonNull
		).map(
			PsiJavaCodeReferenceElement::getQualifiedName
		).findFirst(
		).orElse(
			null
		);
	}

	private void _addCompletions(Map<String, List<LookupElementBuilder>> keywordLookups) {
		extend(
			CompletionType.BASIC, ComponentPropertiesPsiElementPatternCapture.instance,
			new CompletionProvider<CompletionParameters>() {

				@Override
				protected void addCompletions(
					@NotNull CompletionParameters parameters, ProcessingContext context,
					@NotNull CompletionResultSet result) {

					String serviceClassName = _getServiceClassName(parameters.getOriginalPosition());

					if (serviceClassName != null) {
						List<LookupElementBuilder> lookups = keywordLookups.get(serviceClassName);

						if (lookups != null) {
							result.addAllElements(lookups);

							result.stopHere();
						}
					}
				}

			});
	}

	private Map<String, List<LookupElementBuilder>> _createKeywordLookups() {
		Map<String, List<LookupElementBuilder>> keywordLookups = new HashMap<>();

		for (Map.Entry<String, String[][]> entry : _componentProperties.entrySet()) {
			List<LookupElementBuilder> lookups = new ArrayList<>();

			for (String[] keyword : entry.getValue()) {
				LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(keyword[0]);

				lookupElementBuilder = lookupElementBuilder.withTypeText(keyword[1]);
				lookupElementBuilder = lookupElementBuilder.withIcon(LiferayIcons.LIFERAY_ICON);

				lookups.add(lookupElementBuilder);
			}

			keywordLookups.put(entry.getKey(), lookups);
		}

		return keywordLookups;
	}

	//see https://dev.liferay.com/develop/reference/-/knowledge_base/7-0/portlet-descriptor-to-osgi-service-property-map
	private static Map<String, String[][]> _componentProperties = new HashMap<>();

	static {
		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/changing-adaptive-medias-image-scaling
		_componentProperties.put(
			"com.liferay.adaptive.media.image.scaler.AMImageScaler",
			new String[][] {{"mime.type", "String"}, {"service.ranking", "Integer"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/customizing-the-product-menu
		_componentProperties.put(
			"com.liferay.application.list.PanelApp",
			new String[][] {{"panel.app.order", "Integer"}, {"panel.category.key", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/customizing-the-product-menu
		_componentProperties.put(
			"com.liferay.application.list.PanelCategory",
			new String[][] {{"panel.category.key", "String"}, {"panel.category.order", "Integer"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/rendering-an-asset
		_componentProperties.put(
			"com.liferay.asset.kernel.model.AssetRendererFactory", new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.asset.kernel.validator.AssetEntryValidator",
			new String[][] {{"model.class.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/creating-form-field-types
		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.model.DDMFormFieldType",
			new String[][] {
				{"ddm.form.field.type.display.order", "Integer"}, {"ddm.form.field.type.icon", "String"},
				{"ddm.form.field.type.js.class", "String"}, {"ddm.form.field.type.js.module", "String"},
				{"ddm.form.field.type.label", "String"}, {"ddm.form.field.type.name", "String"}
			});

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/creating-form-field-types
		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.render.DDMFormFieldRenderer",
			new String[][] {{"ddm.form.field.type.name", "String"}, });

		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.util.DDMDisplay", new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.expando.kernel.model.CustomAttributesDisplay",
			new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.exportimport.content.processor.ExportImportContentProcessor",
			new String[][] {{"model.class.name", "String"}, });

		_componentProperties.put(
			"com.liferay.exportimport.kernel.controller.ExportImportController",
			new String[][] {{"model.class.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/data-handlers
		_componentProperties.put(
			"com.liferay.exportimport.kernel.lar.PortletDataHandler",
			new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.exportimport.kernel.lar.StagedModelDataHandler",
			new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.exportimport.portlet.preferences.processor.ExportImportPortletPreferencesProcessor",
			new String[][] {{"javax.portlet.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/creating-form-navigator-contexts
		_componentProperties.put(
			"com.liferay.frontend.taglib.form.navigator.context.FormNavigatorContextProvider",
			new String[][] {{"formNavigatorId", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/creating-custom-item-selector-views
		_componentProperties.put(
			"com.liferay.item.selector.ItemSelectorView", new String[][] {{"item.selector.view.order", "Integer"}, });

		_componentProperties.put(
			"com.liferay.portal.deploy.hot.CustomJspBag",
			new String[][] {{"context.id", "String"}, {"context.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.editor.configuration.EditorOptionsProvider",
			new String[][] {
				{"editor.config.key", "String"}, {"editor.name", "String"}, {"javax.portlet.name", "String"},
				{"service.ranking", "Integer"}
			});

		_componentProperties.put(
			"com.liferay.portal.kernel.atom.AtomCollectionAdapter", new String[][] {{"model.class.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor",
			new String[][] {{"background.task.executor.class.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.cache.configurator.PortalCacheConfiguratorSettings",
			new String[][] {{"portal.cache.manager.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/modifying-an-editors-configuration
		_componentProperties.put(
			"com.liferay.portal.kernel.editor.configuration.EditorConfigContributor",
			new String[][] {
				{"editor.config.key", "String"}, {"editor.name", "String"}, {"javax.portlet.name", "String"},
				{"service.ranking", "Integer"}
			});

		_componentProperties.put(
			"com.liferay.portal.kernel.events.LifecycleAction", new String[][] {{"key", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.messaging.Destination", new String[][] {{"destination.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.messaging.MessageListener", new String[][] {{"destination.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.model.ModelListener", new String[][] {{"service.ranking", "Integer"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.model.LayoutTypeController", new String[][] {{"layout.type", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.notifications.UserNotificationDefinition",
			new String[][] {{"javax.portlet.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/providing-the-user-personal-bar
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.AddPortletProvider",
			new String[][] {{"model.class.name", "String"}, {"service.ranking", "Integer"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/implementing-configuration-actions
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.ConfigurationAction",
			new String[][] {{"javax.portlet.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/providing-the-user-personal-bar
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.EditPortletProvider",
			new String[][] {{"model.class.name", "String"}, {"service.ranking", "Integer"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/making-urls-friendlier
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.FriendlyURLMapper",
			new String[][] {{"com.liferay.portlet.friendly-url-routes", "String"}, {"javax.portlet.name", "String"}});

		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.PortletLayoutListener",
			new String[][] {{"javax.portlet.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/providing-the-user-personal-bar
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.ViewPortletProvider",
			new String[][] {{"model.class.name", "String"}, {"service.ranking", "Integer"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/mvc-action-command
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand",
			new String[][] {{"javax.portlet.name", "String"}, {"mvc.command.name", "String"}});

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/mvc-render-command
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand",
			new String[][] {{"javax.portlet.name", "String"}, {"mvc.command.name", "String"}});

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/mvc-resource-command
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand",
			new String[][] {{"javax.portlet.name", "String"}, {"mvc.command.name", "String"}});

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/configuring-your-admin-apps-actions-menu
		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.configuration.icon.PortletConfigurationIcon",
			new String[][] {{"javax.portlet.name", "String"}, {"path", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.toolbar.contributor.PortletToolbarContributor",
			new String[][] {{"javax.portlet.name", "String"}, {"mvc.path", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.poller.PollerProcessor", new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.scheduler.messaging.SchedulerEventMessageListener",
			new String[][] {{"destination.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/introduction-to-liferay-search
		_componentProperties.put(
			"com.liferay.portal.kernel.search.IndexerPostProcessor",
			new String[][] {{"indexer.class.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/introduction-to-liferay-search
		_componentProperties.put(
			"com.liferay.portal.kernel.search.IndexSearcher", new String[][] {{"search.engine.impl", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/introduction-to-liferay-search
		_componentProperties.put(
			"com.liferay.portal.kernel.search.IndexWriter", new String[][] {{"search.engine.impl", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/introduction-to-liferay-search
		_componentProperties.put(
			"com.liferay.portal.kernel.search.hits.HitsProcessor", new String[][] {{"sort.order", "int"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/introduction-to-liferay-search
		_componentProperties.put(
			"com.liferay.portal.kernel.search.suggest.QuerySuggester",
			new String[][] {{"search.engine.impl", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/password-based-authentication-pipelines
		_componentProperties.put(
			"com.liferay.portal.kernel.security.auth.Authenticator", new String[][] {{"key", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.security.permission.BaseModelPermissionChecker",
			new String[][] {{"model.class.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.security.permission.PermissionUpdateHandler",
			new String[][] {{"model.class.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.security.permission.ResourcePermissionChecker",
			new String[][] {{"resource.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/form-navigator
		_componentProperties.put(
			"com.liferay.portal.kernel.servlet.taglib.ui.FormNavigatorCategory",
			new String[][] {{"form.navigator.category.order", "Integer"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/form-navigator
		_componentProperties.put(
			"com.liferay.portal.kernel.servlet.taglib.ui.FormNavigatorEntry",
			new String[][] {{"form.navigator.entry.order", "Integer"}, {"service.ranking", "Integer"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.social.SocialActivityManager",
			new String[][] {{"model.class.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.struts.StrutsAction", new String[][] {{"path", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.struts.StrutsPortletAction", new String[][] {{"path", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.template.TemplateHandler", new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.templateparser.TransformerListener",
			new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.trash.TrashHandler", new String[][] {{"model.class.name", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/liferays-workflow-framework
		_componentProperties.put(
			"com.liferay.portal.kernel.workflow.WorkflowHandler", new String[][] {{"model.class.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.upgrade.UpgradeStep",
			new String[][] {{"upgrade.bundle.symbolic.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.kernel.webdav.WebDAVStorage",
			new String[][] {{"javax.portlet.name", "String"}, {"webdav.storage.token", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.language.LanguageResources", new String[][] {{"language.id", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.verify.VerifyProcess", new String[][] {{"verify.process.name", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.search.analysis.FieldQueryBuilderFactory",
			new String[][] {{"description.fields", "String"}, {"title.fields", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.search.buffer.IndexerRequestBufferExecutor",
			new String[][] {{"buffered.execution.mode", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/customizing-liferay-search
		_componentProperties.put(
			"com.liferay.portal.search.buffer.IndexerRequestBufferOverflowHandler",
			new String[][] {{"mode", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.template.TemplateResourceParser", new String[][] {{"lang.type", "String"}, });

		_componentProperties.put(
			"com.liferay.portal.template.TemplateManager", new String[][] {{"language.type", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/customizing-the-control-menu
		_componentProperties.put(
			"com.liferay.product.navigation.control.menu.ProductNavigationControlMenuEntry",
			new String[][] {
				{"product.navigation.control.menu.category.key", "String"},
				{"product.navigation.control.menu.category.order", "Integer"}
			});

		_componentProperties.put(
			"com.liferay.social.kernel.model.SocialActivityInterpreter",
			new String[][] {{"javax.portlet.name", "String"}, });

		_componentProperties.put(
			"com.liferay.social.kernel.model.SocialRequestInterpreter",
			new String[][] {{"javax.portlet.name", "String"}, });

		//OSGi default commands
		_componentProperties.put(
			"java.lang.Object",
			new String[][] {{"osgi.command.scope", "String"}, {"osgi.command.function", "String"}, });

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/overriding-language-keys
		_componentProperties.put("java.util.ResourceBundle", new String[][] {{"language.id", "String"}, });

		_componentProperties.put(
			"javax.portlet.Portlet",
			new String[][] {
				{"com.liferay.portlet.action-timeout", "int"}, {"com.liferay.portlet.add-default-resource", "boolean"},
				{"com.liferay.portlet.active", "boolean"}, {"com.liferay.portlet.action-url-redirect", "boolean"},
				{"com.liferay.portlet.ajaxable", "boolean"},
				{"com.liferay.portlet.autopropagated-parameters", "String"},
				{"com.liferay.portlet.configuration-path", "String"},
				{"com.liferay.portlet.control-panel-entry-category", "String"},
				{"com.liferay.portlet.control-panel-entry-weight", "double"},
				{"com.liferay.portlet.css-class-wrapper", "String"}, {"com.liferay.portlet.display-category", "String"},
				{"com.liferay.portlet.facebook-integration", "String"},
				{"com.liferay.portlet.footer-portal-css", "String"},
				{"com.liferay.portlet.footer-portlet-css", "String"},
				{"com.liferay.portlet.footer-portal-javascript", "String"},
				{"com.liferay.portlet.footer-portlet-javascript", "String"},
				{"com.liferay.portlet.friendly-url-mapping", "String"},
				{"com.liferay.portlet.friendly-url-routes", "String"},
				{"com.liferay.portlet.header-portal-css", "String"},
				{"com.liferay.portlet.header-portlet-css", "String"},
				{"com.liferay.portlet.header-portal-javascript", "String"},
				{"com.liferay.portlet.header-portlet-javascript", "String"}, {"com.liferay.portlet.icon", "String"},
				{"com.liferay.portlet.instanceable", "boolean"}, {"com.liferay.portlet.layout-cacheable", "boolean"},
				{"com.liferay.portlet.maximize-edit", "boolean"}, {"com.liferay.portlet.maximize-help", "boolean"},
				{"com.liferay.portlet.parent-struts-path", "String"}, {"com.liferay.portlet.pop-up-print", "boolean"},
				{"com.liferay.portlet.preferences-company-wide", "boolean"},
				{"com.liferay.portlet.preferences-owned-by-group", "boolean"},
				{"com.liferay.portlet.preferences-unique-per-layout", "boolean"},
				{"com.liferay.portlet.private-request-attributes", "boolean"},
				{"com.liferay.portlet.private-session-attributes", "boolean"},
				{"com.liferay.portlet.remoteable", "boolean"}, {"com.liferay.portlet.render-timeout", "int"},
				{"com.liferay.portlet.render-weight", "int"},
				{"com.liferay.portlet.requires-namespaced-parameters", "boolean"},
				{"com.liferay.portlet.restore-current-view", "boolean"}, {"com.liferay.portlet.scopeable", "boolean"},
				{"com.liferay.portlet.show-portlet-access-denied", "boolean"},
				{"com.liferay.portlet.show-portlet-inactive", "boolean"},
				{"com.liferay.portlet.single-page-application", "boolean"},
				{"com.liferay.portlet.struts-path", "String"}, {"com.liferay.portlet.system", "boolean"},
				{"com.liferay.portlet.use-default-template", "boolean"},
				{"com.liferay.portlet.user-principal-strategy", "String"},
				{"com.liferay.portlet.virtual-path", "String"}, {"javax.portlet.description", "String"},
				{"javax.portlet.display-name", "String"}, {"javax.portlet.expiration-cache", "int"},
				{"javax.portlet.info.keywords", "String"}, {"javax.portlet.info.short-title", "String"},
				{"javax.portlet.info.title", "String"}, {"javax.portlet.init-param", ""},
				{"javax.portlet.mime-type", "String"}, {"javax.portlet.name", "String"},
				{"javax.portlet.portlet-mode", "String"}, {"javax.portlet.preferences", "String"},
				{"javax.portlet.resource-bundle", "String"}, {"javax.portlet.security-role-ref", "String"},
				{"javax.portlet.supported-processing-event", "String"},
				{"javax.portlet.supported-public-render-parameter", "String"},
				{"javax.portlet.supported-publishing-event", "String"}, {"javax.portlet.window-state", "String"}
			});

		_componentProperties.put(
			"javax.portlet.filter.PortletFilter",
			new String[][] {
				{"javax.portlet.name", "String"}, {"preinitialized.filter", "boolean"}, {"service.id", "String"}
			});

		_componentProperties.put(
			"javax.servlet.Filter",
			new String[][] {
				{"after-filter", "String"}, {"before-filter", "String"}, {"dispatcher", "String"},
				{"init.param", "String"}, {"servlet-context-name", "String"}, {"servlet-filter-name", "String"},
				{"url-pattern", "String"}
			});

		//see https://dev.liferay.com/de/develop/tutorials/-/knowledge_base/7-0/liferay-websocket-whiteboard
		_componentProperties.put(
			"javax.websocket.Endpoint", new String[][] {{"org.osgi.http.websocket.endpoint.path", "String"}, });

		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.util.DDMStructurePermissionSupport",
			new String[][] {
				{"model.class.name", "String"}, {"add.structure.action.id", "String"},
				{"default.model.resource.name", "boolean"}
			});

		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.util.DDMTemplatePermissionSupport",
			new String[][] {
				{"model.class.name", "String"}, {"add.structure.action.id", "String"},
				{"default.model.resource.name", "boolean"}
			});

		_componentProperties.put(
			"com.liferay.adaptive.media.image.optimizer.AMImageOptimizer",
			new String[][] {{"adaptive.media.key", "String"}});

		_componentProperties.put(
			"com.liferay.adaptive.media.image.counter.AMImageCounter",
			new String[][] {{"adaptive.media.key", "String"}});

		_componentProperties.put(
			"com.liferay.adaptive.media.handler.AMRequestHandler",
			new String[][] {{"adaptive.media.handler.pattern", "String"}});

		_componentProperties.put(
			"com.liferay.mentions.matcher.MentionsMatcher", new String[][] {{"model.class.name", "String"}});

		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldValueAccessor",
			new String[][] {{"ddm.form.field.type.name", "String"}});

		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldValueRenderer",
			new String[][] {{"ddm.form.field.type.name", "String"}});

		_componentProperties.put(
			"com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldValueRequestParameterRetriever",
			new String[][] {{"ddm.form.field.type.name", "String"}});

		_componentProperties.put(
			"com.liferay.portal.kernel.captcha.Captcha", new String[][] {{"captcha.engine.impl", "String"}});

		_componentProperties.put(
			"com.liferay.portal.kernel.util.ResourceBundleLoader", new String[][] {{"bundle.symbolic.name", "String"}});

		_componentProperties.put(
			"com.liferay.portal.output.stream.container.OutputStreamContainerFactory",
			new String[][] {{"name", "String"}});

		_componentProperties.put(
			"com.liferay.knowledge.base.web.internal.selector.KBArticleSelector",
			new String[][] {{"model.class.name", "String"}});

		_componentProperties.put(
			"com.liferay.push.notifications.sender.PushNotificationsSender", new String[][] {{"platform", "String"}});

		_componentProperties.put(
			"com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener",
			new String[][] {{"model.class.name", "String"}});

		_componentProperties.put(
			"org.eclipse.osgi.service.urlconversion.URLConverter", new String[][] {{"protocol", "String"}});

		_componentProperties.put(
			"com.liferay.portal.kernel.security.auth.AuthFailure", new String[][] {{"key", "String"}});

		_componentProperties.put(
			"com.liferay.asset.kernel.validator.AssetEntryValidatorExclusionRule",
			new String[][] {{"model.class.name", "String"}});

		_componentProperties.put(
			"com.liferay.portlet.documentlibrary.store.Store", new String[][] {{"store.type", "String"}});

		_componentProperties.put(
			"com.liferay.portlet.documentlibrary.store.StoreWrapper", new String[][] {{"store.type", "String"}});

		_componentProperties.put(
			"com.liferay.portal.kernel.portlet.PortletLayoutFinder", new String[][] {{"model.class.name", "String"}});

		_componentProperties.put(
			"com.liferay.portal.kernel.template.TemplateContextContributor", new String[][] {{"type", "String"}});
	}

}