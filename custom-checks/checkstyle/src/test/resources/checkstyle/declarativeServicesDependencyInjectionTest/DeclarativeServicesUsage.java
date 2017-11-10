import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

public class DeclarativeServicesUsage {
  public void setLog(LogService l) {
    log = l;
    System.out.println("Log service is available!");
  }

  public void unsetLog(LogService l) {
    log = null;
    System.out.println("Log service isn`t available anymore!");
  }
}
