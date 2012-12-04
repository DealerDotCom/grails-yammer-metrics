package com.yammer.metrics.context

import com.yammer.metrics.core.Clock
import com.yammer.metrics.core.Timer
import java.util.concurrent.TimeUnit
import com.yammer.metrics.core.Histogram
import com.yammer.metrics.core.Meter
import com.yammer.metrics.Metrics
import com.yammer.metrics.core.MetricName
import com.yammer.metrics.util.RatioGauge
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.yammer.metrics.core.Counter
import com.yammer.metrics.MetricsDictionary

class ContextualStopWatch implements StopWatch{
    private static final Log log = LogFactory.getLog(ContextualStopWatch)
    private static final String DEFAULT_GROUP_NAME = 'DEFAULT'
    private static final String DEFAULT_TYPE_NAME = 'DEFAULT'

    final String group
    final String type
    private final Clock clock

    private final Map<String, TaskTimingInfo> tasks
    private final String rootContext

    TaskTimingInfo contextTimingInfo

    ContextualStopWatch(String rootContext, String groupName, String typeName){
        this.rootContext = rootContext
        group = groupName ?: DEFAULT_GROUP_NAME
        type = typeName ?: DEFAULT_TYPE_NAME
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
            log?.error("No task named $taskName is running".toString())
        }
    }


    void finish(){
        try{
            Long stopTime = clock.tick()
            contextTimingInfo.stop(stopTime)
            tasks.values().each {TaskTimingInfo taskTimingInfo ->
                taskTimingInfo.stopIfRunning(stopTime)
            }
            Long contextTime = contextTimingInfo.getTotalTime()
            Timer contextTimer = getTimer()
            contextTimer.update(contextTime, TimeUnit.NANOSECONDS)
            Counter contextTotal = getTotalCounter()
            contextTotal.inc(contextTime)

            tasks.each {String taskName, TaskTimingInfo taskTimingInfo ->
                Histogram ratioHistogram = getTaskTimingRatioHistogram(taskName)
                Integer percentage = contextTime == 0I ? -1I : (Integer) (100L * taskTimingInfo.getTotalTime() / contextTime)
                ratioHistogram.update(percentage)
                Meter taskMeter = getTaskMeter(taskName)
                taskMeter.mark(taskTimingInfo.count)
                makeTaskInvocationRatioGauge(taskName, contextTimer, taskMeter)
                Counter taskTotal = getTaskTotalCounter(taskName)
                taskTotal.inc(taskTimingInfo.getTotalTime())
            }
        } catch(Exception e){
            log?.error("Exception encountering when trying to execute finish() method with group: $group and type: $type:".toString(), e)
        }
    }

    private Timer getTimer(){
        return Metrics.newTimer(makeMetricName(rootContext), TimeUnit.MILLISECONDS, TimeUnit.MINUTES)
    }

    private Counter getTotalCounter(){
        return Metrics.newCounter(makeMetricName([rootContext, MetricsDictionary.TOTAL_METRIC_SUFFIX].join('.')))
    }

    private MetricName makeMetricName(String name){
        return new MetricName(group, type, name)
    }

    private Histogram getTaskTimingRatioHistogram(String taskName){
        return Metrics.newHistogram(makeMetricName([taskName, MetricsDictionary.PERCENTAGE_OF_TIME_METRIC_SUFFIX].join('.')), true)
    }

    private Meter getTaskMeter(String taskName){
        return Metrics.newMeter(makeMetricName(taskName), MetricsDictionary.INVOCATIONS_METRIC_SUFFIX, TimeUnit.MINUTES)
    }

    private void makeTaskInvocationRatioGauge(String taskName, Timer ctxTimer, Meter taskMeter){
        Metrics.newGauge(makeMetricName([taskName, MetricsDictionary.INVOCATION_RATIO_METRIC_SUFFIX].join('.')), new TaskInvocationRatio(ctxTimer, taskMeter))
    }

    private Counter getTaskTotalCounter(String taskName){
        return Metrics.newCounter(makeMetricName([taskName, MetricsDictionary.TOTAL_METRIC_SUFFIX].join('.')))
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