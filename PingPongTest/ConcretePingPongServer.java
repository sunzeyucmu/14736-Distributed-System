package PingPongTest;

import rmi.RMIException;

public class ConcretePingPongServer implements PingPongServer{
    @Override
    public String ping(int idNumber) throws RMIException{
        return "Pong"+"-"+idNumber;
    }
}
