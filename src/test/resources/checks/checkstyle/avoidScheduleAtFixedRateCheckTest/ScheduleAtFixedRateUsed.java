public class TestExample {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    
    public static void scheduleService() {
        final Runnable beeper = new Runnable() {
            public void run() { System.out.println("beep"); }
          };
          
        final ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
    }
}
