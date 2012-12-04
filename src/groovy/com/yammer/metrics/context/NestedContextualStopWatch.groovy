package com.yammer.metrics.context

import com.yammer.metrics.MetricsDictionary

class NestedContextualStopWatch implements StopWatch{

    private final Deque<ContextualStopWatch> contextStack = new ArrayDeque<ContextualStopWatch>()

    private final String rootContext

    NestedContextualStopWatch(String rootCtx){
        this.rootContext = rootCtx
    }

    void startContext(String group, String type){
        contextStack.peek()?.start(MetricsDictionary.NESTED_CONTEXT_TASK_METRIC)
        ContextualStopWatch ctx = new ContextualStopWatch(rootContext, group, type)
        contextStack.push(ctx)
    }

    void stopContext(){
        ContextualStopWatch ctx = contextStack.pop()
        contextStack.peek()?.stop(MetricsDictionary.NESTED_CONTEXT_TASK_METRIC)
        ctx.finish()
    }

    void start(String taskName){
        contextStack.peek()?.start(taskName)
    }

    void stop(String taskName){
        contextStack.peek()?.stop(taskName)
    }

    void finish(){
        while(!contextStack.isEmpty()){
            stopContext()
        }
    }
}




