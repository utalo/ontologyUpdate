/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.io.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.builders.BuilderExpr;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp;
import com.hp.hpl.jena.sparql.sse.builders.BuilderTable;
import com.hp.hpl.jena.sparql.sse.lang.ParseHandler;
import com.hp.hpl.jena.sparql.sse.lang.ParseHandlerPlain;
import com.hp.hpl.jena.sparql.sse.lang.ParseHandlerResolver;
import com.hp.hpl.jena.sparql.sse.lang.SSE_Parser;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.util.FileUtils;

public class SSE
{
    private SSE() {}
    
    // Short prefix map for convenience (used in parsing, not in writing).
    protected static PrefixMapping defaultDefaultPrefixMapRead = new PrefixMappingImpl() ;
    static {
        defaultDefaultPrefixMapRead.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("owl" , ARQConstants.owlPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("fn" ,  ARQConstants.fnPrefix) ; 
        defaultDefaultPrefixMapRead.setNsPrefix("ex" ,  "http://example/ns#") ;
        defaultDefaultPrefixMapRead.setNsPrefix("" ,    "http://example/") ;
    }
    
    public static PrefixMapping defaultPrefixMapRead = defaultDefaultPrefixMapRead ;
    public static PrefixMapping getDefaultPrefixMapRead() { return defaultPrefixMapRead ; }
    public static void setDefaultPrefixMapRead(PrefixMapping pmap) { defaultPrefixMapRead =  pmap ; }
    
    // Short prefix map for convenience used in writing.
    protected static PrefixMapping defaultDefaultPrefixMapWrite = new PrefixMappingImpl() ;
    static {
        defaultDefaultPrefixMapWrite.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        defaultDefaultPrefixMapWrite.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        defaultDefaultPrefixMapWrite.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;
    }
    
    public static PrefixMapping defaultPrefixMapWrite = defaultDefaultPrefixMapWrite ;
    public static PrefixMapping getDefaultPrefixMapWrite() { return defaultPrefixMapWrite ; }
    public static void setDefaultPrefixMapWrite(PrefixMapping pmap) { defaultPrefixMapWrite =  pmap ; }
    
    /** Parse a string to obtain a Node */
    public static Node parseNode(String str) { return parseNode(str, null) ; }
    
    /** Parse a string to obtain a Node */
    public static Node parseNode(String str, PrefixMapping pmap)
    { 
        return parseNode(new StringReader(str), pmap) ;
    }
    
    /** Parse a string to obtain a Quad */
    public static Quad parseQuad(String s) { return parseQuad(s, null) ; }
    
    /** Parse a string to obtain a Quad */
    public static Quad parseQuad(String s, PrefixMapping pmap)
    {
        Item item = parse(s, pmap) ;
        if ( !item.isList() )
            throw new ARQException("Not a list: "+s) ; 
        return BuilderGraph.buildQuad(item.getList()) ;
    }

    /** Parse a string to obtain a Triple */
    public static Triple parseTriple(String s) { return parseTriple(s, null) ; }
    
    /** Parse a string to obtain a Triple */
    public static Triple parseTriple(String s, PrefixMapping pmap)
    {
        Item item = parse(s, pmap) ;
        if ( !item.isList() )
            throw new ARQException("Not a list: "+s) ; 
        return BuilderGraph.buildTriple(item.getList()) ;
    }
    
    /** Parse a string to obtain a SPARQL expression  */
    public static Expr parseExpr(String s) { return parseExpr(s, null) ; }
    
    /** Parse a string to obtain a SPARQL expression  */
    public static Expr parseExpr(String s, PrefixMapping pmap)
    { 
        Item item = parse(s, pmap) ;
        return BuilderExpr.buildExpr(item) ;
    }
    
    /** Read in a file, parse, and obtain a graph */
    public static Graph readGraph(String filename) { return readGraph(filename, null) ; }
    
    /** Read in a file, parse, and obtain a graph */
    public static Graph readGraph(String filename, PrefixMapping pmap)
    {
        Item item = readFile(filename, pmap) ;
        return BuilderGraph.buildGraph(item) ;
    }
    
    /** Read in a file, parse, and obtain a graph */
    public static void readGraph(Graph graph, String filename) { readGraph(graph, filename, null) ; }
    
    /** Read in a file, parse, and obtain a graph */
    public static void readGraph(Graph graph, String filename, PrefixMapping pmap)
    {
        Item item = readFile(filename, pmap) ;
        BuilderGraph.buildGraph(graph, item) ;
    }
    
    /** Read in a file, parse, and obtain a SPARQL algebra op */
    public static Op readOp(String filename) { return Algebra.read(filename) ; }
    
    /** Parse a string and obtain a SPARQL algebra op */
    public static Op parseOp(String s) { return Algebra.parse(s) ; }
    
    /** Parse a string and obtain a SPARQL algebra op, given a prefix mapping */
    public static Op parseOp(String s, PrefixMapping pmap) { return Algebra.parse(s, pmap) ; }

    /** Read in a file, parse, and obtain a SPARQL algebra basic graph pattern */
    public static BasicPattern readBGP(String filename)
    { 
        Item item = readFile(filename, null) ;
        return BuilderOp.buildBGP(item) ;
    }    
    
    /** Parse a string and obtain a SPARQL algebra basic graph pattern */
    public static BasicPattern parseBGP(String s)
    { return parseBGP(s, getDefaultPrefixMapRead()) ; }
    
    /** Parse a string and obtain a SPARQL algebra basic graph pattern, given a prefix mapping */
    public static BasicPattern parseBGP(String s, PrefixMapping pmap)
    { 
        Item item = parse(s, pmap) ;
        return BuilderOp.buildBGP(item) ;
    }
    
    /** Read a file and obtain a SPARQL algebra table */
    public static Table readTable(String filename) { return readTable(filename, null) ; }
    
    /** Read a file and obtain a SPARQL algebra table */
    public static Table readTable(String filename, PrefixMapping pmap)
    { 
        Item item = readFile(filename, pmap) ;
        return BuilderTable.build(item) ;
    }
    
    /** Parse a string and obtain a SPARQL algebra table */
    public static Table parseTable(String s) { return parseTable(s, null) ; }

    /** Parse a string and obtain a SPARQL algebra table */
    public static Table parseTable(String s, PrefixMapping pmap)
    { 
        Item item = parse(s, pmap) ;
        return BuilderTable.build(item) ;
    }

    /** Read a file and obtain an SSE item expression */
    public static Item readFile(String filename)
    { return readFile(filename, null) ; }

    
    /** Read a file and obtain an SSE item expression */
    public static Item readFile(String filename, PrefixMapping pmap)
    {
        try {
            FileInputStream in = new FileInputStream(filename) ;
            long len = in.getChannel().size() ;
            if ( len == 0 )
                return Item.nil ;
            return parse(in, pmap) ;
        } 
        catch (FileNotFoundException ex)
        { throw new NotFoundException("Not found: "+filename) ; } 
        catch (IOException ex)
        { throw new ARQException("IOExeption: "+filename, ex) ; }
    }
    
    /** Parse a string and obtain an SSE item expression (no additional prefix mappings)*/
    public static Item parseRaw(String str) { return parse(str, new PrefixMappingImpl()) ; }
    
    /** Parse a string and obtain an SSE item expression */
    public static Item parse(String str) { return parse(str, null) ; }

    /** Parse a string and obtain an SSE item expression */
    public static Item parse(String str, PrefixMapping pmap)
    {
        return parse(new StringReader(str), pmap) ;
    }

    /** Parse from an input stream and obtain an SSE item expression */
    public static Item parse(InputStream in) { return parse(in, null) ; }

    /** Parse from an input stream and obtain an SSE item expression */
    public static Item parse(InputStream in, PrefixMapping pmap)
    {
        Reader reader = FileUtils.asBufferedUTF8(in) ;
        return parse(reader, pmap) ;
    }
    
    // ---- Workers
    
    public static void setUseResolver(boolean flag) { useResolver = flag ; }
    private static boolean useResolver = true ;
    
    private static ParseHandler createParseHandler(PrefixMapping pmap)
    {
        if ( useResolver )
        {
            Prologue prologue = new Prologue(pmap) ;
            return new ParseHandlerResolver(prologue) ;
        }
        else
            return new ParseHandlerPlain() ;
    }
    
    private static Node parseNode(Reader reader, PrefixMapping pmap)
    {
        Item item = parseTerm(reader, pmap) ;
        if ( ! item.isNode() )
            throw new SSEParseException("Not a node: "+item, item.getLine(), item.getColumn()) ;
        return item.getNode() ;
    }

    private static String parseSymbol(Reader reader, PrefixMapping pmap)
    {
        Item item = parseTerm(reader, pmap) ;
        if ( ! item.isSymbol() )
            throw new SSEParseException("Not a symbol: "+item, item.getLine(), item.getColumn()) ;
        return item.getSymbol() ;
    }
    
    public static Item parseItem(String str)
    {
        return parse(str, null) ;
    }

    public static Item parseItem(String str, PrefixMapping pmap)
    {
        return parse(new StringReader(str), pmap) ;
    }
    
    // --- Parse single elements. 
    
    private static Item parseTerm(Reader reader, PrefixMapping pmap)
    {
        if ( pmap == null )
            pmap = getDefaultPrefixMapRead() ;
        ParseHandler handler = createParseHandler(pmap) ;
        SSE_Parser.term(reader, handler) ; 
        return handler.getItem() ;
    }

    private static Item parse(Reader reader, PrefixMapping pmap)
    {
        if ( pmap == null )
            pmap = getDefaultPrefixMapRead() ;
        ParseHandler handler = createParseHandler(pmap) ;
        SSE_Parser.parse(reader, handler) ; 
        return handler.getItem() ;
    }
    
    // ---- To String
    public static String format(Node node)                      { return FmtUtils.stringForNode(node) ; }
    public static String format(Node node, PrefixMapping pmap)  { return FmtUtils.stringForNode(node, pmap) ; }
    
    // ----
    
    public static void write(Op op) { WriterOp.output(IndentedWriter.stdout, op) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, Op op) { WriterOp.output(out, op) ; }
    public static void write(IndentedWriter out, Op op) { WriterOp.output(out, op) ; }

    public static void write(Graph graph)
    { 
        WriterGraph.output(IndentedWriter.stdout, graph, 
                           new SerializationContext(graph.getPrefixMapping())) ;
        IndentedWriter.stdout.flush() ;
    }
    public static void write(OutputStream out, Graph graph)
    { 
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, graph) ;
        iOut.flush();
    }
        
