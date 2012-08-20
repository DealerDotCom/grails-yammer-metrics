package com.yammer.metrics.context

class NestedContextualStopWatch implements StopWatch{
    private static final String NESTED_CONTEXT_TASK_NAME = 'nested'

    private final Deque<ContextualStopWatch> contextStack = new ArrayDeque<ContextualStopWatch>()

    void startContext(String group, String type){
        contextStack.peek()?.start(NESTED_CONTEXT_TASK_NAME)
        ContextualStopWatch ctx = new ContextualStopWatch(group, type)
        contextStack.push(ctx)
    }

    void stopContext(){
        ContextualStopWatch ctx = contextStack.pop()
        contextStack.peek()?.stop(NESTED_CONTEXT_TASK_NAME)
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




