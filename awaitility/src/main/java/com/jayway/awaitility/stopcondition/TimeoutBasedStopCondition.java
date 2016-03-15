package com.jayway.awaitility.stopcondition;

import com.jayway.awaitility.Duration;
import com.jayway.awaitility.pollinterval.PollInterval;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class TimeoutBasedStopCondition implements StopCondition {
    private Duration timeout;
    private Duration pollDelay;
    private PollInterval pollInterval;

    public TimeoutBasedStopCondition(Duration timeout, Duration pollDelay, PollInterval pollInterval) {
        this.timeout = timeout;
        this.pollDelay = pollDelay;
        this.pollInterval = pollInterval;
        validateCondition();
    }

    public Duration await() {
//        Future<?> future = executor.submit(new ConditionPoller(pollInterval));
//        if (timeout == Duration.FOREVER) {
//            future.get();
//        } else {
//            future.get(timeout.getValue(), timeout.getTimeUnit());
//        }
        return timeout;
    }

    public void validateCondition() {
        if (pollDelay.isForever()) {
            throw new IllegalArgumentException("Cannot delay polling forever");
        }
        final long timeoutInMS = timeout.getValueInMS();
        if (!timeout.isForever() && timeoutInMS <= pollDelay.getValueInMS()) {
            throw new IllegalStateException(String.format("Timeout (%s %s) must be greater than the poll delay (%s %s).",
                    timeout.getValue(), timeout.getTimeUnitAsString(), pollDelay.getValue(), pollDelay.getTimeUnitAsString()));
        } else if ((!pollDelay.isForever() && !timeout.isForever()) && timeoutInMS <= pollDelay.getValueInMS()) {
            throw new IllegalStateException(String.format("Timeout (%s %s) must be greater than the poll delay (%s %s).",
                    timeout.getValue(), timeout.getTimeUnitAsString(), pollDelay.getValue(), pollDelay.getTimeUnitAsString()));
        }
    }

    public void await(Future future) throws InterruptedException, ExecutionException, TimeoutException {
        if (timeout == Duration.FOREVER) {
            future.get();
        } else {
            future.get(timeout.getValue(), timeout.getTimeUnit());
        }
    }

    public boolean await(CountDownLatch latch) throws InterruptedException {
        if (timeout == Duration.FOREVER) {
            latch.await();
            return true;
        } else if (timeout == Duration.SAME_AS_POLL_INTERVAL) {
            throw new IllegalStateException("Cannot use 'SAME_AS_POLL_INTERVAL' as maximum wait time.");
        } else {
            return latch.await(timeout.getValue(), timeout.getTimeUnit());
        }
    }
}
