import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ExtendServiceTrackerCustomizer implements ServiceTrackerCustomizer<HttpService, HttpService> {
  @Override
  public HttpService addingService(ServiceReference<HttpService> reference) {
  }

  @Override
  public void modifiedService(ServiceReference <HttpService> reference, Object service) {
  }

  @Override
  public void removedService(ServiceReference <HttpService> reference, Object service) {
  }

}
