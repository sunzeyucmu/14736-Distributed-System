package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Additional Service threads are created when connections are accepted.
 * These Threads will read and parse method calls(name & arguments) forwarded by Stub Object
 * (Stub will open a single connection per method call
 * Then call the correct methods on the server Object implementing Remote Interface
 * When a method returns, the return value (or exception) is sent over the network to the client
 */
public class ServiceThread<T> extends Thread{
    private Socket connection;
    private Skeleton<T> skeleton; //Related Skeleton (Connect to this Skeleton Server)
//    private T remoteObject; // Object Implementing RemoteInterface

    /* Register this Service Thread in 'service_thread_list' */
    public ServiceThread(Socket connection_socket, Skeleton<T> skeleton){
        this.connection = connection_socket;
        this.skeleton = skeleton;
//        this.remoteObject = remoteObject;
        this.skeleton.service_thread_list.add(this);
    }
    /* override the run method of class Thread */
    @Override
    public void run(){
            /* Arguments & Results Transmition
             * Serialize the Communication
             * */
        ObjectOutputStream out = null;
        ObjectInputStream in = null;


        try{
                /* Ensure the out stream is created first, and be flushed before creating input stream
                   To Avoid Deadlock
                 */
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
                /* Parse Information Regard Method Call */
            String method_name = (String)in.readObject(); // Name of the Method
            Class<?>[] args_type = (Class<?>[]) in.readObject(); //Type for each Argument
            Object[] args = (Object[]) in.readObject(); //Arguments

                /* Retrieve the required method on the server */
            Method method = skeleton.remote_interface_c.getMethod(method_name, args_type);

            Class return_type = method.getReturnType();

                /* Invoke the Method
                 * Invokes the underlying method represented by this Method object(method),
                 * on the specified object (object implementing remote Interface) with the specified parameters
                 * */
            try{
                Object return_value = method.invoke(skeleton.remoteObject, args);

                    /* Return The Method Call Result */
                    /* ### Just For Now */
                out.writeObject("Remote Method Call Succeeded!");
                out.writeObject(return_value);
            }
            catch (InvocationTargetException e){//(IllegalAccessException | InvocationTargetException e){
                    /* Send Back the Exception to Client
                     * If the remote method raises an exception,
                     * the Stub must raise the same exception,
                     * */
                out.writeObject("Remote Method Call Failed!");
                out.writeObject(e.getTargetException());
            }

        }
        catch(Exception exception){
                /* an exception occurs at the top level in a service thread */
            skeleton.service_error(new RMIException((exception)));
        }
        finally {
            // executed when the try block is exited
                /* service method's result has been returned
                   Close the Serializable Streams
                   Close the Connection
                 */
            skeleton.service_thread_list.remove(this);
            try{
                if(out != null){
                    out.flush();
                    out.close();
                }
            }
            catch (IOException e){
                System.out.println("Out Stream");
                e.printStackTrace();
            }
            try{
                if(in != null){
                    in.close();
                }
            }
            catch (IOException e){
                System.out.println("In Stream");
                e.printStackTrace();
            }
            try{
                this.connection.close();
            }
            catch (IOException e){
                System.out.println("Connection Close");
                e.printStackTrace();
            }
        }


    }

}
