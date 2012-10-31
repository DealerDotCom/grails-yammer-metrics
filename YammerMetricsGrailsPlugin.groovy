/*
* Copyright 2012 Jeff Ellis / Ellery Crane
*/
class YammerMetricsGrailsPlugin{

    // the plugin version
    def version = "2.1.2-3-DDC"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.6 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views",
            "web-app/**"
    ]

    def author = "Ellery Crane, Jeff Ellis"
    def authorEmail = "ellery.crane@dealer.com, codemonkey@ellises.us"
    def title = "Grails plugin to package Coda Hale's yammer metrics jars"
    def description = '''\\
Provides the following features:
   * metrics-core
   * metrics-servlet (wired to the /metrics end point for the app).

For detailed documentation on the yammer metrics package see:

http://metrics.codahale.com/index.html
'''

    // URL to the plugin's documentation
    def documentation = "http://github.com/jeffellis/grails-yammer-metrics"

    def doWithWebDescriptor = { xml ->
        Boolean metricsEnabled = Boolean.parseBoolean(System.getProperty('metrics.enabled', 'false')) || application.config.metrics.enabled != false
        if(metricsEnabled){
            println("#### YammerMetrics are ENABLED ####")
            def count = xml.'servlet'.size()
            if(count > 0){

                def servletElement = xml.'servlet'[count - 1]

                servletElement + {
                    'servlet' {
                        'servlet-name'("YammerMetrics")
                        'servlet-class'("com.yammer.metrics.reporting.AdminServlet")
                    }
                }
                println "YammerMetrics servlet injected into web.xml"
            }

            count = xml.'servlet-mapping'.size()
            if(count > 0){
                def servletMappingElement = xml.'servlet-mapping'[count - 1]
                servletMappingElement + {

                    'servlet-mapping' {
                        'servlet-name'("YammerMetrics")
                        'url-pattern'("/metrics/*")
                    }
                }
                println "YammerMetrics Admin servlet filter-mapping (for /metrics/*) injected into web.xml"
            }
            println("###################################")
        } else {
            println("#### YammerMetrics are DISABLED ####")
        }
    }

    def doWithSpring = {
        xmlns aop: "http://www.springframework.org/schema/aop"
        customScopeConfigurer(org.springframework.beans.factory.config.CustomScopeConfigurer) {
            scopes = [
                    thread: org.springframework.context.support.SimpleThreadScope
            ]
        }
        stopWatchHolder(com.yammer.metrics.context.StopWatchHolder) { bean ->
            bean.scope = 'thread'
            aop.'scoped-proxy'()
        }
        requestStopWatchManager(com.yammer.metrics.context.RequestStopWatchManager) { bean ->
            bean.scope = 'request'
            bean.initMethod = 'init'
            bean.destroyMethod = 'destroy'
            bean.autowire = 'byName'
            aop.'scoped-proxy'()
        }
        if(application.config.metrics.graphite.host && application.config.metrics.graphite.port && application.config.metrics.graphite.prefix){
            graphiteReporter(com.yammer.metrics.reporting.FilterableGraphiteReporter, application.config.metrics.graphite.host, application.config.metrics.graphite.port, application.config.metrics.graphite.prefix) {bean ->
                bean.initMethod = 'init'
            }
        } else {
            println "Configuration not found for YammerMetrics graphite reporter; skipping initialization"
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
