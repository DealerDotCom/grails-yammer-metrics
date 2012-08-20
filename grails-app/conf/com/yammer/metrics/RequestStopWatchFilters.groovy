package com.yammer.metrics

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class RequestStopWatchFilters{
    def requestStopWatchManager
    def stopWatchHolder

    def filters = {
        all(controller: 'error', invert: true, action: '*') {
            before = {
                requestStopWatchManager?.startControllerAction(controllerName, actionName)
            }
            after = {
                if(CH?.config?.metrics?.timeViewGeneration){
                    stopWatchHolder?.start('view')
                }
            }
            afterView = {
                requestStopWatchManager?.stopControllerAction()
            }
        }
    }
}
