package edu.bupt.sia.ccn;

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.IOException;

/**
 * Created by fish on 16-4-28.
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("CCNServiceHandler started..");
        String DEFAULT_URI = "ccnx:/";
        String ccnPrefix = bundleContext.getProperty("ccnPrefix");
//        if (args.length < 1) {
//            System.err.println("usage: CCNServiceHandler [<ccn prefix URI> default: ccn:/]");
//            return;
//        }
//        String ccnURI = (args.length > 0) ? args[0] : DEFAULT_URI;
        String ccnURI = ccnPrefix != null ? ccnPrefix : DEFAULT_URI;
        try {
            CCNServiceHandler ccnServiceHandler = new CCNServiceHandler(ccnURI, bundleContext);
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("CCNServiceHandler stopped..");
    }
}
