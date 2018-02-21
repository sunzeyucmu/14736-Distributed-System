package PingPongTest;

import rmi.RMIException;

/**
 * Implement 'ping' method
 */

public interface PingPongServer {
    public String ping(int idNumber) throws RMIException;
}
