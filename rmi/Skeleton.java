package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.*;

import java.net.*;

import java.lang.reflect.*;

/** RMI skeleton
 *
 * To Make the Server Object Remotely-accessible

    <p>
    A skeleton encapsulates a multithreaded TCP server. The server's clients are
    intended to be RMI stubs created using the 'Stub' class.

    <p>
    The skeleton class is parametrized by a type variable. This type variable
    should be instantiated with an interface. The skeleton will accept from the
    stub requests for calls to the methods of this interface. It will then
    forward those requests to an object. The object is specified when the
    skeleton is constructed, and must implement the remote interface. Each
    method in the interface should be marked as throwing
    <code>RMIException</code>, in addition to any other exceptions that the user
    desires.

    <p>
    Exceptions may occur at the top level in the listening and service threads.
    The skeleton's response to these exceptions can be customized by deriving
    a class from <code>Skeleton</code> and overriding <code>listen_error</code>
    or <code>service_error</code>.
*/
public class Skeleton<T>
{
    /* The Skeleton class's type variable 'T' is corresponding to the remote interface */

    /*
        Object representing the class of the interface for which
        the skeleton server is to handle method call requests
     */
    protected Class<T> remote_interface_c;

    protected T remoteObject; //Object implementing the remote interface ('Server' Class' )

    protected InetSocketAddress skeleton_address; //The address at which the skeleton is to run

    protected ServerSocket skeleton_server_socket; //Used by Skeleton Server to listen for Clients

    private ListenerThread<T> listener;
    protected final List<ServiceThread> service_thread_list = new LinkedList<ServiceThread>(); //List for All Service Threads Created

    /* -------- Helper Functions -------- */

    /**
     * Return the address(InetSocketAddress)  at which the skeleton is to run.
     */
    public InetSocketAddress getSkeleton_address() {
        return skeleton_address;
    }

    /**
     *
     * All constructors of Skeleton Must reject interfaces which are not remote interfaces
     *
     * Check whether interface'smethods are all marked as throwing RMIException.
     * @return true or false
     */
    private boolean isRemoteInterface(Class<?> c){
        if(!c.isInterface()){ //Determines if the specified Class object represents an interface type
            return false;
        }
        /* Check every methods declared by interface object c */
        for(Method m : c.getDeclaredMethods()){
            boolean containRMIException = false;
            for(Class exceptionClassType : m.getExceptionTypes()){
                if(exceptionClassType.getName().equals("rmi.RMIException")){
                    containRMIException = true;
                    break;
                }
            }
            if(!containRMIException)return false;
        }

        /* All Mehods implement RMIException */
        return true;
    }

//    /**
//     * ListenerThread is the listening thread at Skeleton server side, accepting connections from client(Stub)
//     * @ Chenxuan Weng & Zeyu Sun
//     */
//    private  class ListenerThread extends Thread{
//        private ServerSocket socket;
//
//        private boolean stop_status;
//
//        public ListenerThread(ServerSocket skeleton_server_socket){
//            this.socket = skeleton_server_socket;
//            stop_status = false; // Skeleton's stop method has not been called
//        }
//
//        public synchronized void terminate() {
//            try {
//                if(!this.socket.isClosed()){
//                    this.socket.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        /* One of the way to create a new thread of execution is
//           to declare a class to be a subclass of Thread.
//           This subclass should override the run method of class Thread.
//           An instance of the subclass can then be allocated and started.
//         */
//
//        @Override
//        public void run(){
//            try{
//                // Block of code with multiple exit points
//                /* Run the Skeleton MultiThread Server */
//                while(true){
//                    try{
//                        /* Listening for connections (Method Call)
//                           And Create Service Thread to handle the Remote Method Call When connections areaccepted.
//                         */
//                        Socket connection = this.socket.accept();
//                        ServiceThread<T> service_thread = new ServiceThread(connection, this);
//                        service_thread.start();
//                    }
//                    catch(Exception e){
//                        if(stop_status == true){
//                            /* Skeleton's 'stop' is called, the Skeleton Server needs to stop
//                             * listener Thread need to exit due to call to 'stop'
//                             * */
//                            break;
//                        }
//                        /* Exceptions may occur at the top level in the listening and service threads. */
//                        else if(listen_error(e)){
//                            /* An exception occurs at the top level in the listening thread */
//                            /* The Server Needs to Resume Accepting Connections Now */
//                            continue;
//                        }
//                        else{
//                            // Skeleton Server Has to Stop
//                            // The Listener Thread needs to exits
//                            stopped(e);
//                            break;
//                        }
//                    }
//                }
//            }
//            finally {
//                // Block of code that is always executed when the try block is exited,
//                // no matter how the try block is exited
//                /* Close the Socket */
//                try {
//                    if(!this.socket.isClosed()){
//                        this.socket.close();
//                    }
//                }
//                catch (IOException e){
//                    System.out.println("CLose of the Skeleton Server Socket failed!");
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    /**
//     * Additional Service threads are created when connections are accepted.
//     * These Threads will read and parse method calls(name & arguments) forwarded by Stub Object
//     * (Stub will open a single connection per method call
//     * Then call the correct methods on the server Object implementing Remote Interface
//     * When a method returns, the return value (or exception) is sent over the network to the client
//     */
//    private class ServiceThread extends Thread{
//        private Socket connection;
//
//        /* Register this Service Thread in 'service_thread_list' */
//        public ServiceThread(Socket connection_socket){
//            this.connection = connection_socket;
//            service_thread_list.add(this);
//        }
//        /* override the run method of class Thread */
//        @Override
//        public void run(){
//            /* Arguments & Results Transmition
//             * Serialize the Communication
//             * */
//            ObjectOutputStream out = null;
//            ObjectInputStream in = null;
//
//
//            try{
//                /* Ensure the out stream is created first, and be flushed before creating input stream
//                   To Avoid Deadlock
//                 */
//                out = new ObjectOutputStream(connection.getOutputStream());
//                out.flush();
//                in = new ObjectInputStream(connection.getInputStream());
//                /* Parse Information Regard Method Call */
//                String method_name = (String)in.readObject(); // Name of the Method
//                Class<?>[] args_type = (Class<?>[]) in.readObject(); //Type for each Argument
//                Object[] args = (Object[]) in.readObject(); //Arguments
//
//                /* Retrieve the required method on the server */
//                Method method = remote_interface_c.getMethod(method_name, args_type);
//
//                Class return_type = method.getReturnType();
//
//                /* Invoke the Method
//                 * Invokes the underlying method represented by this Method object(method),
//                 * on the specified object (object implementing remote Interface) with the specified parameters
//                 * */
//                try{
//                    Object return_value = method.invoke(remoteObject, args);
//
//                    /* Return The Method Call Result */
//                    /* ### Just For Now */
//                    out.writeObject("Remote Method Call Succeeded!");
//                    out.writeObject(return_value);
//                }
//                catch (InvocationTargetException e){//(IllegalAccessException | InvocationTargetException e){
//                    /* Send Back the Exception to Client
//                     * If the remote method raises an exception,
//                     * the Stub must raise the same exception,
//                     * */
//                    out.writeObject("Remote Method Call Failed!");
//                    out.writeObject(e.getTargetException());
//                }
//
//            }
//            catch(Exception exception){
//                /* an exception occurs at the top level in a service thread */
//                service_error(new RMIException((exception)));
//            }
//            finally {
//                // executed when the try block is exited
//                /* service method's result has been returned
//                   Close the Serializable Streams
//                   Close the Connection
//                 */
//                service_thread_list.remove(this);
//                try{
//                    if(out != null){
//                        out.flush();
//                        out.close();
//                    }
//                }
//                catch (IOException e){
//                    System.out.println("Out Stream");
//                    e.printStackTrace();
//                }
//                try{
//                    if(in != null){
//                        in.close();
//                    }
//                }
//                catch (IOException e){
//                    System.out.println("In Stream");
//                    e.printStackTrace();
//                }
//                try{
//                    this.connection.close();
//                }
//                catch (IOException e){
//                    System.out.println("Connection Close");
//                    e.printStackTrace();
//                }
//            }
//
//
//        }
//
//    }

