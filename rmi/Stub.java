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
    }


}
