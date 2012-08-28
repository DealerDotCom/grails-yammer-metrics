package com.yammer.metrics

import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedOperationParameters
import org.springframework.jmx.export.annotation.ManagedOperationParameter
import org.springframework.jmx.export.annotation.ManagedAttribute

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

@ManagedResource(objectName = "com.yammer.metrics:name=MetricsConfigService", description = "A service containing oprations for adjusting the application's YammerMetrics configuration at runtime")
class MetricsConfigService{
    static transactional = false

    @ManagedAttribute(description = "Whether or not YammerMetrics are enabled for this application")
    boolean getMetricsEnabled(){
        return CH?.config?.metrics?.enabled
    }

    @ManagedAttribute(description = "Whether or not YammerMetrics are enabled for this application")
    void setMetricsEnabled(boolean value){
        CH?.config?.metrics?.enabled = value
    }

    @ManagedAttribute(description = "Whether or not requests are being profiled and metrics generated for them")
    boolean getRequestProfilingEnabled(){
        return CH?.config?.metrics?.requestProfiling?.enabled
    }

    @ManagedAttribute(description = "Whether or not requests are being profiled and metrics generated for them")
    void setRequestProfilingEnabled(boolean value){
        CH?.config?.metrics?.requestProfiling?.enabled = value
    }

    @ManagedAttribute(description = "Whether or not metrics are being reported to graphite")
    boolean getGraphiteReportingEnabled(){
        return CH?.config?.metrics?.graphite?.enabled
    }

    @ManagedAttribute(description = "Whether or not metrics are being reported to graphite")
    void setGraphiteReportingEnabled(boolean value){
        CH?.config?.metrics?.graphite?.enabled = value
    }

    @ManagedAttribute(description = "Whether or not metrics are being generated for the view rendering portion of requests")
    boolean getProfileViewRendering(){
        return CH?.config?.metrics?.requestProfiling?.profileViewRendering
    }

    @ManagedAttribute(description = "Whether or not metrics are being generated for the view rendering portion of requests")
    void setProfileViewRendering(boolean value){
        CH?.config?.metrics?.requestProfiling?.profileViewRendering = value
    }

    @ManagedAttribute(description = "The host of the graphite server that the GraphiteReporter is publishing metrics to")
    String getGraphiteHost(){
        return CH?.config?.metrics?.graphite?.host
    }

    @ManagedAttribute(description = "The port of the graphite server that the GraphiteReporter is publishing metrics to")
    int getGraphitePort(){
        return CH?.config?.metrics?.graphite?.port
    }

    @ManagedAttribute(description = "The period the GraphiteReporter uses to publish; combined with the time unit determines the frequency publishing occurs")
    int getGraphitePeriod(){
        return CH?.config?.metrics?.graphite?.period
    }

    @ManagedAttribute(description = "The time units the GraphiteReporter uses to publish; combined with the period determines the frequency publishing occurs")
    String getGraphiteTimeUnits(){
        return CH?.config?.metrics?.graphite?.timeUnit
    }

    @ManagedAttribute(description = "The prefix the GraphiteReporter prepends to all metrics it publishes")
    String getGraphitePrefix(){
        return CH?.config?.metrics?.graphite?.prefix
    }

    @ManagedAttribute(description = "The rewrite rules used for all metrics published by the GraphiteReporter. All instances of the keys in this map within metric names are replaced by the matching value")
    Map<String, String> getGraphiteRewriteRules(){
        return CH?.config?.metrics?.graphite?.rewrite
    }

    @ManagedAttribute(description = "Patterns that metric names must END with in order to be published; used only for non-detailed metrics")
    List<String> getWhitelist(){
        return CH?.config?.metrics?.graphite?.filters?.whitelist
    }

    @ManagedAttribute(description = "Patterns that metric names must START with in order to be EXCLUDED from publication")
    List<String> getBlacklist(){
        return CH?.config?.metrics?.graphite?.filters?.blacklist
    }

