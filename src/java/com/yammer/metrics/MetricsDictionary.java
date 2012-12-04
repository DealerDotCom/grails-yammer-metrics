package com.yammer.metrics;

public final class MetricsDictionary{

    // Metric Names and Suffixes
    public static final String TOTAL_METRIC_SUFFIX = "total";
    public static final String INVOCATION_RATIO_METRIC_SUFFIX = "invocationRatio";
    public static final String INVOCATIONS_METRIC_SUFFIX = "invocations";
    public static final String PERCENTAGE_OF_TIME_METRIC_SUFFIX = "percentageOfTime";


    public static final String REQUEST_METRIC = "request";
    public static final String REQUEST_VIEW_METRIC = "view";
    public static final String RESPONSE_SIZE_METRIC = "responseSize";
    public static final String RESPONSE_SIZE_TOTAL_METRIC = RESPONSE_SIZE_METRIC + "." + TOTAL_METRIC_SUFFIX;
    public static final String NESTED_CONTEXT_TASK_METRIC = "nested";


    // Other Constants
    public static final String METRICS_GROUP_REQUEST_ATTRIBUTE = "com.yammer.metrics.http.GROUP_REQUEST_ATTRIBUTE";
    public static final String METRICS_TYPE_REQUEST_ATTRIBUTE = "com.yammer.metrics.http.TYPE_REQUEST_ATTRIBUTE";


    private MetricsDictionary(){ /* unused */ }
}
