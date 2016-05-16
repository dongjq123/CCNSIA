package edu.bupt.sia.ccn;

import edu.bupt.sia.ccn.services.CCNServiceManager;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.CCNFileOutputStream;
import org.ccnx.ccn.profiles.CommandMarker;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.profiles.metadata.MetadataProfile;
import org.ccnx.ccn.protocol.CCNTime;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;

/**
 * Created by fish on 16-4-21.
 */
public class CCNServiceHandler implements CCNInterestHandler {
    static String DEFAULT_URI = "ccnx:/";

    protected CCNHandle ccnHandle;
    protected ContentName _prefix;
    protected CCNServiceManager manager;
    protected BundleContext bundleContext;

    public CCNServiceHandler(String ccnxURI, BundleContext bundleContext) throws MalformedContentNameStringException, IOException, ConfigurationException {
        _prefix = ContentName.fromURI(ccnxURI);

        manager = new CCNServiceManager(bundleContext);
        this.bundleContext = bundleContext;

        ServiceReference sr = bundleContext.getServiceReference(CCNHandle.class.getName());
        CCNHandle h = (CCNHandle)bundleContext.getService(sr);
        if(h != null){
            ccnHandle = h;
        }else {
            throw new NullPointerException("get CCNHandle Object error!!");
        }
        ccnHandle.registerFilter(_prefix, this);
    }

    @Override
    public boolean handleInterest(Interest interest) {
        System.out.println(interest.name().count()+","+interest.name().toURIString());
        if (!_prefix.isPrefixOf(interest.name())) {
            Log.info("Unexpected interest, {0}", _prefix);
            return false;
        }
        if (SegmentationProfile.isSegment(interest.name())
                && !SegmentationProfile.isFirstSegment(interest.name())) {
            Log.info(
                    "Got an interest for something other than a first segment, ignoring {0}.",
                    interest.name());
            return false;
        } else if (interest.name().contains(
                CommandMarker.COMMAND_MARKER_BASIC_ENUMERATION.getBytes())) {
            Log.info("Got a name enumeration request: {0}", interest);
            return false;
        } else if (MetadataProfile.isHeader(interest.name())) {
            Log.info(
                    "Got an interest for the first segment of the header, ignoring {0}.",
                    interest.name());
            return false;
        }
        if (interest.name().contains(
                "_meta_".getBytes()) || interest.name().contains(
                ".header".getBytes())) {
            Log.info(
                    "Got an interest for the first segment of the header, ignoring {0}.",
                    interest.name());
            return false;
        }

        ServiceNameObject serviceNameObject = ServiceNameParser.parseServiceName(interest.name());
        if(serviceNameObject != null) {
            if (serviceNameObject.getServiceName() != null
                    && serviceNameObject.getServiceName().length() > 0) {
                manager.startLocalService(serviceNameObject, interest);
                return true;
            } else {
                Log.warning("Service Name parse error!", serviceNameObject);
            }
        }else{
            Log.warning("ServiceNameObject is null!");
            return false;
        }
        return false;
    }

    protected boolean serviceInit(ServiceNameObject serviceNameObject){
        String serviceName = serviceNameObject.getServiceName();
        if(!manager.service_existed(serviceName)){
//            manager.fetchService();
        }
        return true;
    }
//
//    public static void main(String[] args){
//        if (args.length < 1) {
//            System.err.println("usage: CCNServiceHandler [<ccn prefix URI> default: ccn:/]");
//            return;
//        }
//        String ccnURI = (args.length > 0) ? args[0] : DEFAULT_URI;
//        try {
//            CCNServiceHandler ccnServiceHandler = new CCNServiceHandler(ccnURI);
//
//        } catch (MalformedContentNameStringException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ConfigurationException e) {
//            e.printStackTrace();
//        }
//    }

    public CCNHandle getCCNHandle() {
        return ccnHandle;
    }
}
