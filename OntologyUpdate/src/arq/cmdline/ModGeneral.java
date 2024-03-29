/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

public class ModGeneral extends ModBase
{
    private CallbackHelp helpCallback = null ;

    public ModGeneral(CallbackHelp callback) { this.helpCallback = callback ; }
    
    // Could be turned into a module but these are convenient as inherited flags 
    private final ArgDecl argDeclHelp        = new ArgDecl(false, "help", "h");
    private final ArgDecl argDeclVerbose     = new ArgDecl(false, "v", "verbose");
    private final ArgDecl argDeclQuiet       = new ArgDecl(false, "q", "quiet");
    private final ArgDecl argDeclDebug       = new ArgDecl(false, "debug");

    protected boolean verbose = false ;
    protected boolean quiet = false ;
    protected boolean debug = false ;
    protected boolean help = false ;
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("General") ;
        cmdLine.add(argDeclVerbose, "-v   --verbose", "Verbose") ;
        cmdLine.add(argDeclQuiet, "-q   --quiet", "Run with minimal output") ;
        cmdLine.add(argDeclDebug, "--debug", "Output information for debugging") ;
        cmdLine.add(argDeclHelp, "--help", null) ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        verbose = cmdLine.contains(argDeclVerbose) ;
        quiet   = cmdLine.contains(argDeclQuiet) ;
        debug   = cmdLine.contains(argDeclDebug) ;
        if ( debug )
            verbose = true ;
        help = cmdLine.contains(argDeclHelp) ;
        if ( help )
            helpCallback.doHelp() ;
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