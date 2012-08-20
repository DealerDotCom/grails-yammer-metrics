package com.yammer.metrics.context

class RequestStopWatchManager implements Serializable{

    StopWatchHolder stopWatchHolder

    void init(){
        StopWatch requestStopWatch = new NestedContextualStopWatch()
        stopWatchHolder?.setStopWatch(requestStopWatch)
    }

    void destroy(){
        stopWatchHolder?.clear()
    }

    void startControllerAction(String controller, String action){
        StopWatch requestStopWatch = stopWatchHolder?.getStopWatch()
        if(requestStopWatch instanceof NestedContextualStopWatch){
            ((NestedContextualStopWatch) requestStopWatch).startContext(controller, action)
        }
    }

    void stopControllerAction(){
        StopWatch requestStopWatch = stopWatchHolder?.getStopWatch()
        if(requestStopWatch instanceof NestedContextualStopWatch){
            ((NestedContextualStopWatch) requestStopWatch).stopContext()
        }
    }
}
