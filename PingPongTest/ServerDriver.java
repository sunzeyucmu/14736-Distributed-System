package PingPongTest;

import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetSocketAddress;

/**
 * Launch The Skeleton on the Server Side
 * which handle Calls to the Remote interface
 */

public class ServerDriver {
    public static void main(String[] args) throws RMIException{
//        rr;
        /* Get the Skeleton Server Running */
        int port = Integer.parseInt(args[0]);
        InetSocketAddress address = new InetSocketAddress("localhost",port);
        System.out.println("The address at which the skeleton(Listening Thread) is to run: "+address.toString());
        Class remote_interface = PingPongFactory.class;
        /* Remote Object Implementing the Remote interface */
        ConcretePingPongFactory remote_object = new ConcretePingPongFactory();
        Skeleton<PingPongFactory> skeleton = new Skeleton<PingPongFactory>(remote_interface, remote_object, address);

        /* Start the Skeleton Server */
        skeleton.start();
    }
}
