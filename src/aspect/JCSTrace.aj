

aspect JCSTrace {
    /**
     * Application classes.
     */
     // turned it off
    pointcut myClass(): !within(org.apache.stratum.*) ;
    /**
     * The constructors in those classes.
     */
    pointcut myConstructor(): myClass() && execution(new(..));
    /**
     * The methods of those classes.
     */
    pointcut myMethod(): myClass() && execution(* *(..));

    /**
     * Prints trace messages before and after executing constructors.
     */
    before (): myConstructor() {
        Trace.traceEntry("" + thisJoinPointStaticPart.getSignature());
    }
    after(): myConstructor() {
        Trace.traceExit("" + thisJoinPointStaticPart.getSignature());
    }

    /**
     * Prints trace messages before and after executing methods.
     */
    before (): myMethod() {
        Trace.traceEntry("" + thisJoinPoint.getSignature());
    }
    after(): myMethod() {
        Trace.traceExit("" + thisJoinPoint.getSignature());
    }

}

