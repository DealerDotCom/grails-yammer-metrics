package com.yammer.metrics.http;
import org.apache.commons.io.output.CountingOutputStream;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * Source code taken from:
 *      http://stackoverflow.com/questions/3220820/how-to-insert-jsf-page-rendering-time-and-response-size-into-the-page-itself-at/3221516#3221516
 * On: 11/30/2012
 */
public class CountingServletOutputStream extends ServletOutputStream{

    private final CountingOutputStream output;

    public CountingServletOutputStream(ServletOutputStream output){
        this.output = new CountingOutputStream(output);
    }

    @Override
    public void write(int b) throws IOException{
        output.write(b);
    }

    @Override
    public void flush() throws IOException{
        output.flush();
    }

    public long getByteCount(){
        return output.getByteCount();
    }
}