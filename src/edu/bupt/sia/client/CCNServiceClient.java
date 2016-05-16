package edu.bupt.sia.client;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNInputStream;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Created by yangkuang on 16-5-13.
 */
public class CCNServiceClient {
    CCNHandle ccnHandle;
    public static boolean unversioned = false;

    public CCNServiceClient() throws NullPointerException{
        try {
            this.ccnHandle = CCNHandle.open();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CCNFileInputStream CCNGetStream(String name) {
        try {
            //CCNHandle ccnHandle = CCNHandle.open();
            System.out.println("getCCNFile:" + name);
            ContentName ccname = ContentName.fromURI(name);

            CCNFileInputStream input;
            input = new CCNFileInputStream(ccname, ccnHandle);
            return input;
        } catch (MalformedContentNameStringException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void CCNGetFile(String Name) {
        try {
            short var20 = 1024;
            ContentName ccname = ContentName.fromURI(Name);
            String[] splitList = Name.split("/");
            String imgName = splitList[splitList.length-1];
            File theFile = new File("/home/yangkuang/"+imgName+"");
            if(theFile.exists()) {
                System.out.println("Overwriting file: " + Name);
            }

            FileOutputStream output = new FileOutputStream(theFile);
            //long starttime = System.currentTimeMillis();
            Object input;
            if(unversioned) {
                input = new CCNInputStream(ccname , ccnHandle);
            } else {
                input = new CCNFileInputStream(ccname , ccnHandle);
            }

            byte[] buffer = new byte[var20];
            boolean readcount = false;
            long readtotal = 0L;

            int var21;
            while((var21 = ((CCNInputStream)input).read(buffer)) != -1) {
                readtotal += (long)var21;
                output.write(buffer, 0, var21);
                output.flush();
            }

            //System.out.println("ccngetfile took: " + (System.currentTimeMillis() - starttime) + "ms");
            //System.out.println("Retrieved content " + args[1] + " got " + readtotal + " bytes.");

        } catch (MalformedContentNameStringException var17) {
            //System.out.println("Malformed name: " + args[0] + " " + var17.getMessage());
            var17.printStackTrace();
        } catch (IOException var18) {
            System.out.println("Cannot write file or read content. " + var18.getMessage());
            var18.printStackTrace();
        }

    }

    public void CCNGetImg(CCNInputStream input) {
        try {
            ParseHtml htmlParser = new ParseHtml(input, "UTF-8", "");
            Document htmlDocument = htmlParser.getDocument();
            System.out.println(htmlDocument.html());
            Elements imgs = htmlDocument.getElementsByTag("img");

            if (imgs!=null) {
                Iterator<Element> images = imgs.iterator();
                while (images.hasNext()) {
                    Element img = images.next();
                    String src = img.attr("src");
                    if (src != null && src.startsWith("ccnx:/")) {
                        CCNGetFile(src); //get the img
                    }
                }
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        try {
            String content = URLEncoder.encode("{content:test.html}", "UTF-8");
            String arg1 = URLEncoder.encode("{args:test.html}", "UTF-8");
            String arg2 = URLEncoder.encode("{args:test.html}", "UTF-8");

            String html = "ccnx:/service/edu/bupt/service/htmlparse/"+content+"/"+arg1+"/"+arg2;
            System.out.println(html);
            CCNServiceClient csc = new CCNServiceClient();
            csc.CCNGetImg(csc.CCNGetStream(html));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

