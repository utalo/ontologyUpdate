/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.shared.JenaException;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.util.Base64;
import com.hp.hpl.jena.sparql.util.Convert;
import com.hp.hpl.jena.util.FileUtils;

/** Create an execution object for performing a query on a model
 *  over HTTP.  This is the main protocol engine for HTTP query.
 *  There are higher level classes for doing a query and presenting
 *  the results in an API fashion. 
 * 
 *  If the query string is large, then HTTP POST is used.

 * @author  Andy Seaborne
 */
public class HttpQuery extends Params
{
    static final Log log = LogFactory.getLog(HttpQuery.class.getName()) ;
    
    /** The definition of "large" queries */
    // Not final so that other code can change it.
    static public /*final*/ int urlLimit = 2*1024 ;
    
    String serviceURL ;
    
    String contentTypeResult = HttpParams.contentTypeResultsXML ;
    HttpURLConnection httpConnection = null ;
    
    // An object indicate no value associated with parameter name 
    final static Object noValue = new Object() ;
    String user = null ;
    char[] password = null ;
    
    int responseCode = 0;
    String responseMessage = null ;
    boolean forcePOST = false ;
    String queryString = null ;
    
    //static final String ENC_UTF8 = "UTF-8" ;
    
    /** Create a execution object for a whole model GET
     * @param serviceURL     The model
     */
    
    public HttpQuery(String serviceURL)
    {
        init(serviceURL) ;
    }
        

    /** Create a execution object for a whole model GET
     * @param url           The model
     */
    
    public HttpQuery(URL url)
    {
        init(url.toString()) ;
    }
        

    private void init(String serviceURL)
    {
        if ( log.isTraceEnabled())
            log.trace("URL: "+serviceURL) ;
 
        if ( serviceURL.indexOf('?') >= 0 )
            throw new QueryExceptionHTTP(-1, "URL already has a query string ("+serviceURL+")") ;
        
        this.serviceURL = serviceURL ;
    }
    
    private String getQueryString()
    {
        if ( queryString == null )
            queryString = super.httpString() ;
        return queryString ;
    }

    public HttpURLConnection getConnection() { return httpConnection ; }
    
    /** Set the content type (Accept header) for the results
     */
    
    public void setAccept(String contentType)
    {
        contentTypeResult = contentType ;
    }
    
    public void setBasicAuthentication(String user, char[] password)
    {
        this.user = user ;
        this.password = password ;
    }
    
    /** Return whether this request will go by GET or POST
     *  @return boolean
     */
    public boolean usesPOST()
    {
        if ( forcePOST )
            return true ;
        String s = getQueryString() ;
        
        return serviceURL.length()+s.length() >= urlLimit ;
    }

    /** Force the use of HTTP POST for the query operation
     */

    public void setForcePOST()
    {
        forcePOST = true ;
    }

    /** Execute the operation
     * @return Model    The resulting model
     * @throws QueryExceptionHTTP
     */
    public InputStream exec() throws QueryExceptionHTTP
    {
        try {
            if (usesPOST())
                return execPost();
            return execGet();
        } catch (QueryExceptionHTTP httpEx)
        {
            log.trace("Exception in exec", httpEx);
            throw httpEx;
        }
        catch (JenaException jEx)
        {
            log.trace("JenaException in exec", jEx);
            throw jEx ;
        }
    }
     

    private InputStream execGet() throws QueryExceptionHTTP
    {
        URL target = null ;
        String qs = getQueryString() ;
        
        //System.out.println(qs) ;
        
        try {
            if ( count() == 0 )
                target = new URL(serviceURL) ; 
            else
                target = new URL(serviceURL+"?"+qs) ;
        }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: "+malEx) ; }
        log.trace("GET "+target.toExternalForm()) ;
        
        try
        {
            httpConnection = (HttpURLConnection) target.openConnection();
            httpConnection.setRequestProperty("Accept", contentTypeResult) ;
            // By default, following 3xx redirects is true
            //conn.setFollowRedirects(true) ;
            basicAuthentication(httpConnection) ;
            
            httpConnection.setDoInput(true);
            httpConnection.connect();
            try
            {
                return execCommon();
            }
            catch (QueryExceptionHTTP qEx)
            {
                // Back-off and try POST if something complain about long URIs
                // Broken 
                if (qEx.getResponseCode() == 414 /*HttpServletResponse.SC_REQUEST_URI_TOO_LONG*/ )
                    return execPost();
                throw qEx;
            }
        }
        catch (java.net.ConnectException connEx)
        { throw new QueryExceptionHTTP(QueryExceptionHTTP.NoServer, "Failed to connect to remote server"); }
        catch (IOException ioEx)
        { throw new QueryExceptionHTTP(ioEx); }
    }
    
