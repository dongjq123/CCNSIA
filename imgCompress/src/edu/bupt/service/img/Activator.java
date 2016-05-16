package edu.bupt.service.img;

import edu.bupt.service.io.CCNIOManage;
import edu.bupt.service.io.ICCNService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Created by fish on 16-5-13.
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        ServiceReference sr = bundleContext.getServiceReference(CCNIOManage.class.getName());
        CCNIOManage manage = (CCNIOManage) bundleContext.getService(sr);
        if(manage != null){
            ICCNService img = new ImgCompress(manage);
            //wpa.execute(new String[]{"ccnx:/contents/test.html","ccnx:/contents/out/test.html"});
            bundleContext.registerService(ImgCompress.class.getName(), img, null);
        }
        System.out.println("htmlParseService started..");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
