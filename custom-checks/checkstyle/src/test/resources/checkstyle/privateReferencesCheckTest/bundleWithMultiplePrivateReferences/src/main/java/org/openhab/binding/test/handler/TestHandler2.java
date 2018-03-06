package org.openhab.binding.test.handler;

import org.openhab.binding.test.internal.TestConfiguration;
import org.openhab.binding.test.internal.TestHandlerFactory;

public class TestHandler extends BaseThingHandler {
    @Override
    public void initialize() {
        
    }

    private void startAutomaticRefresh() {               
    }

    @Override
    public void dispose() {
    }

    public void updateChannel(String channelId, TestConfiguration configuration, TestHandlerFactory factory) {
			
    }
	
	public void something(TestHandlerFactory factory) {
		
	}
 }