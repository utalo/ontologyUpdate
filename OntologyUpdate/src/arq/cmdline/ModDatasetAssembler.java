/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import arq.cmd.CmdException;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;

/** Add assembler to a general dataset description */
public class ModDatasetAssembler extends ModDatasetGeneral
{
    ModAssembler modAssembler = new ModAssembler() ;
    
    public Dataset createDataset()
    {
        if ( modAssembler.getAssemblerFile() == null )
            return super.createDataset() ;
        
        try {
            dataset = (Dataset)modAssembler.create(DatasetAssemblerVocab.tDataset) ;
            if ( dataset == null )
                throw new CmdException("No dataset description found in: "+modAssembler.getAssemblerFile()) ;
        }
        catch (CmdException ex) { throw ex ; }
        catch (ARQException ex) { throw ex; }
        catch (NotFoundException ex)
        { throw new CmdException("Not found: "+ex.getMessage()) ; }
        catch (JenaException ex)
        { throw ex ; }
        catch (Exception ex)
        { throw new CmdException("Error creating dataset", ex) ; }

        if ( dataset instanceof DataSource )
            super.addGraphs((DataSource)dataset) ;
        return dataset ;
    }

    public void registerWith(CmdGeneral cmdLine)
    {
        modAssembler.registerWith(cmdLine) ;
        super.registerWith(cmdLine) ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        modAssembler.processArgs(cmdLine) ;
        super.processArgs(cmdLine) ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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