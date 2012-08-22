package com.yammer.metrics.context

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class RequestStopWatchManager implements Serializable{

    StopWatchHolder stopWatchHolder

    void init(){
        if(CH?.config?.metrics?.requestProfiling?.enabled){
            StopWatch requestStopWatch = new NestedContextualStopWatch()
            stopWatchHolder?.setStopWatch(requestStopWatch)
        }
    }

    void destroy(){
        if(CH?.config?.metrics?.requestProfiling?.enabled){
            stopWatchHolder?.clear()
        }
    }

    void startControllerAction(String controller, String action){
        if(CH?.config?.metrics?.requestProfiling?.enabled){
            StopWatch requestStopWatch = stopWatchHolder?.getStopWatch()
            if(requestStopWatch instanceof NestedContextualStopWatch){
                ((NestedContextualStopWatch) requestStopWatch).startContext(controller, action)
            }
        }
    }

    void stopControllerAction(){
        if(CH?.config?.metrics?.requestProfiling?.enabled){
            StopWatch requestStopWatch = stopWatchHolder?.getStopWatch()
            if(requestStopWatch instanceof NestedContextualStopWatch){
                ((NestedContextualStopWatch) requestStopWatch).stopContext()
            }
        }
    }
}
