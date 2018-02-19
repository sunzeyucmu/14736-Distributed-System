package rmi;

/**
 * The generation of stubs that implement arbitrary remote interfaces at run-time is done by
 * creating proxy objects (class Proxy). Every proxy object has a reference to
 * an invocation handler(class InvocationHandler), whose 'invoke' method is called
 * whenever a method is called on the proxy object.
 * The RMI library’s stub objects will therefore be $$$proxy objects$$$,
 * and the $$$marshalling of arguments$$$ will be done in their invocation handlers.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Each proxy instance has an associated invocation handler object,
 * which implements the interface 'InvocationHandler'.
 *
 */
public class StubInvocationHandler<T> implements InvocationHandler, Serializable {

    private Class<T> remote_interface_class;

    private InetSocketAddress skeleton_address;

    public StubInvocationHandler(Class<T> remote_interface_class, InetSocketAddress address){
            /* Match to Corresponding Skeleton */
        this.remote_interface_class = remote_interface_class;
        this.skeleton_address = address;
    }

    public Class<T> getRemote_interface_class(){
        return this.remote_interface_class;
    }

    public InetSocketAddress get_Address(){
        return this.skeleton_address;
    }

    /**
     *
     * The 'toString' method should report:
     *  the name of the remote interface implemented by the stub + the remote address (hostname + port)
     *  of the skeleton to which the stub connects

     * @param proxy
     * @return a string representing the stub proxy
     * @throws IllegalArgumentException  if the argument is not a proxy instance
     */
    private String toStringHelper(Object proxy) throws IllegalArgumentException{
        @SuppressWarnings("unchecked")
        StubInvocationHandler<T> stubProxy = (StubInvocationHandler<T>) Proxy.getInvocationHandler(proxy);
        return "Remote Interface: "+stubProxy.getRemote_interface_class().getName()+" Host: "
                +stubProxy.get_Address().getHostName()+":"+stubProxy.get_Address().getPort();
    }

    /**
     *    $$$ 'equals' Method : implement the same interface & carry the same remote server address $$$
     *
     * @param proxy
     * @param method
     * @param args
     * @return true if Equal, false otherwise
     */

    private boolean equalHelper(Object proxy, Method method, Object[] args){
            /* Two stubs (Proxy object) are considered equal if
               they implement the same remote interface and connect to the same skeleton
             */
        Object other_proxy = args[0];
        if(other_proxy instanceof Proxy){
                /* We Only Need to Compare two Proxy objects' String Representation */
            return toStringHelper(proxy).equals(toStringHelper(other_proxy));
        }
        return false;
    }

    /**
     * compute stub(Proxy object)'s hashcode. Based on its String representation
     * @param proxy
     * @return the hashCode() of the concatenated classname, address and port.
     */
    private Integer hashCodeHelper(Object proxy) throws IllegalArgumentException{
        return toStringHelper(proxy).hashCode();
    }

        /*
         *  A method invocation on a proxy instance through one its proxy interfaces will be dispatched to
         *  the invoke method of the instance's invocation handler, passing
         *          1. the proxy instance,
         *          2. a java.lang.reflect.Method object identifying the method that was invoked
         *          3. and an array of type Object containing the arguments.
         *  The invocation handler processes the encoded method invocation as appropriate
         *  and the result that it returns will be returned as the result of the method invocation on the proxy instance.
         */

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
        if(proxy == null || method == null){
            throw new Error("Empty invoke method!");
        }

//            String method_name = method.getName();
//            String return_type = method.getReturnType().getName();

            /* Deal with 'toString', 'hashCode', 'equal' Method First */
        try{
            if (method.equals(Object.class.getMethod("toString"))) {
                return toStringHelper(proxy);
            }
            else if (method.equals(Object.class.getMethod("hashCode"))) {
                return hashCodeHelper(proxy);
            }
            else if (method.equals(Object.class.getMethod("equals", Object.class))) {
                return equalHelper(proxy, method, args);
            }

        }
        catch(NoSuchMethodException e){
            e.printStackTrace();
        }

            /*
                > Build the Connection Between Stub & Skeleton
                > Open Stream with skeleton for method call invocation and response
                > Send method and parameters to skeleton, receive return-value or exception from skeleton.
             */
        Socket connection = null  ;//= new Socket(skeleton_address.getHostName(), skeleton_address.getPort());
        ObjectOutputStream outStream = null;
        ObjectInputStream inStream = null;
        Object return_value = null;
        Object error_info = null;

        try{
            connection = new Socket(skeleton_address.getHostName(), skeleton_address.getPort());
            outStream = new ObjectOutputStream(connection.getOutputStream());
            outStream.flush();
        }
        catch (IOException e){
            System.out.println("Failed to Connect to Skeleton Server!");
            throw new RMIException("Failed to Connect to Skeleton Server!");
        }

            /* Send Method Call to remote Interface */
        try {
            outStream.writeObject(method.getName());
            outStream.writeObject(method.getParameterTypes());
            outStream.writeObject(args);
        }
        catch (IOException e){
            System.out.println("Failed to Write Objects to Skeleton Server!");
            throw new RMIException("Failed to Write Objects to Skeleton Server!");
        }

            /* Receive Results From Skeleton */
        try{
            inStream = new ObjectInputStream(connection.getInputStream());
                /* Fail Or Success ? */
            Object result_status = inStream.readObject();

                /* Success */
            if(result_status.equals("Remote Method Call Succeeded!")){
                return_value = inStream.readObject();
            }
                /* Failure */
            else if(result_status.equals("Remote Method Call Failed!")){
                System.out.println("Remote Method Call Failed...!");
                    /* Read Error Information */
                error_info = inStream.readObject();
//                    System.out.println("In Stub: "+(Exception)error_info);
//                    throw (Exception) error_info;
            }
        }
        catch (IOException e){
            System.out.println("Failed when Retrieving Results From Skeleton Server!");
            throw new RMIException("Failed when Retrieving Results From Skeleton Server!");
        }

        if(error_info != null){
            throw (Exception) error_info;
        }

            /* Close Connection for This Method Call */
        try{
            inStream.close();
            outStream.close();
            connection.close();
        }
        catch(Exception e){
            throw new RMIException("Can't close network I/O stream or client socket.");
        }

        return return_value;
    }
}