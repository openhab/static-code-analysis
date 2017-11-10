public class ScheduleWithFixedDelayIsOkay {
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
    public void scheduleService() {
        executor.scheduleWithFixedDelay(new Runnable() {
            int count = 0;
            
            @Override
            public void run() {
                ++count;
                if(count>2) {
                    executor.shutdown();
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }
}
