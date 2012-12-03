package com.yammer.metrics

import com.yammer.metrics.http.CountingFilter

class RequestProfilingFilters{
    def requestStopWatchManager
    def stopWatchHolder  
    def grailsApplication

    def filters = {
        all(controller: 'error', invert: true, action: '*') {
            before = {
                if(grailsApplication?.config?.metrics?.requestProfiling?.enabled){
                    if(!request.getAttribute(CountingFilter.METRICS_GROUP_REQUEST_ATTRIBUTE) &&
                            !request.getAttribute(CountingFilter.METRICS_TYPE_REQUEST_ATTRIBUTE)){
                        request.setAttribute(CountingFilter.METRICS_GROUP_REQUEST_ATTRIBUTE, controllerName)
                        request.setAttribute(CountingFilter.METRICS_TYPE_REQUEST_ATTRIBUTE, actionName)
                    }
                    requestStopWatchManager?.startControllerAction(controllerName, actionName)
                }
            }
            after = {
                if(grailsApplication?.config?.metrics?.requestProfiling?.enabled && grailsApplication?.config?.metrics?.requestProfiling?.profileViewRendering){
                    stopWatchHolder?.start('view')
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
