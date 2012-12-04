package com.yammer.metrics

import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedOperationParameters
import org.springframework.jmx.export.annotation.ManagedOperationParameter
import org.springframework.jmx.export.annotation.ManagedAttribute

@ManagedResource(objectName = "com.yammer.metrics:name=MetricsConfigService", description = "A service containing oprations for adjusting the application's YammerMetrics configuration at runtime")
class MetricsConfigService{
    static transactional = false
    def grailsApplication

    @ManagedAttribute(description = "Whether or not YammerMetrics are enabled for this application")
    boolean getMetricsEnabled(){
        return grailsApplication?.config?.metrics?.enabled
    }

    @ManagedAttribute(description = "Whether or not YammerMetrics are enabled for this application")
    void setMetricsEnabled(boolean value){
        grailsApplication?.config?.metrics?.enabled = value
    }

    @ManagedAttribute(description = "Whether or not requests are being profiled and metrics generated for them")
    boolean getRequestProfilingEnabled(){
        return grailsApplication?.config?.metrics?.requestProfiling?.enabled
    }

    @ManagedAttribute(description = "Whether or not requests are being profiled and metrics generated for them")
    void setRequestProfilingEnabled(boolean value){
        grailsApplication?.config?.metrics?.requestProfiling?.enabled = value
    }

    @ManagedAttribute(description = "Whether or not metrics are being reported to graphite")
    boolean getGraphiteReportingEnabled(){
        return grailsApplication?.config?.metrics?.graphite?.enabled
    }

    @ManagedAttribute(description = "Whether or not metrics are being reported to graphite")
    void setGraphiteReportingEnabled(boolean value){
        grailsApplication?.config?.metrics?.graphite?.enabled = value
    }

    @ManagedAttribute(description = "Whether or not metrics are being generated for the view rendering portion of requests")
    boolean getProfileViewRendering(){
        return grailsApplication?.config?.metrics?.requestProfiling?.profileViewRendering
    }

    @ManagedAttribute(description = "Whether or not metrics are being generated for the view rendering portion of requests")
    void setProfileViewRendering(boolean value){
        grailsApplication?.config?.metrics?.requestProfiling?.profileViewRendering = value
    }

    @ManagedAttribute(description = "The host of the graphite server that the GraphiteReporter is publishing metrics to")
    String getGraphiteHost(){
        return grailsApplication?.config?.metrics?.graphite?.host
    }

    @ManagedAttribute(description = "The port of the graphite server that the GraphiteReporter is publishing metrics to")
    int getGraphitePort(){
        return grailsApplication?.config?.metrics?.graphite?.port
    }

    @ManagedAttribute(description = "The period the GraphiteReporter uses to publish; combined with the time unit determines the frequency publishing occurs")
    int getGraphitePeriod(){
        return grailsApplication?.config?.metrics?.graphite?.period
    }

    @ManagedAttribute(description = "The time units the GraphiteReporter uses to publish; combined with the period determines the frequency publishing occurs")
    String getGraphiteTimeUnits(){
        return grailsApplication?.config?.metrics?.graphite?.timeUnit
    }

    @ManagedAttribute(description = "The prefix the GraphiteReporter prepends to all metrics it publishes")
    String getGraphitePrefix(){
        return grailsApplication?.config?.metrics?.graphite?.prefix
    }

    @ManagedAttribute(description = "The rewrite rules used for all metrics published by the GraphiteReporter. All instances of the keys in this map within metric names are replaced by the matching value")
    Map<String, String> getGraphiteRewriteRules(){
        return grailsApplication?.config?.metrics?.graphite?.rewrite
    }

    @ManagedAttribute(description = "Patterns that metric names must END with in order to be published; used only for non-detailed metrics")
    List<String> getWhitelist(){
        return grailsApplication?.config?.metrics?.graphite?.filters?.whitelist
    }

    @ManagedAttribute(description = "Patterns that metric names must START with in order to be EXCLUDED from publication")
    List<String> getBlacklist(){
        return grailsApplication?.config?.metrics?.graphite?.filters?.blacklist
    }

    @ManagedAttribute(description = "Patterns that metric names must START with in order to be considered 'detailed' metrics; additional reporting for these metrics will occur")
    List<String> getDetailedQualifyingPatterns(){
        return grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns
    }

    @ManagedAttribute(description = "Patterns that metric names must END with in order to be published; used only for detailed metrics")
    List<String> getDetailedWhitelist(){
        return grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.whitelist
    }

    @ManagedOperation(description = "Adds a new 'detailed' qualifying pattern. Metrics matching this pattern will report additional info.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern that metrics should match to enable detailed metrics reporting"),
    ])
    void addDetailedQualifyingPattern(String pattern){
        if(!grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.contains(pattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.add(pattern)
        }
    }

    @ManagedOperation(description = "Removes a 'detailed' qualifying pattern from the configuration. Metrics matching this pattern will no longer report additional info.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern that metrics should match to enable detailed metrics reporting"),
    ])
    void removeDetailedQualifyingPattern(String pattern){
        if(grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.contains(pattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.removeAll(pattern)
        }
    }

    @ManagedOperation(description = "Adds a new pattern to the blacklist filter. Metrics that begin with the pattern will be excluded from being reported to graphite.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "blacklistPattern", description = "The prefix that metrics should match to be excluded from reporting"),
    ])
    void addBlacklistPattern(String blacklistPattern){
        if(!grailsApplication?.config?.metrics?.graphite?.filters?.blacklist?.contains(blacklistPattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.blacklist?.add(blacklistPattern)
        }
    }

    @ManagedOperation(description = "Removes a pattern from the blacklist filter")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "blacklistPattern", description = "The pattern to remove"),
    ])
    void removeBlacklistPattern(String blacklistPattern){
        if(grailsApplication?.config?.metrics?.graphite?.filters?.blacklist?.contains(blacklistPattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.blacklist?.removeAll(blacklistPattern)
        }
    }

    @ManagedOperation(description = "Adds a new pattern to the whitelist filter. Metrics that begin end with the pattern will be reported to graphite.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The suffix that metrics should match to be included in reporting"),
    ])
    void addWhitelistPattern(String pattern){
        if(!grailsApplication?.config?.metrics?.graphite?.filters?.whitelist?.contains(pattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.whitelist?.add(pattern)
        }
    }

    @ManagedOperation(description = "Removes a pattern from the whitelist filter")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern to remove"),
    ])
    void removeWhitelistPattern(String pattern){
        if(grailsApplication?.config?.metrics?.graphite?.filters?.whitelist?.contains(pattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.whitelist?.removeAll(pattern)
        }
    }

    @ManagedOperation(description = "Adds a new pattern to the detailed whitelist filter. Metrics that match a 'detailed' prefix and end with the pattern will be reported to graphite.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The suffix that detailed metrics should match to be included in reporting"),
    ])
    void addDetailedWhitelistPattern(String pattern){
        if(!grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.contains(pattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.add(pattern)
        }
    }

    @ManagedOperation(description = "Removes a pattern from the detailed whitelist filter")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern to remove"),
    ])
    void removeDetailedWhitelistPattern(String pattern){
        if(grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.contains(pattern)){
            grailsApplication?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.removeAll(pattern)
        }
    }
}
