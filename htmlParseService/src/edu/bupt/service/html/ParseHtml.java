package edu.bupt.service.html;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * Created by yangkuang on 16-4-21.
 */
public class ParseHtml {
    private Document document = null;

    public ParseHtml(InputStream in, String charsetName, String baseUri) throws IOException {
        // 将Html写到字符串resource里
        // Jsoup还有其他构造方法：connect(String url).get()
        // Html文件：parse(File in, String charsetName)
        //document = Jsoup.parse(resource);
        //document = Jsoup.connect(url).get();
        //document = Jsoup.parse(htmlFile, charsetName);
        document = Jsoup.parse(in, charsetName, baseUri); //参数 baseUri是用来将相对URL转成绝对URL
    }

    public Element getElementById(String id) {
        return document.getElementById(id);
    }

    public List<Element> getElementByIds(String... ids) {
        List<Element> elements = new ArrayList<Element>(ids.length);
        for (String id : ids)
        {
            elements.add(getElementById(id));
        }
        return elements;
    }

    public Elements getElementsByTagName(String name)
    {
        return document.getElementsByTag(name);
    }

    public Document getDocument() {
        return document;
    }
}
