import org.aspectj.lang.*;

aspect ShowSlow {

//   static final void println(String s){ System.out.println(s); }

//   pointcut allExecs(): !within(GetTiming) && cflow(this(com.realm.arch.proxy.PageProxyServlet)) && execution(* *(..));

  pointcut allExecs():

//       !within(GetTiming)
     !withincode(void org.apache.jcs.utils.log.Logger.*(..))
     && !withincode(void org.apache.jcs.utils.log.Logger.*(..))
//  && (cflow(this(org.apache.jcs.engine.control.Cache))
//    || cflow(this(org.apache.jcs.engine.group.GroupCache))
//    )
//    && !withincode(* *.getValueObj(..))
//
//    && execution(* *(..));

      && execution(* *(..));


   Object around(): allExecs() {
      long start = System.currentTimeMillis();
      String s = thisJoinPointStaticPart.getSignature().getName()
        + " in class: "
        + thisJoinPointStaticPart.getSignature().getDeclaringType().getName();
      	//+ printParameters(thisJoinPoint);
      Object result = proceed();
      long delta = System.currentTimeMillis() - start;
      if (delta > 10) {
        System.out.println(delta+ " ms: "+s);
      }
      return result;
   }

/*
   static private void printParameters(JoinPoint jp) {
      System.out.println("Arguments: " );
      Object[] args = jp.getArgs();
      String[] names = ((CodeSignature)jp.getSignature()).getParameterNames();
      Class[] types = ((CodeSignature)jp.getSignature()).getParameterTypes();
      for (int i = 0; i < args.length; i++) {
	      System.out.println("  "  + i + ". " + names[i] +
	     " : " +            types[i].getName() +
	     " = " +            args[i]);
      }
   }
*/

}
