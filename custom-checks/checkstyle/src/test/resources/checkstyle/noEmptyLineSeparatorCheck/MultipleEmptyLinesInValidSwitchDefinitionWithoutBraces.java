public class MultipleEmptyLinesInValidSwitchDefinitionWithoutBraces {

    // empty line after class is allowed

    public void method() {
        switch (channelId) {
            case CHANNEL_WIND_DIRECTION:
                return new DecimalType(windDirection);
            case CHANNEL_AVG_WIND_SPEED:
                return new DecimalType(avgWindSpeed);

            case CHANNEL_WIND_SPEED:
                return new DecimalType(windSpeed);

            case CHANNEL_TEMPERATURE:
                return new DecimalType(temperature);

            case CHANNEL_CHILL_TEMPERATURE:
                return new DecimalType(chillTemperature);

            case CHANNEL_CHILL_TEMPERATURE: break;
            
            
            default: 
                break;           
            
        }
    }
}
