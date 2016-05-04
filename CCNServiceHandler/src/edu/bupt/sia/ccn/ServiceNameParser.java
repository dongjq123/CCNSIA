package edu.bupt.sia.ccn;

import org.ccnx.ccn.protocol.ContentName;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by fish on 16-4-20.
 */
public class ServiceNameParser {
    public static ServiceNameObject getServiceName(ContentName name) {
        int count = name.count();
        ServiceNameObject serviceNameObject = new ServiceNameObject();
        serviceNameObject.setContentName(name.toURIString());
        LinkedList<String> argslist = new LinkedList<>();
        for (int i = 0; i < count; ++i) {
            String tmp = name.stringComponent(i);
            try {
                tmp = URLDecoder.decode(tmp, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (tmp.charAt(0) == '{' && tmp.charAt(tmp.length() - 1) == '}') {
                tmp = tmp.substring(1, tmp.length()-1);
                String[] t = tmp.split(":");
                switch (t[0]){
                    case "servicename":
                        serviceNameObject.setServiceName(t[1]);
                        break;
                    case "type":
                        serviceNameObject.setType(t[1]);
                        break;
                    case "version":
                        serviceNameObject.setVersion(t[1]);
                        break;
                    case "args":
                        //String arg = t[1].substring(1,t[1].length()-1);
                        argslist.add(t[1]);
                        break;
                }
//                serviceNameObject = JSON.parseObject(tmp, ServiceNameObject.class);
            }
        }
        Iterator<String> it = argslist.iterator();
        String[] arr = new String[argslist.size()];
        for (int i = 0; it.hasNext() ; i++) {
            arr[i] = it.next();
        }
        serviceNameObject.setArgs(arr);
        System.out.println(serviceNameObject);
        return serviceNameObject;
    }

}
