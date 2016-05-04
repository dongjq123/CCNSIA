package edu.bupt.service.io;

import edu.bupt.service.io.impl.CCNIOImpl;
import org.ccnx.ccn.CCNHandle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Created by fish on 16-4-22.
 */
public class Activator implements BundleActivator {
    CCNHandle ccnHandle;
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        ccnHandle = CCNHandle.open();
        if (ccnHandle != null){
            bundleContext.registerService(CCNIOManage.class.getName(), new CCNIOImpl(ccnHandle), null);
            bundleContext.registerService(CCNHandle.class.getName(), ccnHandle, null);
            System.out.println("CCN IO Service registered.");
        }else {
            System.err.println("CCNHandle open error");
        }

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        ccnHandle.close();
        System.out.println("CCN IO Service stopped.");
    }
}
