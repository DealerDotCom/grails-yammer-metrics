package com.yammer.metrics.http;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Extension of ServletOutputStream that delegates all calls to the OutputStream it wraps.
 */
public class DelegatingServletOutputStream extends ServletOutputStream{

    private final OutputStream outputDelegate;

    public DelegatingServletOutputStream(OutputStream delegate){
        this.outputDelegate = delegate;
    }

    @Override
    public void write(int b) throws IOException{
        outputDelegate.write(b);
    }

    @Override
    public void flush() throws IOException{
        outputDelegate.flush();
    }

    public OutputStream getDelegate(){
        return outputDelegate;
    }
}