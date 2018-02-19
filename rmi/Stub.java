package rmi;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.*;

/** RMI stub factory.

    <p>
    RMI stubs hide network communication with the remote server and provide a
    simple object-like interface to their users. This class provides methods for
    creating stub objects dynamically, when given pre-defined interfaces.

    <p>
    The network address of the remote server is set when a stub is created, and
    may not be modified afterwards. Two stubs are equal if they implement the
    same interface and carry the same remote server address - and would
    therefore connect to the same skeleton. Stubs are serializable.

 */
public abstract class Stub
{
    /**
     *  Notice:
     *  Stub is a class factory which generates stub objects for remote interfaces.
     *  The class Stub itself cannot be instantiated.
     *  To repeat, it is important to note that stub objects are not instances of the Stub class
     */

     /* -------- Helper Functions -------- */
    /**
     *
     *  All versions of 'Stub.create' must reject interfaces which are not remote interfaces.
     *
     * Check whether interface'smethods are all marked as throwing RMIException.
     * @return true or false
     */
    private static <T>boolean isRemoteInterface(Class<T> c){
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
//     * The generation of stubs that implement arbitrary remote interfaces at run-time is done by
//     * creating proxy objects (class Proxy). Every proxy object has a reference to
//     * an invocation handler(class InvocationHandler), whose 'invoke' method is called
//     * whenever a method is called on the proxy object.
//     * The RMI library’s stub objects will therefore be $$$proxy objects$$$,
//     * and the $$$marshalling of arguments$$$ will be done in their invocation handlers.
//     */
//
//    /**
//     * Each proxy instance has an associated invocation handler object,
//     * which implements the interface 'InvocationHandler'.
//     *
//     */
//    private static class StubInvocationHandler<T> implements InvocationHandler, Serializable{
//
//        private Class<T> remote_interface_class;
//
//        private InetSocketAddress skeleton_address;
//
//        public StubInvocationHandler(Class<T> remote_interface_class, InetSocketAddress address){
//            /* Match to Corresponding Skeleton */
//            this.remote_interface_class = remote_interface_class;
//            this.skeleton_address = address;
//        }
//
//        public Class<T> getRemote_interface_class(){
//            return this.remote_interface_class;
//        }
//
//        public InetSocketAddress get_Address(){
//            return this.skeleton_address;
//        }
//
//        /**
//         *
//         * The 'toString' method should report:
//         *  the name of the remote interface implemented by the stub + the remote address (hostname + port)
//         *  of the skeleton to which the stub connects
//
//         * @param proxy
//         * @return a string representing the stub proxy
//         * @throws IllegalArgumentException  if the argument is not a proxy instance
//         */
//        private String toStringHelper(Object proxy) throws IllegalArgumentException{
//            @SuppressWarnings("unchecked")
//            StubInvocationHandler<T> stubProxy = (StubInvocationHandler<T>) Proxy.getInvocationHandler(proxy);
//            return "Remote Interface: "+stubProxy.getRemote_interface_class().getName()+" Host: "
//                    +stubProxy.get_Address().getHostName()+":"+stubProxy.get_Address().getPort();
//        }
//
//        /**
//         *    $$$ 'equals' Method : implement the same interface & carry the same remote server address $$$
//         *
//         * @param proxy
//         * @param method
//         * @param args
//         * @return true if Equal, false otherwise
//         */
//
//        private boolean equalHelper(Object proxy, Method method, Object[] args){
//            /* Two stubs (Proxy object) are considered equal if
//               they implement the same remote interface and connect to the same skeleton
//             */
//            Object other_proxy = args[0];
//            if(other_proxy instanceof Proxy){
//                /* We Only Need to Compare two Proxy objects' String Representation */
//                return toStringHelper(proxy).equals(toStringHelper(other_proxy));
//            }
//            return false;
//        }
//
//        /**
//         * compute stub(Proxy object)'s hashcode. Based on its String representation
//         * @param proxy
//         * @return the hashCode() of the concatenated classname, address and port.
//         */
//        private Integer hashCodeHelper(Object proxy) throws IllegalArgumentException{
//            return toStringHelper(proxy).hashCode();
//        }
//
//        /*
//         *  A method invocation on a proxy instance through one its proxy interfaces will be dispatched to
//         *  the invoke method of the instance's invocation handler, passing
//         *          1. the proxy instance,
//         *          2. a java.lang.reflect.Method object identifying the method that was invoked
//         *          3. and an array of type Object containing the arguments.
//         *  The invocation handler processes the encoded method invocation as appropriate
//         *  and the result that it returns will be returned as the result of the method invocation on the proxy instance.
//         */
//
//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
//            if(proxy == null || method == null){
//                throw new Error("Empty invoke method!");
//            }
//
////            String method_name = method.getName();
////            String return_type = method.getReturnType().getName();
//
//            /* Deal with 'toString', 'hashCode', 'equal' Method First */
//            try{
//                if (method.equals(Object.class.getMethod("toString"))) {
//                    return toStringHelper(proxy);
//                }
//                else if (method.equals(Object.class.getMethod("hashCode"))) {
//                    return hashCodeHelper(proxy);
//                }
//                else if (method.equals(Object.class.getMethod("equals", Object.class))) {
//                    return equalHelper(proxy, method, args);
//                }
//
//            }
//            catch(NoSuchMethodException e){
//                e.printStackTrace();
//            }
//
//            /*
//                > Build the Connection Between Stub & Skeleton
//                > Open Stream with skeleton for method call invocation and response
//                > Send method and parameters to skeleton, receive return-value or exception from skeleton.
//             */
//            Socket connection = null  ;//= new Socket(skeleton_address.getHostName(), skeleton_address.getPort());
//            ObjectOutputStream outStream = null;
//            ObjectInputStream inStream = null;
//            Object return_value = null;
//            Object error_info = null;
//
//            try{
//                connection = new Socket(skeleton_address.getHostName(), skeleton_address.getPort());
//                outStream = new ObjectOutputStream(connection.getOutputStream());
//                outStream.flush();
//            }
//            catch (IOException e){
//                System.out.println("Failed to Connect to Skeleton Server!");
//                throw new RMIException("Failed to Connect to Skeleton Server!");
//            }
//
//            /* Send Method Call to remote Interface */
//            try {
//                outStream.writeObject(method.getName());
//                outStream.writeObject(method.getParameterTypes());
//                outStream.writeObject(args);
//            }
//            catch (IOException e){
//                System.out.println("Failed to Write Objects to Skeleton Server!");
//                throw new RMIException("Failed to Write Objects to Skeleton Server!");
//            }
//
//            /* Receive Results From Skeleton */
//            try{
//                inStream = new ObjectInputStream(connection.getInputStream());
//                /* Fail Or Success ? */
//                Object result_status = inStream.readObject();
//
//                /* Success */
//                if(result_status.equals("Remote Method Call Succeeded!")){
//                    return_value = inStream.readObject();
//                }
//                /* Failure */
//                else if(result_status.equals("Remote Method Call Failed!")){
//                    System.out.println("Remote Method Call Failed...!");
//                    /* Read Error Information */
//                    error_info = inStream.readObject();
////                    System.out.println("In Stub: "+(Exception)error_info);
////                    throw (Exception) error_info;
//                }
//            }
//            catch (IOException e){
//                System.out.println("Failed when Retrieving Results From Skeleton Server!");
//                throw new RMIException("Failed when Retrieving Results From Skeleton Server!");
//            }
//
//            if(error_info != null){
//                throw (Exception) error_info;
//            }
//
//            /* Close Connection for This Method Call */
//            try{
//                inStream.close();
//                outStream.close();
//                connection.close();
//            }
//            catch(Exception e){
//                throw new RMIException("Can't close network I/O stream or client socket.");
//            }
//
//            return return_value;
//        }
//    }

    /** Creates a stub, given a skeleton with an assigned adress.

        <p>
        The stub is assigned the address of the skeleton. The skeleton must
        either have been created with a fixed address, or else it must have
        already been started.

        <p>
        This method should be used when the stub is created together with the
        skeleton. The stub may then be transmitted over the network to enable
        communication with the skeleton.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose network address is to be used.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned an
                                      address by the user and has not yet been
                                      started.
        @throws UnknownHostException When the skeleton address is a wildcard and
                                     a port is assigned, but no address can be
                                     found for the local host.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton) throws UnknownHostException
    {
        if(c == null || skeleton == null){
            throw new NullPointerException("Null Arguments!");
        }

        if(skeleton.getSkeleton_address() == null){
            throw new IllegalStateException("The Skeleton has not been assigned an Address");
        }
        /* Skeleton Has not yet been started ? */
        // TOBEDONE

        /* Check if c is remote interface */
        if(!isRemoteInterface(c)) throw new  Error("Interface: "+c.getSimpleName()+" does not represent a remote interface");

        /* Unkonwn Host Exception */

        /* Get Proxy for Remote interface T */
        try{
            /* A proxy instance is an instance of a proxy class.
               Each proxy instance has an associated invocation handler objec
             */
            InvocationHandler handler = new StubInvocationHandler<T>(c, skeleton.getSkeleton_address());
            T stub = (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, handler);
            return stub;
        }
        catch(Exception e){
            throw new Error("Object(Stub) implementing interface: "+c.getCanonicalName()+" Cannot be Dynamically Created!");
        }

    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
        which overrides the skeleton's hostname.

        <p>
        The stub is assigned the port of the skeleton and the given hostname.
        The skeleton must either have been started with a fixed port, or else
        it must have been started to receive a system-assigned port, for this
        method to succeed.

        <p>
        This method should be used when the stub is created together with the
        skeleton, but firewalls or private networks prevent the system from
        automatically assigning a valid externally-routable address to the
        skeleton. In this case, the creator of the stub has the option of
        obtaining an externally-routable address by other means, and specifying
        this hostname to this method.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose port is to be used.
        @param hostname The hostname with which the stub will be created.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned a
                                      port.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname)
    {
        if (c == null || skeleton == null || hostname == null || hostname.equals("")) {
            throw new NullPointerException("Null Arguments!");
        }
        if(skeleton.getSkeleton_address() == null){
            throw new IllegalStateException("The Skeleton has not been assigned an Address");
        }
        /* Skeleton Has not yet been started ? */
        // TOBEDONE

        /* Check if c is remote interface */
        if(!isRemoteInterface(c)) throw new  Error("Interface: "+c.getSimpleName()+" does not represent a remote interface");

        /* Generating the new Address(HostName Overrided) For Stub */
        InetSocketAddress new_address = new InetSocketAddress(hostname, skeleton.getSkeleton_address().getPort());
        try{
            InvocationHandler handler = new StubInvocationHandler<T>(c, new_address);
            T stub = (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, handler);
            return stub;
        }
        catch(Exception e){
            throw new Error("Object(Stub) implementing interface: "+c.getCanonicalName()+" Cannot be Dynamically Created!");
        }
//        throw new UnsupportedOperationException("not implemented");
    }

    /** Creates a stub, given the address of a remote server.

        <p>
        This method should be used primarily when bootstrapping RMI. In this
        case, the server is already running on a remote host but there is
        not necessarily a direct way to obtain an associated stub.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param address The network address of the remote skeleton.
        @return The stub created.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
        if(c == null || address == null){
            throw new NullPointerException("Null Arguments!");
        }

        if(!isRemoteInterface(c)) throw new  Error("Interface: "+c.getSimpleName()+" does not represent a remote interface");

        /* Get Proxy for Remote interface T */
        try{
            InvocationHandler handler = new StubInvocationHandler<T>(c, address);
            T stub = (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, handler);
            return stub;
        }
        catch(Exception e){
            throw new Error("Object(Stub) implementing interface: "+c.getCanonicalName()+" Cannot be Dynamically Created!");
        }
//        throw new UnsupportedOperationException("not implemented");
    }


}
