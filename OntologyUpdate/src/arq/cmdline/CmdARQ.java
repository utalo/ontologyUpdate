/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.query.ARQ;

public abstract class CmdARQ extends CmdGeneral
{
    protected ModSymbol modSymbol = new ModSymbol() ;
    ArgDecl  strictDecl = new ArgDecl(ArgDecl.NoValue, "strict", "basic") ;
    
    protected CmdARQ(String[] argv)
    {
        super(argv) ;
        ModSymbol.addPrefixMapping(ARQ.arqSymbolPrefix, ARQ.arqNS) ;
        addModule(modSymbol) ;
        super.add(strictDecl, "--strict", "Operate in strict SPARQL mode (no extensions of any kind)") ;
    }
    
    protected void processModulesAndArgs()
    { 
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( super.contains(strictDecl) ) 
            ARQ.setStrictMode() ;
        if ( modGeneral.debug )
            QueryIteratorBase.traceIterators = true ;
    }
    
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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