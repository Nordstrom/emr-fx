package com.nordstrom.amp.emr;

import java.io.IOException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;

/**
 * Pass in your routine using lambdas, like so:
 *
 * ExponentialBackoff.execute(3, () -> getIngestionProcessor().process(payload));
 *
 */
public final class ExponentialBackoff {
    private static final Logger logger = LoggerFactory.getLogger(ExponentialBackoff.class);
    public static final String maxTries = "Max tries exceeded";
    public static final long baseSleepTimeMs = 500;

    @FunctionalInterface
    public interface CheckedFunction<T> {
        T get() throws IOException;
    }

    @FunctionalInterface
    public interface CheckedVoidFunction {
        void apply() throws IOException;
    }

    /**
     * RuntimeException will trigger a backoff (e.g. there was a network error etc).
     *
     * Given the provided lambda, try X times with an exponential backoff.
     * Quit retrying if it errors out or it succeeds.
     *
     * Return false if the passed in lambda failed to succeed within the given number of retries.
     * True if everything was peachy.
     *
     * @param retryAttempts
     * @param fn
     * @return
     * @throws IllegalArgumentException
     *             Any IllegalArgumentException suppresses retries
     * @throws RuntimeException
     *             AmazonServiceException suppresses retries if the cause is an expired token
     */
    public static <T> T execute(int retryAttempts, CheckedFunction<T> fn)
            throws IllegalArgumentException, RuntimeException {
        Random random = new Random();
        int tries = 0;
        Exception coreException;
        do {
            try {
                return fn.get();
            } catch (IllegalArgumentException e) {
                // Illegal arguments will always fail, no point in retrying.
                throw e;
            } catch (AmazonServiceException ex) {
                if (ex.getErrorCode().indexOf("ExpiredToken") > -1) {
                    // Fail if the user is running tests locally without AWSCreds
                    throw ex;
                }
                coreException = ex;
            } catch (Exception ex) {
                coreException = ex;
                logger.warn(String.format("Exception encoutered, %s retry: %s",
                        (tries + 1 < retryAttempts ? "will" : "will NOT"), ex.getMessage()));
            }

            try {
                long sleepTime = (long) ((baseSleepTimeMs * (1 << tries + 1)) * (0.5 + random.nextDouble()));
                Thread.sleep(sleepTime);
            } catch (InterruptedException e1) {
                logger.info("Received interrupted signal during backoff. Proceeding to bail.", e1);
                throw new RuntimeException("Received interrupted signal during backoff.", coreException);
            }
        } while (tries++ < retryAttempts);

        throw new RuntimeException(maxTries, coreException);
    }

    /**
     * @param retryAttempts
     * @param fn
     * @throws IllegalArgumentException
     *             Any IllegalArgumentException suppresses retries
     * @throws RuntimeException
     *             AmazonServiceException suppresses retries if the cause is an expired token
     */
    public static void execute(int retryAttempts, CheckedVoidFunction fn)
            throws IllegalArgumentException, RuntimeException {
        Random random = new Random();
        int tries = 0;
        Exception coreException = null;
        do {
            try {
                fn.apply();
                return;
            } catch (IllegalArgumentException e) {
                // Illegal arguments will always fail, no point in retrying.
                throw e;
            } catch (AmazonServiceException ex) {
                if (ex.getErrorCode().indexOf("ExpiredToken") > -1) {
                    // Fail if the user is running tests locally without AWSCreds
                    throw ex;
                }
                coreException = ex;
            } catch (Exception ex) {
                coreException = ex;
                logger.warn(String.format("Exception encoutered, %s retry: %s",
                        (tries + 1 < retryAttempts ? "will" : "will NOT"), ex.getMessage()));
            }

            try {
                long sleepTime = (long) ((baseSleepTimeMs * (1 << tries + 1)) * (0.5 + random.nextDouble()));
                Thread.sleep(sleepTime);
            } catch (InterruptedException e1) {
                logger.info("Received interrupted signal during backoff. Proceeding to bail.", e1);
                throw new RuntimeException("Received interrupted signal during backoff.", coreException);
            }
        } while (tries++ < retryAttempts);

        throw new RuntimeException(maxTries, coreException);
    }
}

