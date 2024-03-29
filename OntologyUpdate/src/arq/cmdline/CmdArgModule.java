/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class CmdArgModule extends CmdMain
{
    List modules = new ArrayList() ;
    
    protected CmdArgModule(String[] argv)
    {
        super(argv) ;
    }
    
    protected void addModule(ArgModuleGeneral argModule)
    {
        modules.add(argModule) ;
    }

    final
    public void process()
    {
        super.process() ;
        forEach(new Action(){
            public void action(CmdArgModule controller, ArgModuleGeneral module)
            { 
                module.processArgs(controller) ;
            }
        } ) ;
        processModulesAndArgs() ;
    }
    
    abstract
    protected void processModulesAndArgs() ;
    
    private void forEach(Action action)
    {
        for ( Iterator iter = modules.iterator() ; iter.hasNext(); )
        {
            ArgModuleGeneral am = (ArgModuleGeneral)iter.next() ;
            action.action(this, am) ;
        }
    }
                    
    interface Action
    {
        public void action(CmdArgModule controller, ArgModuleGeneral module) ;
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