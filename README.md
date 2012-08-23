Grails Plugin for the Yammer Metrics Package
=======

Provides the following features:
   * metrics-core
   * metrics-servlet

This plugin wires the AdminServlet to the /metrics endpoint in the including application. To disable this functionality,
add the following to the application's Config:
'''
    metrics.servletEnabled = false
'''
For detailed documentation on the yammer metrics package see:

http://metrics.codahale.com/index.html

Annotations
-------
@Timed
This annotation can be added to any method you wish to be timed.  @Timed uses sensible defaults to create an instance of
com.yammer.metrics.core.Timer and the associated code to update it from within your method body.

Before
```
class SomeService{
  public String serve(def foo, def bar) {
     return "OK";
  }
}
```

Timed the Java Way
```
class SomeService{
  private final Timer serveTimer = Metrics.newTimer(SomeService.class, "serveTimer", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
  public String serve(def foo, def bar) {
    final TimerContext context = responses.time();
    try {
        return "OK";
    } finally {
        context.stop();
    }
  }
}
```

Timed the Grails Way
```
class SomeService{
  @com.yammer.metrics.groovy.Timed
  public String serve(def foo, def bar) {
    return "OK"
  }
}
```

@Metered
This annotation can be added to any method you wish to be metered.  @Metered uses sensible defaults to create an instance of
com.yammer.metrics.core.Meter and the associated code to update it from within your method body.


Before
```
class SomeService{
  public String serve(def foo, def bar) {
    return "OK";
  }
}
```

Metered the Java Way
```
class SomeService{
  private final Meter serveMeter = Metrics.newMeter(SomeService.class, "serveMeter", "serveMeter", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
  public String serve(def foo, def bar) {
    serveMeter.mark();
    return "OK";
  }
}
```

Metered the Grails Way
```
class SomeService{

  @com.yammer.metrics.groovy.Meter
  public String serve(def foo, def bar) {
    return "OK"
  }
}
```

Note: Annotations can be safely combined.

Request Profiling
-------
Metrics on request durations and rates are generated automatically based on the properties defined in the application's Config.groovy.
These metrics are generated at the controller/action level. It is possible to add additional request profiling metrics by making use of the
'''stopWatchHolder''' spring bean that can be injected into controllers/services/taglibs/etc via standard Grails dependency injection.  This
bean has 'start' and 'stop' methods that take in task names, and track metrics on those tasks within the scope of the controller/action.

For instance, you may wish to track the number and duration of database calls that a particular controller/action makes. You can do so with something
like the following:

```
class SomeService{
    def stopWatchHolder
    def dataService

    def getData(){
        stopWatchHolder.start('databaseQuery')
        def data = dataService.getData()
        stopWatchHolder.stop('databaseQuery')
        return data
    }
}
```

Metrics on the 'databaseQuery' task will now be kept relative to the controller/action. Calling it from the 'book/show' controller action might yield
request profiling metrics such as:

```
book.show.request.mean                                  //mean request time in milliseconds
book.show.request.1MinuteRate                           //weighted moving average rate of requests for this controller/action for the last minute
book.show.databaseQuery.meanPercentageOfRequestTime     //average percentage of the total request time that was spent performing this task
book.show.databaseQuery.invocationRatio                 //the ratio between the rate of requests to this controller actions and the rate of invocations of this task;
                                                        //1.0 implies that you make one invocation per request, 2.0 would mean you make two per request, etc
```

It is possible to enable similar 'task' metrics for rendering the view for a controller/action by enabling a config property.

Nested controller/action calls within a single request (such as by using the g:include tag, redirecting, etc) are tracked independently, with the standard
invocationRatio/meanPercentageOfRequestTime metrics appearing for the 'nested' task for the original controller/action.

Publishing Metrics to Graphite
-------
This plugin extends the com.yammer.metrics.reporting.GraphiteReporter to allow for fine grained, configurable publishing of metrics to a graphite server.

Based on properties in the application's Config.groovy, a GraphiteReporter instance will be initialized on app startup to publish metrics that match
various user configured criteria.  Explanations of these config properties and their effects are detailed below, in an example Config.groovy file.

Much of the graphite filtering rules and metrics generation can be configured at runtime by the use of a plugin-created MBean: com.yammer.metrics.MetricsConfigService
This allows you to disable reporting, tweak what metrics are reported and so on by using the Mbean attributes and managed operations.



Example config:
```
metrics {
    enabled = true // if false, no metrics will be automatically tracked by this plugin. User created metrics (by making use of the Yammer Metrics API) are unaffected
    requestProfiling {
        enabled = true // if false, controller/action metrics will not be automatically generated for the application
        profileViewRendering = false // if true, metrics for the view rendering portion of each request will be tracked
    }
    graphite {
        enabled = true // if false, no reporting to graphite will occur
        host = 'graphite.example.com' // the host of the graphite server to publish to
        port = 2003 // the port of the graphite server to publish to
        prefix = 'com.example.application' // the prefix to use for all metrics published to the graphite server
        period = 1  // period to use in conjunction with the timeUnit config value to control how frequently data is reported to graphite
        timeUnit = "MINUTES" //java.util.concurrent.TimeUnit string value; combined with period determines report frequency
        rewrite = [ // metric name rewrite rules. All metric name Strings will replace all instances of each key in this map with its value
                'percentageOfTime.mean': 'meanPercentageOfRequestTime',
                'invocationRatio.value': 'invocationRatio'
        ]
        filters { // all filter patterns use the Ant path matching syntax, with periods as the directory separator
            blacklist = ['debug.**', 'admin.**'] // metric name patterns that should never be reported.
            whitelist = ['**.request.1MinuteRate', '**.request.mean'] // metrics must match a whitelist pattern in order to be reported to graphite
            detailed {
                qualifyingPatterns = ['book.show.**', 'author.**'] // metrics that match these patterns will use the 'detailed.whitelist' filter instead of the
                whitelist = [                                      // regular whitelist filter. This allows you to specify particular controller/actions
                        '**.request.1MinuteRate',                  // that you want additional reporting on without needing to spam graphite with more metrics
                        '**.request.mean',                         // for everything
                        '**.request.median',
                        '**.request.max',
                        '**.request.stddev',
                        '**.meanPercentageOfRequestTime',
                        '**.invocationRatio'
                ]
            }
        }
    }
}
```



TODO
-------
 * Support more of the underlying metric parameters via annotations
 * Annotations to support other yammer metrics
 * Automatic controller or service metrics
 * Some gui component (different plugin?)


License
-------

This plugin is
 Copyright (c) 2012 Jeff Ellis

 Published under Apache Software License 2.0, see LICENSE

The metrics jars are:
 Copyright (c) 2010-2012 Coda Hale, Yammer.com

 Published under Apache Software License 2.0
