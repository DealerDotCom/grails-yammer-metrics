package com.yammer.metrics.context

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import com.yammer.metrics.MetricsDictionary

/**
 * Request scoped bean that sets up and tears down the thread scoped stopWatchHolder used for grails request metrics.
 */
class RequestStopWatchManager implements Serializable{
    private static final Log log = LogFactory.getLog(RequestStopWatchManager)

    StopWatchHolder stopWatchHolder

    void init(){
        try{
            if(CH?.config?.metrics?.requestProfiling?.enabled){
                StopWatch requestStopWatch = new NestedContextualStopWatch(MetricsDictionary.REQUEST_METRIC)
                stopWatchHolder?.setStopWatch(requestStopWatch)
            }
        } catch(Exception e){
            log?.error("Exception when trying to init RequestStopWatchManager", e)
        }
    }

    void destroy(){
        try{
            if(CH?.config?.metrics?.requestProfiling?.enabled){
                stopWatchHolder?.clear()
            }
        } catch(Exception e){
            log?.error("Exception when trying to destroy RequestStopWatchManager", e)
        }
    }

    void startControllerAction(String controller, String action){
        try{
            if(CH?.config?.metrics?.requestProfiling?.enabled && controller && action){
                StopWatch requestStopWatch = stopWatchHolder?.getStopWatch()
                if(requestStopWatch instanceof NestedContextualStopWatch){
                    ((NestedContextualStopWatch) requestStopWatch).startContext(controller, action)
                }
            }
        } catch(Exception e){
            log?.error("Exception when trying to start controller/action stopwatch for: $controller/$action".toString(), e)
        }
    }

    void stopControllerAction(){
        try{
            if(CH?.config?.metrics?.requestProfiling?.enabled){
                StopWatch requestStopWatch = stopWatchHolder?.getStopWatch()
                if(requestStopWatch instanceof NestedContextualStopWatch){
                    ((NestedContextualStopWatch) requestStopWatch).stopContext()
                }
            }
        } catch(Exception e){
            log?.error("Exception when trying to stop controller/action stopwatch", e)
        }
    }
}
