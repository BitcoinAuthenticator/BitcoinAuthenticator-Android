package org.bitcoin.authenticator.Events;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Event
{
    public Event()
    { methods = new ArrayList<MethodClassPairing>(); }

    class MethodClassPairing
    {
        public MethodClassPairing(Object passedContainingClass, Method passedMethod)
        {
            if(passedContainingClass == null || passedMethod == null)
                throw new RuntimeException("passedContainingClass or passedMethod were null.");
            else
            {
            	containingClassWeak = new WeakReference<Object>(passedContainingClass);
                method = passedMethod;
            }
        }

        public WeakReference<Object> containingClassWeak;
        public Method method;

        @Override
        public boolean equals(Object PassedObject)
        {
            if(PassedObject.getClass() != this.getClass() || PassedObject == null)
                return false;
            else
            {
                MethodClassPairing comparedPairing = (MethodClassPairing)PassedObject;

                return (containingClassWeak.get() == comparedPairing.containingClassWeak.get()) && (method.equals(comparedPairing.method));
            }
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 37 * hash + (this.containingClassWeak.get() != null ? this.containingClassWeak.get().hashCode() : 0);
            hash = 37 * hash + (this.method != null ? this.method.hashCode() : 0);
            return hash;
        }
    }

    List<MethodClassPairing> methods;

    public boolean AddListener(Object ListeningClass, String MethodName)
    {
        try
        {
            if(ListeningClass.getClass().getMethod(MethodName, Object.class, Object.class) == null)
                throw new RuntimeException(MethodName + "(Object, " + "); is not accessible.");
            else
            {
                MethodClassPairing method = new MethodClassPairing(ListeningClass, ListeningClass.getClass().getMethod(MethodName, Object.class, Object.class));

                if(methods.contains(method))
                    return false;
                else
                {
                    methods.add(method);
                    return true;
                }
            }
        }
        catch (NoSuchMethodException error) { throw new RuntimeException("No such method, or correct overloads of method, exists."); }
    }

    public void RemoveListener(Object ListeningClass, String MethodName)
    {
        try { 
        	methods.remove(new MethodClassPairing(ListeningClass, ListeningClass.getClass().getMethod(MethodName, Object.class, Object.class))); 
        }
        catch (NoSuchMethodException error) 
        { 
        	error.printStackTrace();
        	//throw new RuntimeException("No such method, or correct overloads of method, exists.");  // commented so unregistered events could be removed as a verification
        }
    }

    public void ClearListeners()
    { methods.clear(); }

    public boolean isListenerExists(Object ListeningClass)
    {
    	for(int i = 0; i < methods.size(); i++)
    		if (methods.get(i) == ListeningClass)
    			return true;
    	return false;
    }
    
    public void Raise(Object Sender, Object Arguments)
    {
        for(int i = 0; i < methods.size(); i++)
        {
            try { 
            	Object containingClass = methods.get(i).containingClassWeak.get();
            	if(containingClass != null)
            		methods.get(i).method.invoke(containingClass, Sender, Arguments); 
            }
            catch (IllegalAccessException error)
            { throw new RuntimeException("This shouldn't have happened ???_???"); }
            catch (InvocationTargetException error)
            { 
            	throw new RuntimeException("Error in event handler"); 
            }
        }
    }
}