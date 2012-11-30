package com.yammer.metrics.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Source code taken from:
 *      http://stackoverflow.com/questions/3220820/how-to-insert-jsf-page-rendering-time-and-response-size-into-the-page-itself-at/3221516#3221516
 * On: 11/30/2012
 */
public class CountingServletResponse extends HttpServletResponseWrapper{

    private final CountingServletOutputStream output;
    private final PrintWriter writer;

    public CountingServletResponse(HttpServletResponse response) throws IOException{
        super(response);
        output = new CountingServletOutputStream(response.getOutputStream());
        writer = new PrintWriter(output, true);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException{
        return output;
    }

    @Override
    public PrintWriter getWriter() throws IOException{
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException{
        writer.flush();
    }

    public long getByteCount() throws IOException{
        flushBuffer(); // Ensure that all bytes are written at this point.
        return output.getByteCount();
    }
}