package edu.bupt.sia.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Created by fish on 16-4-11.
 */
public class OSGILauncher {
    private static Framework framework;

    private OSGILauncher(){
    }

    private static Framework frameworkInit() throws BundleException {
        // Obtain a framework factory.
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        FrameworkFactory factory = loader.iterator().next();
        Map<String, String> configProps = new HashMap<String, String>();
//        configProps.put("org.osgi.framework.bundle.parent", "app");
//        configProps.put("org.osgi.framework.bootdelegation", "javax.crypto.*,javax.crypto.interfaces.*");
//        configProps.put("org.osgi.framework.executionenvironment", "JavaSE-1.8,JavaSE-1.7");
        configProps.put("org.osgi.framework.storage.clean", "onFirstInit");
        // And get a framework.
        framework = factory.newFramework(configProps);
        //初始化framework
        framework.init();
        framework.start();
        return framework;
    }

    private static void startOSGIFramework(){
        BundleContext bundleContext = framework.getBundleContext();
        //add initial bundles
        try {
//            Bundle ccnbundle01 = bundleContext.installBundle("file:bundles/org.apache.felix.gogo.runtime_0.10.0.v201209301036.jar");
//            ccnbundle01.start();
//            Bundle ccnbundle02 = bundleContext.installBundle("file:bundles/org.apache.felix.gogo.shell_0.10.0.v201212101605.jar");
//            ccnbundle02.start();
//            Bundle ccnbundle03 = bundleContext.installBundle("file:bundles/org.apache.felix.gogo.command_0.10.0.v201209301215.jar");
//            ccnbundle03.start();

            Bundle ccnbundle = bundleContext.installBundle("file:bundles/ccn-bundle_1.0.0.jar");
            Bundle ccniobundle = bundleContext.installBundle("file:bundles/CCNIOService.jar");
            Bundle ccnhandlerbundle = bundleContext.installBundle("file:bundles/CCNServiceHandler.jar");
            ccniobundle.start();
            ccnbundle.start();
            ccnhandlerbundle.start();
        } catch (BundleException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        try {
            frameworkInit();
            startOSGIFramework();
        } catch (BundleException e) {
            e.printStackTrace();
        }
    }
}
