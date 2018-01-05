package checks.checkstyle.javadocMethodCheckTest;

public class MethodWithParamNameAndDescriptionNewLine {
    
    /**
     * Build channel ID for a control, based on control's UUID, thing's UUID and index of the channel for the control
     *
     * @param control 
     *            control to build the channel ID for
     * @param index 
     *            index of a channel within control (0 for primary channel) all indexes greater than 0 will have -index added to the channel ID
     * @return
     *         channel ID for the control and index
     */
    private ChannelUID getChannelIdForControl(LxControl control, int index) {
        
    }
}
