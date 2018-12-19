<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<liferay-ui:search-container>
    <liferay-ui:search-container-row className="com.liferay.ide.model.MyModel">
        <liferay-ui:search-container-column-text name="foo" property="<caret>" />
    </liferay-ui:search-container-row>
</liferay-ui:search-container>

