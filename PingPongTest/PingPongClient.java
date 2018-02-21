package PingPongTest;

import rmi.RMIException;
import rmi.Stub;

import java.net.InetSocketAddress;

/**
 * Client to Invoke 'ping' method On the server
 * Need to Pass in 1.IP address & 2.Port Number
 */

public class PingPongClient {
    public static void main(String[] args) throws RMIException{
        if(args.length < 2){
            System.out.println("Invalid Arguments!");
            return;
        }
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        InetSocketAddress address = new InetSocketAddress(ip, port);
        /* Client Use RMI registry to get a reference to the factory */
        /* Generate stub Using The network address of the remote skeleton */
        PingPongFactory factory = Stub.create(PingPongFactory.class, address);
        /* Use factory to get Reference to the PingPongServer */
        PingPongServer ping_pong_server = factory.makePingPongServer();
        /* Test the PingPongServer 4 times */
        int fail_count = 0;
        for(int i=0; i<4; i++){
            try{
                String return_value = ping_pong_server.ping(i);
                if(!return_value.equals("Pong-"+i)){
                    fail_count ++;
                }
            }
            catch (RMIException e){
                fail_count ++;
            }
        }

        System.out.println("4 Tests Completed, "+fail_count+" Tests Failed");
    }
}
