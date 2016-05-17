package edu.bupt.sia.ccn.services;

import edu.bupt.sia.ccn.ServiceNameObject;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.*;
import java.util.*;

/**
 * Created by yangkuang on 16-4-14.
 */

public class CCNServiceManager{
    CCNServiceTable<String, CCNServiceObject> _serviceTable = new CCNServiceTable<>(5);
    OSGIController _serviceController;
    CCNServicePopularity _servicePopularity = new CCNServicePopularity();
    Hashtable<String, Integer> _servicePriorityTable = new Hashtable<>();

    public CCNServiceManager(BundleContext bundleContext)
            throws MalformedContentNameStringException, ConfigurationException,
            IOException {
        _serviceController = new OSGIController(bundleContext);
        System.out.println("CCNServiceManager Start!");
    }

    public int getMinIndex(int[] arr){
        int minIndex = 0;
        for(int i=0; i<arr.length; i++){
            if(arr[i] < arr[minIndex]){
                minIndex = i;
            }
        }
        return minIndex;
    }


    public int getMinNum(int[] arr){
        int minNum = arr[0];
        for(int i=0; i<arr.length; i++){
            if(arr[i] < minNum){
                minNum = arr[i];
            }
        }
        return minNum;
    }

    public boolean service_existed(String serviceName) {//Check whether a specific service already existed in the serviceTable(or already be installed)
        boolean compare_result = false;
        if (_serviceTable.get(serviceName) != null) {
            return true;
        }
        return compare_result;
    }

    public boolean same_version(String serviceVersion, String serviceName) {
        boolean compare_result = false;
        if (serviceVersion == _serviceTable.get(serviceName).serviceVersion()) {
            return true;
        }
        return compare_result;
    }

    public boolean serviceTable_withinSize() {
        boolean default_result = false;
        if (_serviceTable.usedSize() < 5) { //default max table size = 5
            return true;
        }
        return default_result;
    }

    public void removeService(String serviceName) {
        _serviceController.removeServiceBySymbolicName(serviceName);
        _serviceTable.delete(serviceName);
        System.out.println("Service: "+serviceName+" is removed!");
    }

    public String service_tobeRemoved() {//generate the lowest servicePriority service which will be removed
        int base_priority = 1;
        int j = 0;
        String[] serviceName_list = {};
        int[] servicePriority_list = {};

        for (Iterator it = _serviceTable.getMap().keySet().iterator(); it.hasNext();) {
            int servicePopularity = 0;
            int servicePriority = 0;

            String serviceName_key = it.next().toString();
            servicePopularity = _serviceTable.get(serviceName_key).servicePopularity();
            servicePriority = servicePopularity + base_priority;
            _servicePriorityTable.put(serviceName_key, servicePriority);

            serviceName_list[j] = serviceName_key;
            servicePriority_list[j] = servicePriority;

            base_priority++;
            j++;
        }

        int removedService_index = 0;
        removedService_index = getMinIndex(servicePriority_list);

        String removedService = null;
        removedService = serviceName_list[removedService_index];

        return removedService;
    }

    public void installService(String serviceName, String servicePath) {
        Bundle bundle = _serviceController.installBundle(servicePath);
        long serviceID = bundle.getBundleId();
        String serviceVersion = bundle.getVersion().toString();

        int servicePopularity = 0;
        servicePopularity = _servicePopularity.get_CCNServicePopularity().get("ccnx:/" + serviceName + ".jar");

        CCNServiceObject CCNService_Object = null;
        try {
            CCNService_Object = new CCNServiceObject(serviceID, serviceName, serviceVersion, servicePopularity);
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _serviceTable.put(serviceName, CCNService_Object);

    }

    public boolean installService(String serviceName) {
        Bundle bundle = _serviceController.installBundleByCCNIOStream(serviceName);
        if(bundle == null){
            System.out.println("bundle install failed!");
            return false;
        }
        long serviceID = bundle.getBundleId();
        String serviceVersion = bundle.getVersion().toString();

        int servicePopularity = 0;
        servicePopularity = _servicePopularity.get_CCNServicePopularity().get("ccnx:/" + serviceName + ".jar");

        CCNServiceObject CCNService_Object = null;
        try {
            CCNService_Object = new CCNServiceObject(serviceID, serviceName, serviceVersion, servicePopularity);
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _serviceTable.put(serviceName, CCNService_Object);
        return true;
    }

    public void startLocalService(ServiceNameObject serviceNameObject, Interest interest) {
        String serviceName = serviceNameObject.getServiceName();
        String[] args = serviceNameObject.getArgs();
        String serviceVersion = serviceNameObject.getVersion();
        if (service_existed(serviceName)) { //whether a service exists in the CCNServiceTable
            if (serviceVersion == null || same_version(serviceVersion, serviceName)) { //check service version
                System.out.println("Service:"+serviceName+" is existed and executing..");
                _serviceController.executeServiceBySymbolicName(serviceName, args, interest);

                System.out.println("CCNServiceTable: ");
                for (Iterator it = _serviceTable.getMap().keySet().iterator(); it.hasNext();) {
                    String serviceName_key = it.next().toString();
                    System.out.println(serviceName_key);
                }

            } else {
                System.out.println("Need to update the version of Service:"+serviceName+"");
                removeService(serviceName);
                if(!installService(serviceName)){
                    System.err.println("Service install error!!");
                    return;
                }
                startLocalService(serviceNameObject, interest);
            }
        }else {
            System.out.println("Service:"+serviceName+" is not existed and installing..");
            if (serviceTable_withinSize()) {//check whether the CCNServiceTable is full of service
                System.out.println("CCNServiceTable is within the max size...Service:"+serviceName+" is installing..");
                if(!installService(serviceName)){
                    System.err.println("Service install error!!");
                    return;
                }
                startLocalService(serviceNameObject, interest);
            } else {
                System.out.println("CCNServiceTable is outside the max size...Check whether Service:"+serviceName+" can be installed..");
                if (_servicePopularity.get_CCNServicePopularity().get("ccnx:/" + serviceName + ".jar") == 2) {
                    System.out.println("Meet the requirement to replace one old service in CCNServiceTable...Service:"+serviceName+" is installing..");

                    removeService(service_tobeRemoved());

                    if(!installService(serviceName)){
                        System.err.println("Service install error!!");
                        return;
                    }
                    startLocalService(serviceNameObject, interest);
                } else {
                    System.out.println("Do not meet the requirement to replace one old service in CCNServiceTable...Service:"+serviceName+" is dropped..");
                }
            }
        }
    }

    public void startCCNService(String serviceName, CCNFileInputStream serviceStream) {
        _serviceController.executeServiceBySymbolicName(serviceName, null, null);
    }


}
