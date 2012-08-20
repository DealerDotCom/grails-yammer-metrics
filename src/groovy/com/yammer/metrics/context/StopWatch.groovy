package com.yammer.metrics.context

public interface StopWatch {
    void start(String taskName)
    void stop(String taskName)
    void finish()
}