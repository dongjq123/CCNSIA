package edu.bupt.service.html;

import edu.bupt.service.io.CCNIOManage;
import edu.bupt.service.io.ICCNService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Created by fish on 16-4-27.
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        ServiceReference sr = bundleContext.getServiceReference(CCNIOManage.class.getName());
        CCNIOManage manage = (CCNIOManage) bundleContext.getService(sr);
        if(manage != null){
            ICCNService wpa = new WebpageAdaptation(manage);
            //wpa.execute(new String[]{"ccnx:/contents/test.html","ccnx:/contents/out/test.html"});
            bundleContext.registerService(WebpageAdaptation.class.getName(), wpa, null);
        }
        System.out.println("htmlParseService started..");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
