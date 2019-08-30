package org.openhab.binding.test.handler;

import org.openhab.binding.test.internal.TestConfiguration;

public class TestHandler extends BaseThingHandler {
    @Override
    public void initialize() {
        
    }

    private void startAutomaticRefresh() {               
    }

    @Override
    public void dispose() {
    }

    protected void updateChannel(String channelId, TestConfiguration configuration) {
			TestHandlerFactory factory;
    }
}