    @ManagedAttribute(description = "Patterns that metric names must START with in order to be considered 'detailed' metrics; additional reporting for these metrics will occur")
    List<String> getDetailedQualifyingPatterns(){
        return CH?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns
    }

    @ManagedAttribute(description = "Patterns that metric names must END with in order to be published; used only for detailed metrics")
    List<String> getDetailedWhitelist(){
        return CH?.config?.metrics?.graphite?.filters?.detailed?.whitelist
    }

    @ManagedOperation(description = "Adds a new 'detailed' qualifying pattern. Metrics matching this pattern will report additional info.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern that metrics should match to enable detailed metrics reporting"),
    ])
    void addDetailedQualifyingPattern(String pattern){
        if(!CH?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.contains(pattern)){
            CH?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.add(pattern)
        }
    }

    @ManagedOperation(description = "Removes a 'detailed' qualifying pattern from the configuration. Metrics matching this pattern will no longer report additional info.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern that metrics should match to enable detailed metrics reporting"),
    ])
    void removeDetailedQualifyingPattern(String pattern){
        if(CH?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.contains(pattern)){
            CH?.config?.metrics?.graphite?.filters?.detailed?.qualifyingPatterns?.removeAll(pattern)
        }
    }

    @ManagedOperation(description = "Adds a new pattern to the blacklist filter. Metrics that begin with the pattern will be excluded from being reported to graphite.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "blacklistPattern", description = "The prefix that metrics should match to be excluded from reporting"),
    ])
    void addBlacklistPattern(String blacklistPattern){
        if(!CH?.config?.metrics?.graphite?.filters?.blacklist?.contains(blacklistPattern)){
            CH?.config?.metrics?.graphite?.filters?.blacklist?.add(blacklistPattern)
        }
    }

    @ManagedOperation(description = "Removes a pattern from the blacklist filter")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "blacklistPattern", description = "The pattern to remove"),
    ])
    void removeBlacklistPattern(String blacklistPattern){
        if(CH?.config?.metrics?.graphite?.filters?.blacklist?.contains(blacklistPattern)){
            CH?.config?.metrics?.graphite?.filters?.blacklist?.removeAll(blacklistPattern)
        }
    }

    @ManagedOperation(description = "Adds a new pattern to the whitelist filter. Metrics that begin end with the pattern will be reported to graphite.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The suffix that metrics should match to be included in reporting"),
    ])
    void addWhitelistPattern(String pattern){
        if(!CH?.config?.metrics?.graphite?.filters?.whitelist?.contains(pattern)){
            CH?.config?.metrics?.graphite?.filters?.whitelist?.add(pattern)
        }
    }

    @ManagedOperation(description = "Removes a pattern from the whitelist filter")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern to remove"),
    ])
    void removeWhitelistPattern(String pattern){
        if(CH?.config?.metrics?.graphite?.filters?.whitelist?.contains(pattern)){
            CH?.config?.metrics?.graphite?.filters?.whitelist?.removeAll(pattern)
        }
    }

    @ManagedOperation(description = "Adds a new pattern to the detailed whitelist filter. Metrics that match a 'detailed' prefix and end with the pattern will be reported to graphite.")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The suffix that detailed metrics should match to be included in reporting"),
    ])
    void addDetailedWhitelistPattern(String pattern){
        if(!CH?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.contains(pattern)){
            CH?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.add(pattern)
        }
    }

    @ManagedOperation(description = "Removes a pattern from the detailed whitelist filter")
    @ManagedOperationParameters([
    @ManagedOperationParameter(name = "pattern", description = "The pattern to remove"),
    ])
    void removeDetailedWhitelistPattern(String pattern){
        if(CH?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.contains(pattern)){
            CH?.config?.metrics?.graphite?.filters?.detailed?.whitelist?.removeAll(pattern)
        }
    }
}
