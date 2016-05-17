package edu.bupt.sia.ccn.services;

import edu.bupt.service.io.CCNIOManage;
import edu.bupt.service.io.ICCNService;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.protocol.Interest;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by fish on 16-4-11.
 */
public class OSGIController {
    private BundleContext bundleContext;
    final static protected String CCNServiceTag = "CCNService";
    final static protected String servicePrefix = "ccnx:/";

    public OSGIController(BundleContext context){
        this.bundleContext = context;
    }

    public Bundle installBundle(String path){
        Bundle bundle = null;
        try {
            bundle = bundleContext.installBundle(path);
            bundle.start();
        } catch (BundleException e) {
            System.out.println("installBundle exception:"+path);
            e.printStackTrace();
        }
        return bundle;
    }

    public Bundle installBundleByCCNIOStream(String bundleName){
        Bundle bundle = null;
        try {
            ServiceReference sr = bundleContext.getServiceReference(CCNIOManage.class.getName());
            CCNIOManage manage = (CCNIOManage)bundleContext.getService(sr);

            if(manage != null) {
                CCNFileInputStream cfi = manage.getCCNFile(servicePrefix+bundleName+".jar");
                bundle = bundleContext.installBundle(bundleName, cfi);
                System.out.println("CCNServiceInstall: "+bundle.getSymbolicName()+" service install success!!");
                bundle.start();
            }else {
                System.err.println("get CCNServiceInstall error!");
            }
        } catch (BundleException e) {
            System.out.println("installBundle by InputStream exception:"+bundleName);
            e.printStackTrace();
        }
        return bundle;
    }

    public void executeServiceByID(long bundleID, String[] args){
        Bundle b = bundleContext.getBundle(bundleID);
        String ServiceEntryName = b.getHeaders().get(CCNServiceTag);
//        String ServiceEntryName = "";
        if(ServiceEntryName != null && ServiceEntryName.length() > 0) {
            ServiceReference sr = bundleContext.getServiceReference(ServiceEntryName);
            Object o = bundleContext.getService(sr);
            try {
                Method m = o.getClass().getMethod("execute", String[].class);
                m.invoke(o, new Object[]{args});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                System.out.println("Can't find execute method!!");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }else{
            System.err.println("executeServiceByID : Can't get Service entry class");
        }

    }

    public void executeServiceBySymbolicName(String bundleSymbolicName, String[] args, Interest interest){
        Bundle b = bundleContext.getBundle(bundleSymbolicName);
        String ServiceEntryName = b.getHeaders().get(CCNServiceTag);
//        String ServiceEntryName = "";
        if(ServiceEntryName != null && ServiceEntryName.length() > 0) {
            ServiceReference sr = bundleContext.getServiceReference(ServiceEntryName);
            Object o = bundleContext.getService(sr);
            if(o instanceof ICCNService) {
                ICCNService service = (ICCNService)o;
                new Thread(new ServiceExecutor(service,args,interest)).start();
            }else{
                System.out.println("not a CCNService error!!");
            }

        }else{
            System.err.println("executeServiceBySymbolicName : Can't get Service entry class");
        }
    }

    public void updateServiceByID(long bundleID){
    }

    public void removeServiceByID(long bundleID){
        Bundle b = bundleContext.getBundle(bundleID);
        try {
            b.stop();//
            b.uninstall();
        } catch (BundleException e) {
            e.printStackTrace();
        }
    }

    public void removeServiceBySymbolicName(String bundleSymbolicName){
        Bundle b = bundleContext.getBundle(bundleSymbolicName);
        try {
            b.stop();
            b.uninstall();
        } catch (BundleException e) {
            e.printStackTrace();
        }
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    class ServiceExecutor implements Runnable {
        ICCNService service;
        String[] args;
        Interest interest;
        public ServiceExecutor(ICCNService service, String[] args, Interest interest){
            this.service = service;
            this.args = args;
            this.interest = interest;
        }
        @Override
        public void run() {
            try {
//                Method[] ms = service.getClass().getMethods();
//                for(Method m : ms){
//                    System.out.println(m.getName());
//                }
                service.execute(args, interest);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}