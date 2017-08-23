
```
public class ZWaveDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final static Logger logger = LoggerFactory.getLogger(ZWaveDiscoveryService.class);

        for (ZWaveNode node : controllerHandler.getNodes()) {
            deviceAdded(node);
        }

        // Start the search for new devices
        controllerHandler.startDeviceDiscovery();
    }
```
here must be an empty line but it's not
end