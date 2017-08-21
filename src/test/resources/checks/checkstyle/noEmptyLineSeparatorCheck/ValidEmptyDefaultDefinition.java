package org.openhab.binding.satel.internal;

import org.openhab.binding.satel.command.SatelCommand;

public class SatelBinding  {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendCommand(SatelCommand command) {
        while (!interrupted) {
            // wait for command state change
            try {
                synchronized (command) {
                    command.wait(this.satelModule.getTimeout());
                }
            } catch (InterruptedException e) {
                // ignore, we will leave the loop on next interruption state check
                interrupted = true;
            }
            // check current state
            switch (command.getState()) {
                case SUCCEEDED:
                    return true;
                case FAILED:
                    return false;
                default:
                    // wait for next change unless interrupted
            }
        }
        return false;
    }
}
