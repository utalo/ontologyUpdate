/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.op;


import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.UpdateSerializer;
import com.hp.hpl.jena.sparql.modify.UpdateVisitor;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintSerializable;
import com.hp.hpl.jena.sparql.util.PrintUtils;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateFactory;

public abstract class Update implements PrintSerializable
{
    
    /** Execute an update.
     * @deprecated {@link UpdateFactory.create(GraphStore)}
     * @param graphStore
     */
    
    public final void exec(GraphStore graphStore)
    { UpdateFactory.create(this, graphStore).execute() ; }
    
    /** Execute an update, using the binding as values for some of the variables in a pattern.
     * Binding ignored in operations without a pattern.
     * @deprecated {@link UpdateFactory.create(GraphStore, Binding)}
     * @param graphStore
     * @param binding
     */
    
    public final void exec(GraphStore graphStore, Binding binding)
    {
        UpdateFactory.create(this, graphStore, binding).execute() ;
    }

    public abstract void visit(UpdateVisitor visitor) ; 
    
    public final void output(IndentedWriter out, SerializationContext sCxt)
    {
        visit(new UpdateSerializer(out, sCxt)) ;
    }

    public void output(IndentedWriter out)
    {
        System.err.println("Update.output") ;
    }

    public String toString(PrefixMapping pmap)
    {  return PrintUtils.toString(this, pmap) ; } 
    
    //@Override
    public String toString()
    { return PrintUtils.toString(this) ; }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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