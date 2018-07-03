package checkstyle.nullAnnotationsCheckTest;

import javax.ws.rs.ext.Provider;
import org.osgi.service.component.annotations.Component;

@Provider
@Component(immediate = true, service = SatisfiableResourceFilter.class)
public class SatisfiableResourceFilter implements ContainerRequestFilter {
    
}
