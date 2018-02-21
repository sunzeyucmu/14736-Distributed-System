package rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ListenerThread is the listening thread at Skeleton server side, accepting connections from client(Stub)
 * @ Chenxuan Weng & Zeyu Sun
 */
public  class ListenerThread<T> extends Thread{
    private ServerSocket socket;

    private Skeleton<T> skeleton; //Related Skeleton (Listen on Connections on This Skeleton Server)

    protected boolean stop_status;

    public ListenerThread(ServerSocket skeleton_server_socket, Skeleton<T> skeleton){
        this.socket = skeleton_server_socket;
        this.skeleton = skeleton;
        stop_status = false; // Skeleton's stop method has not been called
    }

    public synchronized void terminate() {
        try {
            if(!this.socket.isClosed()){
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        /* One of the way to create a new thread of execution is
           to declare a class to be a subclass of Thread.
           This subclass should override the run method of class Thread.
           An instance of the subclass can then be allocated and started.
         */

    @Override
    public void run(){
        try{
            // Block of code with multiple exit points
                /* Run the Skeleton MultiThread Server */
            while(true){
                try{
                        /* Listening for connections (Method Call)
                           And Create Service Thread to handle the Remote Method Call When connections areaccepted.
                         */
                    Socket connection = this.socket.accept();
                    System.out.println("New Connection!");
                    ServiceThread<T> service_thread = new ServiceThread(connection, this.skeleton);
                    service_thread.start();
                }
                catch(Exception e){
                    if(stop_status == true){
                            /* Skeleton's 'stop' is called, the Skeleton Server needs to stop
                             * listener Thread need to exit due to call to 'stop'
                             * */
                        break;
                    }
                        /* Exceptions may occur at the top level in the listening and service threads. */
                    else if(skeleton.listen_error(e)){
                            /* An exception occurs at the top level in the listening thread */
                            /* The Server Needs to Resume Accepting Connections Now */
                        continue;
                    }
                    else{
                        // Skeleton Server Has to Stop
                        // The Listener Thread needs to exits
                        skeleton.stopped(e);
                        break;
                    }
                }
            }
        }
        finally {
            // Block of code that is always executed when the try block is exited,
            // no matter how the try block is exited
                /* Close the Socket */
            try {
                if(!this.socket.isClosed()){
                    this.socket.close();
                }
            }
            catch (IOException e){
                System.out.println("CLose of the Skeleton Server Socket failed!");
                e.printStackTrace();
            }
        }
    }
}