    /** Creates a <code>Skeleton</code> with no initial server address. The
        address will be determined by the system when <code>start</code> is
        called. Equivalent to using <code>Skeleton(null)</code>.

        <p>
        This constructor is for skeletons that will not be used for
        bootstrapping RMI - those that therefore do not require a well-known
        port.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are $$$forwarded by the skeleton to this object$$$.
        @throws Error If <code>c</code> $$$does not represent a remote interface$$$ -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of 'c' or 'server' is null.
     */
    public Skeleton(Class<T> c, T server)
    {
        System.out.println("For Debug: Skeleton Construct without address Called!" );
        /* If either of c or server is null */
        if(c == null || server == null)throw new  NullPointerException();

        /* Check if c is remote interface */
        if(!isRemoteInterface(c)) throw new  Error("Interface: "+c.getSimpleName()+" does not represent a remote interface");
        //        throw new UnsupportedOperationException("not implemented");
        this.remote_interface_c = c;
        remoteObject = server;
        skeleton_address = null;
    }

    /** Creates a <code>Skeleton</code> with the given initial server address.

        <p>
        This constructor should be used when the port number is significant.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @param address The address at which the skeleton is to run. If
                       <code>null</code>, the address will be chosen by the
                       system when <code>start</code> is called.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
        System.out.println("For Debug: Skeleton Construct with address Called! "+address.toString());
        /* If either of c or server is null */
        if(c == null || server == null)throw new  NullPointerException();

