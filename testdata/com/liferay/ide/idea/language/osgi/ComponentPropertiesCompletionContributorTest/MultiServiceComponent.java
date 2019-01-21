import org.osgi.service.component.annotations.Component;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.search.IndexerPostProcessor;

@Component(
    property = {
        "<caret>"
    },
    service = {MVCActionCommand.class, IndexerPostProcessor.class}
)
public class MultiServiceComponent {
}