package edu.bupt.service.html;

import java.io.*;

import edu.bupt.service.io.CCNIOManage;
import edu.bupt.service.io.ICCNService;
import org.ccnx.ccn.io.CCNFileOutputStream;
import org.ccnx.ccn.protocol.Interest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by yangkuang on 16-4-21.
 */
public class WebpageAdaptation implements ICCNService{
    private CCNIOManage manage;
    private static boolean hasRun=false;

    public WebpageAdaptation(CCNIOManage manage){
        this.manage = manage;
    }
    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }

    @Override
    public void execute(String args[], Interest interest) throws IOException {
        //String url = "file:///home/yangkuang/source/test.html";
        //File htmlFile = new File("/home/yangkuang/source/test.html");
        //InputStream ips = new FileInputStream(htmlFile);
        if (!hasRun) {
            if (args.length > 1) {
                String htmlFile = "ccnx:/contents/"+args[0];
                InputStream ips = manage.getCCNFile(htmlFile);
                ParseHtml htmlParser = new ParseHtml(ips, "UTF-8", "ccnx:/");
                Document htmlDocument = htmlParser.getDocument();
                Elements jpgs = htmlDocument.select("img[src$=.jpg]");

                System.out.println(jpgs.size());

                for (Element src : jpgs) {
                    if (src.tagName().equals("img")) {
                        print(" * %s: %sx%s (%s)",
                                src.tagName(), src.attr("width"), src.attr("height"),
                                trim(src.attr("alt"), 40));
                    } else {
                        print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
                    }
                }

                for (Element src : jpgs) {
                    src.attr("width", "50");
                    src.attr("height", "50");
                }

                for (Element src : jpgs) {
                    if (src.tagName().equals("img")) {
                        print(" * %s: %sx%s (%s)",
                                src.tagName(), src.attr("width"), src.attr("height"),
                                trim(src.attr("alt"), 40));
                    } else {
                        print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
                    }
                }

                CCNFileOutputStream cfo = manage.writeCCNBack(interest);
                DataOutputStream dot = new DataOutputStream(cfo);
                DataOutputStream drt = new DataOutputStream(manage.putRepoFile(interest.getContentName().toURIString()));
                dot.writeChars(htmlDocument.html());
                drt.writeChars(htmlDocument.html());
                dot.flush();
                drt.flush();
                dot.close();
                drt.close();

            }else {
                throw new IOException("args should not be null!!");
            }
            hasRun = true;
        }
    }

}
