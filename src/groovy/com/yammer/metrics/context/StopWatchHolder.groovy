package com.yammer.metrics.context

class StopWatchHolder implements Serializable, StopWatch{
    StopWatch stopWatch

    void clear(){
        finish()
        stopWatch = null
    }

    @Override
    void start(String taskName){
        stopWatch?.start(taskName)
    }

    @Override
    void stop(String taskName){
        stopWatch?.stop(taskName)
    }

    @Override
    void finish(){
        stopWatch?.finish()
    }
}
