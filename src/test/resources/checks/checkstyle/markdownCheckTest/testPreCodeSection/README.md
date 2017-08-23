here is some text
and before code section
must be an empty line here

```
public class ZWaveDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final static Logger logger = LoggerFactory.getLogger(ZWaveDiscoveryService.class);

    private ZWaveControllerHandler controllerHandler;
    private DiscoveryServiceCallback discoveryServiceCallback;

    public ZWaveDiscoveryService(ZWaveControllerHandler coordinatorHandler, int searchTime) {
        super(searchTime);
        this.controllerHandler = coordinatorHandler;
    }
```


```
    public void activate() {
        logger.debug("Activating ZWave discovery service for {}", controllerHandler.getThing().getUID());
    }
	
    @Override
    public void deactivate() {
        logger.debug("Deactivating ZWave discovery service for {}", controllerHandler.getThing().getUID());
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }
```

break here...
```
    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ZWaveConfigProvider.getSupportedThingTypes();
    }

    @Override
    public void startScan() {
        logger.debug("Starting ZWave inclusion scan for {}", controllerHandler.getThing().getUID());

        // Add all existing devices
        for (ZWaveNode node : controllerHandler.getNodes()) {
            deviceAdded(node);
        }

        // Start the search for new devices
        controllerHandler.startDeviceDiscovery();
    }
```

