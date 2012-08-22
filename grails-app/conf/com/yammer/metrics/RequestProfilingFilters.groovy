package com.yammer.metrics

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class RequestProfilingFilters{
    def requestStopWatchManager
    def stopWatchHolder

    def filters = {
        all(controller: 'error', invert: true, action: '*') {
            before = {
                if(CH?.config?.metrics?.requestProfiling?.enabled){
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
