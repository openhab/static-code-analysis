package org.openhab.binding.test.handler;

import com.configurations.TestConfiguration;

public class TestHandler extends BaseThingHandler {
    @Override
    public void initialize() {
        
    }

    private void startAutomaticRefresh() {               
    }

    @Override
    public void dispose() {
    }

    public void updateChannel(String channelId, TestConfiguration configuration) {
			TestHandlerFactory factory;
    }
}