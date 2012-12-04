package com.yammer.metrics

import static com.yammer.metrics.MetricsDictionary.*;

class RequestProfilingFilters{
    def requestStopWatchManager
    def stopWatchHolder
    def grailsApplication

    def filters = {
        all(controller: 'error', invert: true, action: '*') {
            before = {
                if(grailsApplication?.config?.metrics?.requestProfiling?.enabled){
                    if(!request.getAttribute(METRICS_GROUP_REQUEST_ATTRIBUTE) &&
                            !request.getAttribute(METRICS_TYPE_REQUEST_ATTRIBUTE)){
                        request.setAttribute(METRICS_GROUP_REQUEST_ATTRIBUTE, controllerName)
                        request.setAttribute(METRICS_TYPE_REQUEST_ATTRIBUTE, actionName)
                    }
                    requestStopWatchManager?.startControllerAction(controllerName, actionName)
                }
            }
            after = {
                if(grailsApplication?.config?.metrics?.requestProfiling?.enabled && grailsApplication?.config?.metrics?.requestProfiling?.profileViewRendering){
                    stopWatchHolder?.start(MetricsDictionary.REQUEST_VIEW_METRIC)
                }
            }
            afterView = {
                if(grailsApplication?.config?.metrics?.requestProfiling?.enabled){
                    requestStopWatchManager?.stopControllerAction()
                }
            }
        }
    }
}
