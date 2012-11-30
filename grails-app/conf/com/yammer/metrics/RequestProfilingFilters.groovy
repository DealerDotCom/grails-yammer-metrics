package com.yammer.metrics

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import com.yammer.metrics.http.CountingFilter

class RequestProfilingFilters{
    def requestStopWatchManager
    def stopWatchHolder

    def filters = {
        all(controller: 'error', invert: true, action: '*') {
            before = {
                if(CH?.config?.metrics?.requestProfiling?.enabled){
                    if(!request.getAttribute(CountingFilter.METRICS_GROUP_REQUEST_ATTRIBUTE) &&
                            !request.getAttribute(CountingFilter.METRICS_TYPE_REQUEST_ATTRIBUTE)){
                        request.setAttribute(CountingFilter.METRICS_GROUP_REQUEST_ATTRIBUTE, controllerName)
                        request.setAttribute(CountingFilter.METRICS_TYPE_REQUEST_ATTRIBUTE, actionName)
                    }
                    requestStopWatchManager?.startControllerAction(controllerName, actionName)
                }
            }
            after = {
                if(CH?.config?.metrics?.requestProfiling?.enabled && CH?.config?.metrics?.requestProfiling?.profileViewRendering){
                    stopWatchHolder?.start('view')
                }
            }
            afterView = {
                if(CH?.config?.metrics?.requestProfiling?.enabled){
                    requestStopWatchManager?.stopControllerAction()
                }
            }
        }
    }
}
