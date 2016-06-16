package edu.bupt.service.img;

import edu.bupt.service.io.CCNIOManage;
import edu.bupt.service.io.ICCNService;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNFileOutputStream;
import org.ccnx.ccn.protocol.Interest;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by fish on 16-5-13.
 */
public class ImgCompress implements ICCNService {
    private CCNIOManage manage;

    public ImgCompress(CCNIOManage manage) {
        this.manage = manage;
    }

    @Override
    public void execute(String[] args, Interest interest) throws IOException {
        if (args.length == 0) {
            System.out.println("args are null, error!!");
            return;
        }
        String content = args[0];
        if (!content.contains("ccnx:/")) {
            content = "ccnx:/" + content;
        }
        System.out.println("content: "+ content);
        BufferedImage img;
        int width;
        int height;
        CCNFileInputStream in = manage.getCCNFile(content);
        img = ImageIO.read(in); // 构造Image对象
        width = img.getWidth(null);    // 得到源图宽
        height = img.getHeight(null);  // 得到源图长
        int w = 600;
        int h = 400;
        if (args.length > 1) {
            w = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            h = Integer.parseInt(args[2]);
        }

        CCNFileOutputStream cfo = manage.writeCCNBack(interest);
        cfo.addOutstandingInterest(interest);
//        CCNFileOutputStream cro = manage.putRepoFile(interest.name().toURIString());


        BufferedImage image_to_save = new BufferedImage(w, h,
                img.getType());
        image_to_save.getGraphics().drawImage(
                img.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0,
                0, null);

////        ImageOutputStream iros = ImageIO.createImageOutputStream(cro);
////        imageWriter.setOutput(iros);
////        imageWriter.write(imageMetaData,
////                new IIOImage(image_to_save, null, null), null);
////        iros.close();
//
//        imageWriter.dispose();

        ImageOutputStream imOut = ImageIO.createImageOutputStream(cfo);
        ImageIO.write(image_to_save,"jpg", imOut);
//        ImageOutputStream imROut = ImageIO.createImageOutputStream(cro);
//        ImageIO.write(image_to_save,"jpg", imROut);
        cfo.close();
//        cro.close();
    }

}
