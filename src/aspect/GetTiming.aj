import org.aspectj.lang.*;

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

aspect GetTiming {

//   static final void println(String s){ System.out.println(s); }

//   pointcut allExecs(): !within(GetTiming) && cflow(this(com.realm.arch.proxy.PageProxyServlet)) && execution(* *(..));

  pointcut allExecs():

//       !within(GetTiming)
     !withincode(void org.apache.jcs.utils.log.Logger.*(..))
     && !withincode(void org.apache.jcs.utils.log.Logger.*(..))
   && (cflow(this(org.apache.jcs.engine.control.Cache))
//    || cflow(this(org.apache.jcs.engine.group.GroupCache))
    )
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
      if (delta >= 0) {
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
