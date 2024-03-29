/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class EARL
{
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/earl#";

    
    private static Resource resource(String name)
    { return ResourceFactory.createResource(NS+name) ; }

    private static Property property(String name)
    { return ResourceFactory.createProperty(NS+name) ; }
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = resource( NS );
    
    public static final Property assertedBy =property( "assertedBy" );
    
    public static final Property info = property( "info" );
    
    public static final Property outcome = property( "outcome" );
    
    public static final Property mode = property( "mode" );
    
    /** <p>Assertor assisting to determine assertion result</p> */
    public static final Property helpAssertor = property( "helpAssertor" );
    
    public static final Property result = property( "result" );
    
    /** <p>Assertor mainly responsible for determining assertion result</p> */
    public static final Property mainAssertor = property( "mainAssertor" );
    
    public static final Property pointer = property( "pointer" );
    
    public static final Property sourceCopy = property( "sourceCopy" );
    
    public static final Property test = property( "test" );
    
    public static final Property subject = property( "subject" );
    
    public static final Property context = property( "context" );
    
    /** <p>Group of persons or evaluation tools that claim assertions</p> */
    public static final Resource CompoundAssertor = resource( "CompoundAssertor" );
    
    /** <p>Nominal value of the result</p> */
    public static final Resource OutcomeValue = resource( "OutcomeValue" );
    
    /** <p>Result from conducting test cases on subjects</p> */
    public static final Resource TestResult = resource( "TestResult" );
    
    /** <p>A test case against which subjects are tested</p> */
    public static final Resource TestCase = resource( "TestCase" );
    
    /** <p>One person or evaluation tool that claims assertions</p> */
    public static final Resource SingleAssertor = resource( "SingleAssertor" );
    
    /** <p>A testable statement against which subjects are tested</p> */
    public static final Resource TestCriterion = resource( "TestCriterion" );
    
    /** <p>Parent node that contains all parts of an assertion</p> */
    public static final Resource Assertion = resource( "Assertion" );
    
    /** <p>Mode in which tests were conducted</p> */
    public static final Resource TestMode = resource( "TestMode" );
    
    /** <p>Subjects that are available on the Web</p> */
    public static final Resource Content = resource( "Content" );
    
    /** <p>A tool that can perform tests or be the subject of testing</p> */
    public static final Resource Software = resource( "Software" );
    
    /** <p>A requirement against which subjects are tested</p> */
    public static final Resource TestRequirement = resource( "TestRequirement" );
    
    /** <p>Subject of the assertion</p> */
    public static final Resource TestSubject = resource( "TestSubject" );
    
    /** <p>Persons or evaluation tools that claim assertions</p> */
    public static final Resource Assertor = resource( "Assertor" );
    
    
    /** <p>Test failed</p> */
    public static final Resource fail = resource( "fail" );
    
    /** <p>Test has not been carried out</p> */
    public static final Resource notTested = resource( "notTested" );
    
    /** <p>Test passed</p> */
    public static final Resource pass = resource( "pass" );
    
    /** <p>Test was performed primarily by a tool, and human assistance</p> */
    public static final Resource semiAutomatic = resource( "semiAutomatic" );
    
    /** <p>Test is not applicable to the subject</p> */
    public static final Resource notApplicable = resource( "notApplicable" );
    
    /** <p>Result was derived from other results</p> */
    public static final Resource heuristic = resource( "heuristic" );
    
    /** <p>Test was performed by a tool only</p> */
    public static final Resource automatic = resource( "automatic" );
    
    /** <p>Test was performed by a human only</p> */
    public static final Resource manual = resource( "manual" );
    
    /** <p>Outcome of the test is uncertain</p> */
    public static final Resource cannotTell = resource( "cannotTell" );
    
    /** <p>Test was performed by a combination of persons and tools</p> */
    public static final Resource notAvailable = resource( "notAvailable" );
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