package com.yammer.metrics.http;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Source code taken from:
 *      http://stackoverflow.com/questions/3220820/how-to-insert-jsf-page-rendering-time-and-response-size-into-the-page-itself-at/3221516#3221516
 * On: 11/30/2012
 */
public class CountingFilter implements Filter{

    public static final String METRICS_GROUP_REQUEST_ATTRIBUTE = "com.yammer.metrics.http.GROUP_REQUEST_ATTRIBUTE";
    public static final String METRICS_TYPE_REQUEST_ATTRIBUTE = "com.yammer.metrics.http.TYPE_REQUEST_ATTRIBUTE";
    public static final String RESPONSE_SIZE_METRIC_NAME = "responseSize";

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // NOOP.
    }

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException{
        HttpServletResponse httpres = (HttpServletResponse) response;
        CountingServletResponse counter = new CountingServletResponse(httpres);
        HttpServletRequest httpreq = (HttpServletRequest) request;
        httpreq.setAttribute("counter", counter);
        chain.doFilter(request, counter);
        try{
            processResponseSizeMetric(request, counter);
        } finally{
            counter.flushBuffer(); // Push the last bits containing HTML comment.
        }
    }

    private void processResponseSizeMetric(final ServletRequest request, final CountingServletResponse counter) throws IOException{
        Object group = request.getAttribute(METRICS_GROUP_REQUEST_ATTRIBUTE);
        Object type = request.getAttribute(METRICS_TYPE_REQUEST_ATTRIBUTE);
        if(isValid(group) && isValid(type)){
            // Update the response size metric only if the request has the metrics group and type attributes
            Histogram responseSizeHistogram = Metrics.newHistogram(getMetricName((String)group, (String)type), true);
            responseSizeHistogram.update(counter.getByteCount());
        }
    }

    private MetricName getMetricName(String group, String type){
        return new MetricName(group, type, RESPONSE_SIZE_METRIC_NAME);
    }

    private boolean isValid(Object attribute){
        return attribute != null && attribute instanceof String;
    }

    @Override
    public void destroy() {
        // NOOP.
    }

}