    private InputStream execPost() throws QueryExceptionHTTP
    {
        URL target = null;
        try { target = new URL(serviceURL); }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: " + malEx); }
        log.trace("POST "+target.toExternalForm()) ;
        
        try
        {
            httpConnection = (HttpURLConnection) target.openConnection();
            httpConnection.setRequestMethod("POST") ;
            httpConnection.setRequestProperty("Accept", contentTypeResult) ;
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") ;
            basicAuthentication(httpConnection) ;
            httpConnection.setDoOutput(true) ;
            
            boolean first = true ;
            OutputStream out = httpConnection.getOutputStream() ;
            for ( Iterator iter = pairs().listIterator() ; iter.hasNext() ; )
            {
                if ( ! first )
                    out.write('&') ;
                first = false ;
                Pair p = (Pair)iter.next() ;
                out.write(p.getName().getBytes()) ;
                out.write('=') ;
                String x = p.getValue() ;
                x = Convert.encWWWForm(x) ;
                out.write(x.getBytes()) ;
            }
            out.flush() ;
            httpConnection.connect() ;
            return execCommon() ;
        }
        catch (java.net.ConnectException connEx)
        { throw new QueryExceptionHTTP(-1, "Failed to connect to remote server"); }
        catch (IOException ioEx)
        { throw new QueryExceptionHTTP(ioEx); }
    }
    
    private void basicAuthentication(HttpURLConnection httpConnection2)
    {
        // Do basic authentication : do directly, not via an Authenticator, because it 
        // avoids an extra round trip (Java normally does the request without authetication,
        // then reties with)

        if ( user != null || password != null)
        {
            try
            {
                if ( user == null || password == null )
                    log.warn("Only one of user/password is set") ;
                // We want: "Basic user:password" except user:password is base 64 encoded.
                // Build string, get as UTF-8, bytes, translate to base 64. 
                StringBuffer x = new StringBuffer() ;
                byte b[] = x.append(user).append(":").append(password).toString().getBytes("UTF-8") ;
                String y = Base64.encodeBytes(b) ;
                httpConnection.setRequestProperty("Authorization", y) ;
                // Overwrite any password details we copied.
                // Still leaves the copy in the HTTP connection.  But this only basic auth. 
                for ( int i = 0 ; i < x.length() ; i++ ) x.setCharAt(i, '*') ;
                for ( int i = 0 ; i < b.length ; i++ ) b[i] = (byte)0 ; 
            } catch (UnsupportedEncodingException ex)
            {
                // Can't happen - UTF-8 is required of all Java platforms. 
                throw new ARQInternalErrorException("UTF-8 is broken on this platform", ex) ;
            }
        }
    }


    private InputStream execCommon() throws QueryExceptionHTTP
    {
        try {        
            responseCode = httpConnection.getResponseCode() ;
            responseMessage = Convert.decWWWForm(httpConnection.getResponseMessage()) ;
            
            // 1xx: Informational 
            // 2xx: Success 
            // 3xx: Redirection 
            // 4xx: Client Error 
            // 5xx: Server Error 
            
            if ( 300 <= responseCode && responseCode < 400 )
            {
                
                throw new QueryExceptionHTTP(responseCode, responseMessage) ;
            }
            
            // Other 400 and 500 - errors 
            
            if ( responseCode >= 400 )
            {
                throw new QueryExceptionHTTP(responseCode, responseMessage) ;
            }
  
            // Request suceeded
            InputStream in = httpConnection.getInputStream() ;
            
            if ( false )
            {
                // Dump the reply
                Map map = httpConnection.getHeaderFields() ;
                for ( Iterator iter = map.keySet().iterator() ; iter.hasNext() ; )
                {
                    String k = (String)iter.next();
                    List v = (List)map.get(k) ;
                    System.out.println(k+" = "+v) ;
                }
                
                // Dump response body
                StringBuffer b = new StringBuffer(1000) ;
                byte[] chars = new byte[1000] ;
                while(true)
                {
                    int x = in.read(chars) ;
                    if ( x < 0 ) break ;
                    b.append(new String(chars, 0, x, FileUtils.encodingUTF8)) ;
                }
                System.out.println(b.toString()) ;
                System.out.flush() ;
                // Reset
                in = new ByteArrayInputStream(b.toString().getBytes(FileUtils.encodingUTF8)) ;
            }
            return in ;
        }
        catch (IOException ioEx)
        {
            throw new QueryExceptionHTTP(ioEx) ;
        } 
        catch (JenaException rdfEx)
        {
            throw new QueryExceptionHTTP(rdfEx) ;
        }
    }
    
    public String toString()
    {
        String s = httpString() ;
        if ( s != null && s.length() > 0 )
            return serviceURL+"?"+s ;
        return serviceURL ;
    }
}

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
