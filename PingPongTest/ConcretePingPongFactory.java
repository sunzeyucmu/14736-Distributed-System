package PingPongTest;

import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

/**
 * Object Class Implementing Remote Interface 'PingPongFactory'
 */

public class ConcretePingPongFactory implements PingPongFactory{
//    private Skeleton<PingPongServer> ping_skeleton;
//    private ConcretePingPongServer server;
    @Override
    public PingPongServer makePingPongServer() throws RMIException {
        /* Create a New PingPongServer and return it as (remote object reference */
        /* Here We Need to Return The Stub */
                return new ConcretePingPongServer();
//        server = new ConcretePingPongServer();
//        ping_skeleton = new Skeleton<PingPongServer>(PingPongServer.class, server);
//        ping_skeleton.start();
//        return Stub.create(PingPongServer.class, ping_skeleton.getSkeleton_address());
    }
}
