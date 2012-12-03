package com.yammer.metrics.reporting

import com.yammer.metrics.core.MetricsRegistry
import com.yammer.metrics.core.MetricPredicate

import com.yammer.metrics.core.Clock
import com.yammer.metrics.core.VirtualMachineMetrics
import java.util.concurrent.TimeUnit
import com.yammer.metrics.Metrics
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.springframework.util.AntPathMatcher

class FilterableGraphiteReporter extends GraphiteReporter{
    private static final Log LOG = LogFactory.getLog(FilterableGraphiteReporter)

    protected final AntPathMatcher matcher = new AntPathMatcher()

    /**
     * Enables the graphite reporter to send data for the default metrics registry to graphite
     * server with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     */
    public static void enable(long period, TimeUnit unit, String host, int port){
        enable(Metrics.defaultRegistry(), period, unit, host, port)
    }

    /**
     * Enables the graphite reporter to send data for the given metrics registry to graphite server
     * with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port){
        enable(metricsRegistry, period, unit, host, port, null)
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     * @param prefix the string which is prepended to all metric names
     */
    public static void enable(long period, TimeUnit unit, String host, int port, String prefix){
        enable(Metrics.defaultRegistry(), period, unit, host, port, prefix)
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     * @param prefix the string which is prepended to all metric names
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, String prefix){
        enable(metricsRegistry, period, unit, host, port, prefix, MetricPredicate.ALL)
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     * @param prefix the string which is prepended to all metric names
     * @param predicate filters metrics to be reported
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, String prefix, MetricPredicate predicate){
        try{
            final FilterableGraphiteReporter reporter = new FilterableGraphiteReporter(metricsRegistry,
                    prefix,
                    predicate,
                    new GraphiteReporter.DefaultSocketProvider(host, port),
                    Clock.defaultClock())
            reporter.start(period, unit)
        } catch(Exception e){
            LOG.error("Error creating/starting Graphite reporter:", e)
        }
    }

    FilterableGraphiteReporter(String host, int port, String prefix){
        super(host, port, prefix)
    }

    FilterableGraphiteReporter(MetricsRegistry metricsRegistry, String host, int port, String prefix){
        super(metricsRegistry, host, port, prefix)
    }

    FilterableGraphiteReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock clock){
        super(metricsRegistry, prefix, predicate, socketProvider, clock)
    }

    FilterableGraphiteReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock clock, VirtualMachineMetrics vm){
        super(metricsRegistry, prefix, predicate, socketProvider, clock, vm)
    }

    FilterableGraphiteReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock clock, VirtualMachineMetrics vm, String name){
        super(metricsRegistry, prefix, predicate, socketProvider, clock, vm, name)
    }

    private static final int TAB_WIDTH = 10
    private static final String TAB_CHAR = ' '

    private void printList(Collection values, String name, int indentLevel){
        if(values){
            println(indent(indentLevel) + "$name: [")
            println(values.collect {indent(indentLevel + 1) + it}.join('\n'))
            println(indent(indentLevel) + ']')
        } else {
            println(indent(indentLevel) + "$name: []")
        }
    }

    private String indent(int indentLevel){
        return TAB_CHAR * (indentLevel * TAB_WIDTH)
    }

    private String configLine(String label, Object value){
        String paddedLabel = "$label:".toString().padRight(12I)
        return (paddedLabel + value)
    }

    void init(){
        try{
            matcher.setPathSeparator('.') // we use periods as the path separator
            TimeUnit timeUnit = CH?.config?.metrics?.graphite?.timeUnit ? TimeUnit.valueOf(CH?.config?.metrics?.graphite?.timeUnit) : TimeUnit.MINUTES
            long period = CH?.config?.metrics?.graphite?.period ?: 1
            start(period, timeUnit)
            println("############# Graphite Reporter Initialized #############")
            println(configLine('ENABLED', CH?.config?.metrics?.graphite?.enabled))
            println(configLine('host', CH?.config?.metrics?.graphite?.host))
            println(configLine('port', CH?.config?.metrics?.graphite?.port))
            println(configLine('prefix', prefix))
            println(configLine('period', period))
            println(configLine('timeUnit', timeUnit.toString()))
            println()
            println('filters:')
            printList(getBlacklist(), "blacklist", 1)
            printList(getWhitelist(), "whitelist", 1)
            println(indent(1) + 'detailed:')
            printList(getDetailedQualifyingPatterns(), "qualifyingPatterns", 2)
            printList(getDetailedWhitelist(), "whitelist", 2)
            println("#########################################################")
            println()

        } catch(Exception e){
            LOG.error("Error initializing Graphite reporter:", e)
        }
    }

    private Map<String, String> rewriteCache = [:]

    protected String getGraphiteName(final String metricName, final String valueName){
        String fullName = [metricName, valueName].join('.')
        if(!rewriteCache?.containsKey(fullName)){
            String name = fullName
            (CH?.config?.metrics?.graphite?.rewrite)?.each {String pattern, String replacement ->
                name = name.replaceAll(pattern, replacement)
            }
            rewriteCache[fullName] = name
        }
        return rewriteCache.get(fullName)
    }

    private static final Set<String> DEFAULT_WHITELIST = ['**.request.1MinuteRate', '**.request.mean']
    private static final Set<String> DEFAULT_DETAILED_WHITELIST = [
            '**.request.1MinuteRate',
            '**.request.mean',
            '**.request.median',
            '**.request.max',
            '**.request.stddev',
            '**.meanPercentageOfRequestTime',
            '**.invocationRatio'
    ]

    protected Boolean isBlacklisted(String graphiteName){
        return matchesAnyPattern(graphiteName, getBlacklist())
    }

    protected Collection<String> getBlacklist(){
        return CH?.config?.metrics?.graphite?.filters?.blacklist ?: []
    }

    protected Collection<String> getWhitelist(){
        return CH?.config?.metrics?.graphite?.filters?.whitelist ?: DEFAULT_WHITELIST
    }

    protected Collection<String> getDetailedWhitelist(){
        return CH?.config?.metrics?.graphite?.filters?.detailed?.whitelist ?: DEFAULT_DETAILED_WHITELIST
    }

    protected Collection<String> getDetailedQualifyingPatterns(){
        return CH?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns ?: []
    }

    protected Boolean useDetailedWhitelist(String graphiteName){
        return matchesAnyPattern(graphiteName, getDetailedQualifyingPatterns())
    }

    private Boolean matchesAnyPattern(String graphiteName, Collection<String> patterns){
        return patterns?.any {String prefixString -> matcher.match(prefixString, graphiteName)}
    }

    /**
     * TODO: This value can be cached, but we would need to expire the cache if we change the config
     * @param graphiteName the name of the value to send graphite
     * @return whether or not it should be sent to the graphite server
     */
    protected Boolean shouldSend(String graphiteName){
        return !isBlacklisted(graphiteName) &&
                matchesAnyPattern(graphiteName, useDetailedWhitelist(graphiteName) ? getDetailedWhitelist() : getWhitelist())
    }

    @Override
    protected void sendInt(long timestamp, String name, String valueName, long value){
        String graphiteName = getGraphiteName(name, valueName)
        sendToGraphite(timestamp, graphiteName, String.format(locale, "%d", value));
    }

    @Override
    protected void sendFloat(long timestamp, String name, String valueName, double value){
        String graphiteName = getGraphiteName(name, valueName)
        sendToGraphite(timestamp, graphiteName, String.format(locale, "%2.2f", value));
    }

    @Override
    protected void sendObjToGraphite(long timestamp, String name, String valueName, Object value){
        String graphiteName = getGraphiteName(name, valueName)
        sendToGraphite(timestamp, graphiteName, String.format(locale, "%s", value));
    }

    private int lineCount = 0

    @Override
    protected void sendToGraphite(long timestamp, String name, String value){
        if(shouldSend(name)){
            if(LOG?.isTraceEnabled()){LOG.trace(name + '=' + value)}
            try{
                if(!prefix.isEmpty()){
                    writer.write(prefix);
                }
                writer.write(sanitizeString(name));
                writer.write(' ');
                writer.write(value);
                writer.write(' ');
                writer.write(Long.toString(timestamp));
                writer.write('\n');
                writer.flush();
                lineCount++
            } catch(IOException e){
                LOG.error("Error sending to Graphite:", e);
            }
        }
    }

    protected boolean isEnabled(){
        return CH?.config?.metrics?.enabled && CH?.config?.metrics?.graphite?.enabled
    }

    /**
     * TODO: Graphite is configured to have a maximum number of data points per request; we should add a configuration
     * that allows us to throttle/chunk the metrics if we exceed that value, so we don't just lose data unexpectedly
     */
    @Override
    void run(){
        if(isEnabled()){
            lineCount = 0
            super.run()
            if(LOG?.isTraceEnabled()){LOG.trace("Sent $lineCount lines to Graphite")}
            lineCount = 0
        } else {
            if(LOG?.isTraceEnabled()){LOG.trace('Graphite Reporter is disabled - no data will be sent')}
        }

    }
}
