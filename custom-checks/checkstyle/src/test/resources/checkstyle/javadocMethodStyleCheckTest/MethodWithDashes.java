package checks.checkstyle.javadocCommentMethodCheckTest;

public class MethodWithDashes {
    
    /** 
     * @param watchService - the watch service, providing the watch events for the watched directory.
     * @param toWatch - the directory being watched by the watch service
     * @param registredWatchKeys - a mapping between the registered directories and their {@link WatchKey registration keys}.
     * @return the concrete queue reader
     */
     protected void buildWatchQueueReader(WatchService watchService,
     Path toWatch, Map<WatchKey, Path> registredWatchKeys) {

     }
}
