package org.apache.commons.jcs;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.log4j.Category;

/**
 * This class provides support for printing trace messages into a
 * log4j category.
 * 
 * The messages are appended with the string representation of the objects
 * whose constructors and methods are being traced.
 *
 * @author <a href="mailto:jvanzyl@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public aspect TraceJCS
{
    /*
     * Functional part
     */

    /**
     * There are 3 trace levels (values of TRACELEVEL):
     * 
     * 0 - No messages are printed
     * 1 - Trace messages are printed, but there is no indentation 
     *     according to the call stack
     * 2 - Trace messages are printed, and they are indented
     *     according to the call stack
     */
    public static int TRACELEVEL = 2;
    
    /**
     * Tracks the call depth for indented traces made according
     * to the structure of the stack.
     */
    protected static int callDepth = 0;
    
    /**
     * Log4j category used for tracing.
     */
    private static Category log = Category.getInstance( TraceJCS.class );
    
    /**
     * Tracing method used in before advice.
     */
    protected static void traceEntry(String str, Object o) 
    {
        if (TRACELEVEL == 0) 
        {
            return;
        }            
        
        if (TRACELEVEL == 2) 
        {
            callDepth++;
        }            
        printEntering(str + ": " + o.toString());
    }

    /**
     * Tracing method used in after advice.
     */
    protected static void traceExit(String str, Object o) 
    {
        if (TRACELEVEL == 0) 
        {
            return;
        }            
        
        printExiting(str + ": " + o.toString());
        
        if (TRACELEVEL == 2) 
        {
            callDepth--;
        }            
    }
    
    private static void printEntering(String str) 
    {
        log.debug(indent() + "--> " + str);
    }

    private static void printExiting(String str) 
    {
        log.debug(indent() + "<-- " + str);
    }

    private static String indent() 
    {
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < callDepth; i++)
        {
            sb.append("  ");
        }            
    
        return sb.toString(); 
    }

    /*
     * Crosscut part
     */

    /**
     * JCS Application classes
     */
    pointcut myClass(Object obj): this(obj) && 
        (within(org.apache.stratum.jcs..*)); 

    /**
     * The constructors in those classes.
     */
    pointcut myConstructor(Object obj): myClass(obj) && execution(new(..));
    
    /**
     * The methods of those classes.
     */
    pointcut myMethod(Object obj): myClass(obj) && 
        execution(* *(..)) && !execution(String toString());

    /**
     * Before advice that will execute before a constructor
     * is invoked.
     */
    before(Object obj): myConstructor(obj) 
    {
        traceEntry("" + thisJoinPointStaticPart.getSignature(), obj);
    }
    
    /**
     * After advice that will execute after a constructor
     * a constructor has been invoked.
     */
    after(Object obj): myConstructor(obj) 
    {
        traceExit("" + thisJoinPointStaticPart.getSignature(), obj);
    }
    
    /**
     * Before advice that will execute before a method
     * is invoked.
     */
    before(Object obj): myMethod(obj) 
    {
        traceEntry("" + thisJoinPointStaticPart.getSignature(), obj);
    }
    
    /**
     * After advice that will execute after a method
     * has been invoked.
     */
    after(Object obj): myMethod(obj) 
    {
        traceExit("" + thisJoinPointStaticPart.getSignature(), obj);
    }
}
