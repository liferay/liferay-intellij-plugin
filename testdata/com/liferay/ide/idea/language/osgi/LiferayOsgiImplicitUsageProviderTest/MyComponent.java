import org.osgi.service.component.annotations.Reference;

public class <warning descr="Class 'MyComponent' is never used">MyComponent</warning> {

    @Reference
    private org.osgi.service.component.annotations.ConfigurationPolicy <warning descr="Private field 'foo' is assigned but never accessed">foo</warning>;

}