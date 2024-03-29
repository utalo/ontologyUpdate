/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;

import java.util.* ;

import com.hp.hpl.jena.sparql.util.Convert;

/** A collection of parameters for protocol use. */

public class Params
{
    // As seen.
    private List paramList = new ArrayList() ;
    
    // string -> list -> string
    private Map params = new HashMap() ;
    
    
    /** Create a Params object */
    
    public Params() { }
    
    /** Create a Params object, initialized from another one.  A copy is made
     * so the initial values of the Params object are as of the time this constructor
     * was called.
     *  
     * @param other
     */
    public Params(Params other)
    {
        merge(other) ;
    }
    
    public void merge(Params other)
    {
        params.putAll(other.params) ;
        paramList.addAll(other.paramList) ;
    }

    
    /** Add a parameter.
     * @param name  Name of the parameter
     * @param value Value - May be null to indicate none - the name still goes.
     */
    
    public void addParam(String name, String value)
    {
        Pair p = new Pair(name, value) ;
        paramList.add(p) ;
        List x = (List)params.get(name) ;
        if ( x == null )
        {
            x = new ArrayList() ;
            params.put(name, x) ;
        }
        x.add(value) ;
    }

    /** Valueless parameter */
    public void addParam(String name) { addParam(name, null) ; }
    
    public boolean containsParam(String name) { return params.containsKey(name) ; }
    
    public String getValue(String name)
    {
        List x = getMV(name) ;
        if ( x == null )
            return null ;
        if ( x.size() != 1 )
            throw new MultiValueException("Multiple value ("+x.size()+" when exactly one requested") ; 
        return (String)x.get(0) ;
    }
    
    public List getValues(String name)
    {
        List x = getMV(name) ;
        return x ;
    }
        
    public void remove(String name)
    {
        // Absolute record
        for ( Iterator iter = paramList.listIterator() ; iter.hasNext() ; )
        {
            Pair p = (Pair)iter.next() ;
            if ( p.getName().equals(name) )
                iter.remove() ;
        }
        // Map
        params.remove(name) ;
    }
    
    /** Exactly as seen */
    public List pairs()
    {
        return paramList ;
    }
    
    public int count() { return paramList.size() ; }
    
    /** Get the names of parameters - one ocurrence */ 
    public List names()
    {
        List names = new ArrayList() ;
        for (Iterator iter = paramList.listIterator() ; iter.hasNext() ; )
        {
            String s = (String)iter.next() ;
            if ( names.contains(s) )
                continue ;
            names.add(s) ;
        }
        return names ; 
    }
    
    public String httpString()
    {
        StringBuffer sbuff = new StringBuffer() ; 
        boolean first = true ;
        for ( Iterator iter = pairs().listIterator() ; iter.hasNext() ; )
        {
            Pair p = (Pair)iter.next() ;
            if ( !first )
                sbuff.append('&') ;
            sbuff.append(p.getName()) ;
            sbuff.append('=') ;
            String x = p.getValue() ;
            x = Convert.encWWWForm(x) ;
            sbuff.append(x) ;
            first = false ;
        }
        return sbuff.toString() ;
    }
    
    private List getMV(String name)
    {
        List x = (List)params.get(name) ;
        if ( x == null )
            return null ;
        return x ;
    }

    static class MultiValueException extends RuntimeException
    {
        MultiValueException(String msg) { super(msg) ; }
    }
        
    public static class Pair
    { 
        String name ;
        String value ;

        Pair(String name, String value) { setName(name) ; setValue(value) ; }
        public String getName()  { return name ;  }
        public String getValue() { return value ; }

        void setName(String name)   { this.name = name ; }
        void setValue(String value) { this.value = value ; }
        
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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