    public static void write(IndentedWriter out, Graph graph)
    { 
        WriterGraph.output(out, graph, 
                           new SerializationContext(graph.getPrefixMapping())) ;
    }

    
    
    
    public static void write(DatasetGraph dataset) { write(IndentedWriter.stdout, dataset) ; IndentedWriter.stdout.flush() ; } 
    public static void write(OutputStream out, DatasetGraph dataset)
    { 
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, dataset) ;
        iOut.flush();
    }
        
    public static void write(IndentedWriter out, DatasetGraph dataset)  
    { 
        WriterGraph.output(out, dataset, sCxt(dataset.getDefaultGraph())) ;
    }

    public static void write(Dataset dataset)                       { write(dataset.asDatasetGraph()) ; } 
    public static void write(OutputStream out, Dataset dataset)     { write(out, dataset.asDatasetGraph()) ; } 
    public static void write(IndentedWriter out, Dataset dataset)   { write(out, dataset.asDatasetGraph()) ; }

    public static void write(BasicPattern pattern)                  { write(IndentedWriter.stdout, pattern) ; IndentedWriter.stdout.flush() ; }
    
    public static void write(IndentedWriter out, BasicPattern pattern)
    { write(IndentedWriter.stdout, pattern, null) ; IndentedWriter.stdout.flush() ; }
    
    public static void write(IndentedWriter out, BasicPattern pattern, PrefixMapping pMap)
    {
        WriterGraph.output(out, pattern, sCxt(pMap)) ;
        out.flush() ;
    }
    
    public static void write(Triple triple) { write(IndentedWriter.stdout, triple) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, Triple triple)
    { 
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, triple) ;
        iOut.flush();
    }
    public static void write(IndentedWriter out, Triple triple)                         
    { 
        WriterNode.output(out, triple, sCxt(defaultDefaultPrefixMapWrite)) ; 
        out.flush() ;
    }
    
    public static void write(Node node) { write(IndentedWriter.stdout, node) ; IndentedWriter.stdout.flush() ; }
    public static void write(OutputStream out, Node node)
    { 
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, node) ;
        iOut.flush();
    }
    public static void write(IndentedWriter out, Node node)                         
    { 
        WriterNode.output(IndentedWriter.stdout, node, sCxt(defaultDefaultPrefixMapWrite)) ;
        IndentedWriter.stdout.flush() ;
    }
    
    /** Return a SerializationContext appropriate for the graph */
    public static SerializationContext sCxt(Graph graph)
    {
        if ( graph != null )
            return sCxt(graph.getPrefixMapping()) ;
        return new SerializationContext() ;
    }  
    
    /** Return a SerializationContext appropriate for the prfix mapping */
    public static SerializationContext sCxt(PrefixMapping pmap)
    {
        if ( pmap != null )
            return new SerializationContext(pmap) ;
        return new SerializationContext() ;
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