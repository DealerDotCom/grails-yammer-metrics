package com.yammer.metrics.http;

import org.apache.commons.io.output.CountingOutputStream;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HttpServletResponseWrapper that also delegates all output to a CountingOutputStream so that the number of bytes written can be accessed.
 */
public class CountingServletResponse extends OutputDelegatingServletResponseWrapper{

    public CountingServletResponse(HttpServletResponse response) throws IOException{
        super(response, new CountingOutputStream(response.getOutputStream()));
    }

    public long getByteCount() throws IOException{
        flushBuffer(); // Ensure that all bytes are written at this point.
        return ((CountingOutputStream) (getOutputDelegate())).getByteCount();
    }
}