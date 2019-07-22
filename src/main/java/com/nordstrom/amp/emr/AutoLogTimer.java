package com.nordstrom.amp.emr;


public class AutoLogTimer implements AutoCloseable {
    private long startTime;
    private org.slf4j.Logger oldLogger;
    private Logger logger;
    private String msg;

    public AutoLogTimer(org.slf4j.Logger logger, String msg) {
        startTime = System.nanoTime();
        oldLogger = logger;
        this.msg = msg;
    }

    public AutoLogTimer(Logger logger, String msg) {
        startTime = System.nanoTime();
        this.logger = logger;
        this.msg = msg;
    }

    @Override
    public void close() {
        try {
            long stopTime = System.nanoTime();

            long nanoSeconds = stopTime - startTime;
            long milliSeconds = nanoSeconds / 1000000;

            if (oldLogger != null) {
                oldLogger.info("{} : Elapsed {} milliseconds", msg, milliSeconds);
            } else {
                logger.withDuration(milliSeconds)
                        .info("Elapsed : " + msg);
            }
        } catch (Exception ex) {
            // Just eat the exception if anything happens
        }
    }
}
