package PingPongTest;

import rmi.RMIException;

/**
 * The Remote Interface
 * contains a ”PingServer makePingPongServer()” method
 * that should create a new PingServer and return it (as a remote object reference).
 */

public interface PingPongFactory{
    public PingPongServer makePingPongServer() throws RMIException;
}
