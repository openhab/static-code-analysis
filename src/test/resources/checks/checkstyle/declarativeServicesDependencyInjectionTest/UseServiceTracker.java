import org.osgi.util.tracker.ServiceTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.service.http.HttpService;

public class UseServiceTracker implements BundleActivator {

  static ServiceTracker serviceTracker;
  org.osgi.util.tracker.ServiceTracker serviceTrackerFullName;

  @Override
  public void start(BundleContext context) throws Exception {
    serviceTracker = new ServiceTracker(context, HttpService.class.getName(), this);
    serviceTracker.open();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    serviceTracker.close();

  }
}
