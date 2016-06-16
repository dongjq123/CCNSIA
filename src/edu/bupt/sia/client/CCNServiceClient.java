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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    public void CCNGetImg(CCNInputStream input) {
        try {
            ParseHtml htmlParser = new ParseHtml(input, "UTF-8", "");
            Document htmlDocument = htmlParser.getDocument();
            System.out.println(htmlDocument.html());
            input.close();
            Elements imgs = htmlDocument.getElementsByTag("img");
            // 定义一个缓冲的线程值 线程池的大小根据任务变化
            ExecutorService threadPool = Executors.newCachedThreadPool();
            if (imgs!=null) {
                Iterator<Element> images = imgs.iterator();
                while (images.hasNext()) {
                    Element img = images.next();
                    String src = img.attr("src");
                    if (src != null && src.startsWith("ccnx:/")) {
                        //CCNGetFile(src); //get the img
                        threadPool.execute(new GetImgFile(src));
                    }
                }
            }
            // 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。
            threadPool.shutdown();
            try {
                // 请求关闭、发生超时或者当前线程中断，无论哪一个首先发生之后，都将导致阻塞，直到所有任务完成执行
                // 设置最长等待10秒
                threadPool.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //
                e.printStackTrace();
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();    //获取开始时间
            String content = URLEncoder.encode("{content:test.html}", "UTF-8");
            String arg1 = URLEncoder.encode("{args:test.html}", "UTF-8");
            String arg2 = URLEncoder.encode("{args:test3.html}", "UTF-8");

            String html = "ccnx:/service/edu/bupt/service/htmlparse/"+arg1+"/"+arg2;
            String html1 = "ccnx:/contents/test.html";
//            System.out.println(html);
            CCNServiceClient csc = new CCNServiceClient();
            csc.CCNGetImg(csc.CCNGetStream(html));
            long endTime = System.currentTimeMillis();    //获取结束时间
            System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间


//            String content1 = URLEncoder.encode("{content:DSC_2925.JPG}", "UTF-8");
//            String arg3 = URLEncoder.encode("{args:DSC_2925.JPG}", "UTF-8");
//            String w = URLEncoder.encode("{args:300}", "UTF-8");
//            String h = URLEncoder.encode("{args:200}", "UTF-8");
//            String v = URLEncoder.encode("{version:DSC_2925.JPG}", "UTF-8");
//            String img = "ccnx:/service/edu/bupt/service/imgcompress/"+arg3+"/"+w+"/"+h+"/";
//            System.out.println(img);
//            CCNServiceClient csc = new CCNServiceClient();
//            csc.CCNGetFile(img);

            csc.ccnHandle.close();
            System.exit(0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    class GetImgFile implements Runnable{
        String imgsrc;

        public GetImgFile(String src){
            this.imgsrc = src;
        }

        @Override
        public void run() {
            try {
                short var20 = 1024;
                ContentName ccname = ContentName.fromURI(imgsrc);
                String[] splitList = imgsrc.split("/");
                String imgName = splitList[splitList.length-1];
                File theFile = new File("/home/fish/"+imgName+"");
                if(theFile.exists()) {
                    System.out.println("Overwriting file: " + imgsrc);
                }

                FileOutputStream output = new FileOutputStream(theFile);
                //long starttime = System.currentTimeMillis();
                CCNInputStream input;
                if(unversioned) {
                    input = new CCNInputStream(ccname , ccnHandle);
                } else {
                    input = new CCNFileInputStream(ccname , ccnHandle);
                }
                input.setTimeout(200000);
                byte[] buffer = new byte[var20];
                boolean readcount = false;
                long readtotal = 0L;

                int var21;
                while((var21 = input.read(buffer)) != -1) {
                    readtotal += (long)var21;
                    output.write(buffer, 0, var21);
                    output.flush();
                }

                //System.out.println("ccngetfile took: " + (System.currentTimeMillis() - starttime) + "ms");
                System.out.println("Retrieved content " + imgsrc + " got " + readtotal + " bytes.");

                input.close();
                output.close();

            } catch (MalformedContentNameStringException var17) {
                //System.out.println("Malformed name: " + args[0] + " " + var17.getMessage());
                var17.printStackTrace();
            } catch (IOException var18) {
                System.out.println("Cannot write file or read content. " + var18.getMessage());
                var18.printStackTrace();
            }
        }
    }
}

