import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.codehaus.groovy.grails.commons.GrailsServiceClass
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod
import com.yammer.metrics.Metrics
import java.util.concurrent.TimeUnit
import com.yammer.metrics.core.TimerContext

/*
* Copyright 2012 Jeff Ellis
*/
class YammerMetricsGrailsPlugin {

	// the plugin version
    def version = "2.1.2-2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0.3 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views",
            "web-app/**"
    ]

    // TODO Fill in these fields
    def author = "Jeff Ellis"
    def authorEmail = "codemonkey@ellises.us"
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


    def observe = ["controllers", "services"]


    def doWithWebDescriptor = { xml ->

        if(application.config.metrics.servletEnabled!=false){
            def count = xml.'servlet'.size()
            if(count > 0) {

                def servletElement = xml.'servlet'[count - 1]

                servletElement + {
                    'servlet' {
                        'servlet-name'("YammerMetrics")
                        'servlet-class'("com.yammer.metrics.reporting.AdminServlet")
                    }
                }
                println "***\nYammerMetrics servlet injected into web.xml"
            }

            count = xml.'servlet-mapping'.size()
            if(count > 0) {
                def servletMappingElement = xml.'servlet-mapping'[count - 1]
                servletMappingElement + {

                    'servlet-mapping' {
                        'servlet-name'("YammerMetrics")
                        'url-pattern'("/metrics/*")
                    }
                }
                println "YammerMetrics Admin servlet filter-mapping (for /metrics/*) injected into web.xml\n***"
            }
        } else{
            println "Skipping YammerMetrics Admin servlet mapping\n***"
        }
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
        application.controllerClasses.each { controller ->
            instrumentController(controller)
        }

        application.serviceClasses.each { service ->
            instrumentService(service)
        }
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
        if( application.isControllerClass(event.source) ) {
            instrumentController(event.source)
        } else if( application.isServiceClass(event.source) ) {
            instrumentService(event.source)
        } else {
            println "Unable to handle change for $event.source"
        }
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }


    String resolveArtifactName(artifact) {
        if( application.isControllerClass(artifact) ) {
            application.getArtefactType()
        } else if( application.isServiceClass(artifact) ) {

        } else if( application.isArtefact(artifact) ) {

        }
    }

    def instrumentController(controller) {
        println "Controller to instrument is: ${controller}" // interceptor is: ${controller.getBeforeInterceptor()}"

        //controller.metaClass.setProperty(controller, GrailsControllerClass.BEFORE_INTERCEPTOR, { println "Before" } )


        //controller.metaClass.setProperty(controller, GrailsControllerClass.AFTER_INTERCEPTOR, { println "After" } )
    }

    /**
     * Instrument a service class for performance tracing. This will delegate to any other meta-methods that have been defined first.
     * @param service The GrailsServiceClass instance to instrument
     */
    void instrumentService(service) {

        println "Service to instrument is: ${service}"


        //TODO: Determine how we are going to generate appropriate MetricNames or if we are just going to do classes
        // The PITA is that doWithDynamicMethods passing GrailsControllerClass and GrailsServiceClass but the onChange handler passes the actual class
        // instance so name resolution is a bit of a PITA...
        // Probably want to define a bean or a closure so that someone can override it down the road.
        //def serviceName = //service.getSimpleName()

        // See: http://redpointtech.blogspot.com/2010/03/grails-intercepting-service-class.html
        def existingMetaInvokeMethod = service.metaClass.invokeMethod
        service.metaClass.invokeMethod = { String name, args ->

            final MetaMethod metaMethod = locateMetaMethod(delegate, name, args)

            final timerMetric = Metrics.newTimer( delegate.class, name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS )
            final TimerContext context = timerMetric.time();
            try {
                return executeMetaMethod(delegate, metaMethod, name, args, true)
            }
            //TODO: Log exceptions w/ count or timers.
            finally {
                context.stop();
            }
        }
    }

    /**
     * Find the appropriate MetaMethod to invoke.
     *
     * Based upon https://svn.codehaus.org/grails-plugins/grails-grails-melody/trunk/GrailsMelodyGrailsPlugin.groovy
     * @param delegate
     * @param methodName
     * @param args
     * @return
     */
    MetaMethod locateMetaMethod(delegate, methodName, args) {
        MetaMethod existingMetaMethod = delegate.metaClass.getMetaMethod(methodName, args)

        //TODO: Should we look to the class' metaClass?

        if( !existingMetaMethod )  {
            // An existing name match (the first one..., TODO: might try to find a better one...)
            existingMetaMethod = delegate.getClass().getMethods()?.find() { it -> it.name == methodName }

            // Properties
            if( !existingMetaMethod && delegate.metaClass.properties.find { it.name == methodName } ) {
                def property = delegate."${methodName}"
                if( property && property instanceof Closure) {
                    existingMetaMethod = metaMethod = [doMethodInvoke: {dlg, arguments-> property.call(arguments)}]
                }
            }
        }

        existingMetaMethod
    }

    def executeMetaMethod(delegate, metaMethod, methodName, args, callMethodMissing = false) {
        if( !metaMethod ) {
            // TODO: Should we instrument methodMissing?
            return delegate.metaClass.invokeMissingMethod(delegate, methodName, args)
        } else if( metaMethod instanceof ClosureMetaMethod ) {
            // Necessary to avoid "java.lang.IllegalArgumentException: wrong number of arguments" with other call types.
            Closure cloned = (Closure) metaMethod.getClosure().clone();
            cloned.setDelegate(delegate);
            return cloned.call([methodName, args]);

        } else if( metaMethod instanceof MetaMethod ) {
            return metaMethod.doMethodInvoke(delegate, args)
        } else {
            throw new IllegalStateException("Unable to invoke $method on $delegate with $args")
        }
    }

    //TODO: GORM Named queries and other GORM queries?
    //TODO: Datasource hold times
    //TODO: SQL Statement times
    //TODO: HttpClient times

    //TODO: RestTemplate, RestClient times.
    //TODO: ThreadPool sizes?

}
