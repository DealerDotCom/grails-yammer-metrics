package com.yammer.metrics.http;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Extension of HttpServletResponseWrapper that, in addition to wrapping a response, delegates all response output to a provided OutputStream or
 * ServletOutputStream.
 */
public class OutputDelegatingServletResponseWrapper extends HttpServletResponseWrapper{

    private final DelegatingServletOutputStream output;
    private final PrintWriter writer;

    public OutputDelegatingServletResponseWrapper(HttpServletResponse response, OutputStream outputDelegate) throws IOException{
        this(response, new DelegatingServletOutputStream(outputDelegate));
    }

    public OutputDelegatingServletResponseWrapper(HttpServletResponse response, DelegatingServletOutputStream outputDelegate) throws IOException{
        super(response);
        output = outputDelegate;
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

    protected OutputStream getOutputDelegate(){
        return output.getDelegate();
    }
}