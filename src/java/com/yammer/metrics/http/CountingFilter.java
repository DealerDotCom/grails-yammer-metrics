package com.yammer.metrics.http;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Source code based on:
 * url: http://stackoverflow.com/questions/3220820/how-to-insert-jsf-page-rendering-time-and-response-size-into-the-page-itself-at/3221516#3221516
 * date: 11/30/2012
 */
public class CountingFilter implements Filter{
    private static final Logger LOGGER = LoggerFactory.getLogger(CountingFilter.class);

    public static final String METRICS_GROUP_REQUEST_ATTRIBUTE = "com.yammer.metrics.http.GROUP_REQUEST_ATTRIBUTE";
    public static final String METRICS_TYPE_REQUEST_ATTRIBUTE = "com.yammer.metrics.http.TYPE_REQUEST_ATTRIBUTE";
    public static final String RESPONSE_SIZE_METRIC_NAME = "responseSize";
    public static final String RESPONSE_SIZE_TOTAL_METRIC_NAME = RESPONSE_SIZE_METRIC_NAME + ".total";

    @Override
    public void init(FilterConfig arg0) throws ServletException{
        // NOOP.
    }

    @Override
    public void destroy(){
        // NOOP.
    }

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException{
        CountingServletResponse counter = new CountingServletResponse((HttpServletResponse) response);
        chain.doFilter(request, counter);
        counter.flushBuffer(); // Push the last bits containing HTML comment.
        try{
            processResponseSizeMetric(request, counter);
        } catch(Exception e){
            //Log the exception but don't interrupt the request
            LOGGER.error("Exception thrown while processing " + RESPONSE_SIZE_METRIC_NAME + " metric", e);
        }
    }

    private void processResponseSizeMetric(final ServletRequest request, final CountingServletResponse counter) throws IOException{
        String group = getAttributeValue(request, METRICS_GROUP_REQUEST_ATTRIBUTE);
        String type = getAttributeValue(request, METRICS_TYPE_REQUEST_ATTRIBUTE);
        if(StringUtils.isNotBlank(group) && StringUtils.isNotBlank(type)){ // Update the response size metrics only if the request has the metrics group and type attributes
            long byteCount = counter.getByteCount();
            Histogram responseSizeHistogram = Metrics.newHistogram(new MetricName(group, type, RESPONSE_SIZE_METRIC_NAME), true);
            responseSizeHistogram.update(byteCount);
            Counter responseSizeCounter = Metrics.newCounter(new MetricName(group, type, RESPONSE_SIZE_TOTAL_METRIC_NAME));
            responseSizeCounter.inc(byteCount);

        }
    }

    private String getAttributeValue(final ServletRequest request, final String attributeName){
        Object value = request.getAttribute(attributeName);
        if(value != null && value instanceof String){
            return (String) value;
        }
        return null;
    }
}