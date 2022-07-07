import org.osgi.service.component.annotations.Component;
import com.liferay.portal.kernel.service.ServiceWrapper;

@Component(
        immediate = true,
        service = ServiceWrapper.class
)
public class MyServiceWrapper implements ServiceWrapper {

    public MyServiceWrapper() {
    }

    public MyServiceWrapper(Object wrappedService) {
        System.out.println(wrappedService);
    }

}