        /* Check if c is remote interface */
        if(!isRemoteInterface(c)) throw new  Error("Interface: "+c.getSimpleName()+" does not represent a remote interface");
        //        throw new UnsupportedOperationException("not implemented");
        this.remote_interface_c = c;
        remoteObject = server;
        skeleton_address = address;
    }

    /** Called when the listening thread exits.

        <p>
        The listening thread may exit due to a top-level exception, or due to a
        call to <code>stop</code>.

        <p>
        When this method is called, the calling thread owns the lock on the
        <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
        calling <code>start</code> or <code>stop</code> from different threads
        during this call.

        <p>
        The default implementation does nothing.

        @param cause The exception that stopped the skeleton, or
                     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
        System.out.println("For Debug: Enter stopped()");
        if(cause == null){
            System.out.println("The Skeleton Stopped Normally");
        }
        else{
            throw new Error("The Skeleton Stooped Due to: ", cause);
        }
        /* Handle All Current Service Threads */
        /* Service Threads may continue running until their invocations of the service method return. */
        synchronized (service_thread_list){
            /* synchronized methods enable a simple strategy for preventing thread interference and memory consistency errors:
               if an object is visible to more than one thread, all reads or writes to that object's variables
               are done through synchronized methods.
             */
            if(service_thread_list != null){
                for(ServiceThread thread : service_thread_list){
                    try{
                        /* Waits for this thread to die. */
                        thread.join();
                    }
                    catch (InterruptedException exception){
                        /*  if any thread has interrupted the current thread.
                            The interrupted status of the current thread is cleared when this exception is thrown.
                         */
                        exception.printStackTrace();
                    }
                }
            }
        }
        System.out.println("For Debug: Leave stopped()");
    }

    /** Called when an exception occurs at the top level in the listening
        thread.

        <p>
        The intent of this method is to allow the user to report exceptions in
        the listening thread to another thread, by a mechanism of the user's
        choosing. The user may also ignore the exceptions. The default
        implementation simply stops the server. The user should not use this
        method to stop the skeleton. The exception will again be provided as the
        argument to <code>stopped</code>, which will be called later.

        @param exception The exception that occurred.
        @return <code>true</code> if the server is to resume accepting
                connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

        <p>
        The default implementation does nothing.

        @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
    }

    /** Starts the skeleton server.

        <p>
        A thread is created to listen for connection requests, and the method
        returns immediately. Additional threads are created when connections are
        accepted. The network address used for the server is determined by which
        constructor was used to create the <code>Skeleton</code> object.

        @throws RMIException When the listening socket cannot be created or
                             bound, when the listening thread cannot be created,
                             or when the server has already been started and has
                             not since stopped.
     */
    public synchronized void start() throws RMIException
    {
        System.out.println("For Debug: Start Called!" );
        /* Throws Exception When the server has already been started and has not since stopped */
        if(listener != null && listener.isAlive()){ //Has been Started and Hasn't Died Yet
            throw new RMIException("The Skeleton Server has Already been Started and has not since Stopped!");
        }

        /* If the constructor doesn't specify The address at which the skeleton is to run.
           Then Choose the address Here
         */

        /* 'InetSocketAddress' class implements an IP Socket Address (IP address + port number)
            It can also be a pair (hostname + port number)
         */
        try{
            if(skeleton_address == null){
                /* Pick a socket address where the IP address is the wildcard address
                   and the port number is a randome value between 1 & 65535.
                 */
                int max = 65535;
                Random rand = new Random();
                int port_number = rand.nextInt(max) + 1;
                skeleton_address = new InetSocketAddress(port_number);
//                String localIp = InetAddress.getLocalHost().getHostAddress();
                // Get some free port and assign: moving with this
//                serverSocket = new ServerSocket(0);
//                skeleton_address = new InetSocketAddress(localIp, port_number);
                System.out.println("Skeleton Server Address(port): "+skeleton_address.getPort());
            }
//            else{
//                System.out.println("Skeleton Server Address(Passed in)(port): "+skeleton_address.getPort());
//            }
            if((skeleton_server_socket == null) || skeleton_server_socket.isClosed()) {
                /* Create new Server Socket Based on Skeleton_Address's Port Number (Bound to it)*/
                skeleton_server_socket = new ServerSocket(skeleton_address.getPort());
            }

            listener = new ListenerThread<T>(skeleton_server_socket, this);
            /* Start Listening Thread */
            listener.start();

        }
        catch (IOException exception){
            exception.printStackTrace();
        }

//        throw new UnsupportedOperationException("not implemented");
    }

    /** Stops the skeleton server, if it is already running.

        <p>
        The listening thread terminates. Threads created to service connections
        may continue running until their invocations of the <code>service</code>
        method return. The server stops at some later time; the method
        <code>stopped</code> is called at that point. The server may then be
        restarted.
     */
    public synchronized void stop()
    {
        System.out.println("For Debug: Stop Called");
        /* Check if the Listening Thread(Skeleton Server) is still running */
        if(listener != null && listener.isAlive()){
            /* Terminate The Listener */
            listener.stop_status = true;
            try {
                listener.terminate();
                listener.join();
                /* Stop the Skeleton Server */
//                    if(!skeleton_server_socket.isClosed()){
//                        skeleton_server_socket.close();
//                    }
                /* Wait for Service Threads to Finish
                 * stopped is called Here.
                 * Pass "null" cause Here for skeleton stopped normally.
                 * */
                stopped(null);
            }
           catch (InterruptedException exception){//| IOException exception){
                exception.printStackTrace();
           }
        }
        System.out.println("For Debug: Stop Ended");

//        throw new UnsupportedOperationException("not implemented");
    }

}
