package org.apache.jcs.utils.servlet.session;

import java.rmi.dgc.VMID;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class SessionIdGenerator
{

    private static int[] count = new int[1];
    /** Description of the Field */
    public final static String JVM_ID = new VMID().toString();


    /** Creates a unique session id. */
    public static String createSessionId()
    {
        synchronized ( count )
        {
            return JVM_ID + count[0]++;
        }
    }

}
