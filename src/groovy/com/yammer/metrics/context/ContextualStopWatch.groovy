package com.yammer.metrics.context

import com.yammer.metrics.core.Clock
import com.yammer.metrics.core.Timer
import java.util.concurrent.TimeUnit
import com.yammer.metrics.core.Histogram
import com.yammer.metrics.core.Meter
import com.yammer.metrics.Metrics
import com.yammer.metrics.core.MetricName
import com.yammer.metrics.util.RatioGauge

class ContextualStopWatch implements StopWatch{
    final String group
    final String type
    private final Clock clock

    private final Map<String, TaskTimingInfo> tasks

    TaskTimingInfo contextTimingInfo

    ContextualStopWatch(String groupName, String typeName){
        group = groupName
        type = typeName
        clock = Clock.defaultClock()
        tasks = [:]
        contextTimingInfo = new TaskTimingInfo(clock.tick())
    }

    void start(String taskName){
        Long startTime = clock.tick()
        if(tasks.containsKey(taskName)){
            tasks.get(taskName).start(startTime)
        } else {
            tasks.put(taskName, new TaskTimingInfo(startTime))
        }
    }

    void stop(String taskName){
        if(tasks.containsKey(taskName)){
            tasks.get(taskName).stop(clock.tick())
        } else {
            throw new IllegalArgumentException("No task named $taskName is running") //TODO: squelch? do we want to throw an exception for timing info?
        }
    }


    void finish(){
        Long stopTime = clock.tick()
        contextTimingInfo.stop(stopTime)
        tasks.values().each {TaskTimingInfo taskTimingInfo ->
            taskTimingInfo.stopIfRunning(stopTime)
        }
        Long contextTime = contextTimingInfo.getTotalTime()
        Timer contextTimer = getTimer()
        contextTimer.update(contextTime, TimeUnit.NANOSECONDS)

        tasks.each {String taskName, TaskTimingInfo taskTimingInfo ->
            Histogram ratioHistogram = getTaskTimingRatioHistogram(taskName)
            Integer percentage = contextTime == 0I ? -1I : (Integer) (100L * taskTimingInfo.getTotalTime() / contextTime)
            ratioHistogram.update(percentage)
            Meter taskMeter = getTaskMeter(taskName)
            taskMeter.mark(taskTimingInfo.count)
            makeTaskInvocationRatioGauge(taskName, contextTimer, taskMeter)
        }

    }

    private Timer getTimer(){
        return Metrics.newTimer(makeMetricName('request'), TimeUnit.MILLISECONDS, TimeUnit.MINUTES) //TODO: make configurable
    }

    private MetricName makeMetricName(String name){
        return new MetricName(group, type, name)
    }

    private Histogram getTaskTimingRatioHistogram(String taskName){
        return Metrics.newHistogram(makeMetricName([taskName, 'percentageOfTime'].join('.')), true) //TODO: make configurable
    }

    private Meter getTaskMeter(String taskName){
        return Metrics.newMeter(makeMetricName(taskName), 'invocations', TimeUnit.MINUTES) //TODO: make configurable
    }

    private void makeTaskInvocationRatioGauge(String taskName, Timer ctxTimer, Meter taskMeter){
        Metrics.newGauge(makeMetricName([taskName, 'invocationRatio'].join('.')), new TaskInvocationRatio(ctxTimer, taskMeter)) //TODO: make configurable
    }

    private static class TaskInvocationRatio extends RatioGauge{

        private final Timer contextTimer
        private final Meter invocations

        TaskInvocationRatio(Timer ctxTimer, Meter taskInvocationMeter){
            contextTimer = ctxTimer
            invocations = taskInvocationMeter
        }


        @Override
        protected double getNumerator(){
            return invocations.oneMinuteRate()
        }

        @Override
        protected double getDenominator(){
            return contextTimer.oneMinuteRate()
        }
    }


    private static class TaskTimingInfo{
        final List<Long> durations
        private Long startTime

        TaskTimingInfo(final Long startTimeInClockUnits){
            durations = new ArrayList<Long>()
            start(startTimeInClockUnits)
        }

        void stopIfRunning(final Long stopTime){
            if(startTime != null){
                stop(stopTime)
            }
        }

        void stop(final Long stopTime){
            if(startTime == null || stopTime == null){
                throw new IllegalArgumentException("Cannot stop task; either startTime or stopTime is null: startTime:$startTime, stopTime:$stopTime")
            }
            Long duration = stopTime - startTime
            durations << duration
            startTime = null
        }

        void start(final Long start){
            stopIfRunning(start)
            startTime = start
        }

        Integer getCount(){
            return durations.size()
        }

        Long getTotalTime(){
            return (Long) durations.sum()
        }
    }
}