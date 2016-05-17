package edu.bupt.service.img;

import edu.bupt.service.io.CCNIOManage;
import edu.bupt.service.io.ICCNService;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNFileOutputStream;
import org.ccnx.ccn.protocol.Interest;

import javax.imageio.ImageIO;
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
    private Image img;
    private int width;
    private int height;


    public ImgCompress(CCNIOManage manage){
        this.manage = manage;
    }

    @Override
    public void execute(String[] args, Interest interest) throws IOException {
        if(args.length == 0){
            System.out.println("args are null, error!!");
            return;
        }
        String content = args[0];
        if(!content.contains("ccnx:/")){
            content = "ccnx:/"+content;
        }
        CCNFileInputStream in = manage.getCCNFile(content);
        img = ImageIO.read(in); // 构造Image对象
        width = img.getWidth(null);    // 得到源图宽
        height = img.getHeight(null);  // 得到源图长
        int w = 600;
        int h = 400;
        if (args.length > 1){
            w = Integer.parseInt(args[1]);
        }
        if (args.length > 2){
            h = Integer.parseInt(args[2]);
        }

        CCNFileOutputStream cfo = manage.writeCCNBack(interest);
        DataOutputStream dot = new DataOutputStream(cfo);
        DataOutputStream drt = new DataOutputStream(manage.putRepoFile(interest.getContentName().toURIString()));
        ClassLoader cc = Thread.currentThread().getContextClassLoader();
        try {
            Class jc = cc.loadClass("com.sun.image.codec.jpeg.JPEGCodec");
            Class jie = cc.loadClass("com.sun.image.codec.jpeg.JPEGImageEncoder");
            Method jcm = jc.getMethod("createJPEGEncoder", OutputStream.class);
            Method jiem = jie.getMethod("encode", BufferedImage.class);
            Object encoder1 = jcm.invoke(null, new Object[]{dot});
            Object encoder2 = jcm.invoke(null, new Object[]{drt});
            BufferedImage re = resize(w,h);
            jiem.invoke(encoder1,new Object[]{re}); // JPEG编码
            jiem.invoke(encoder2,new Object[]{re}); // JPEG编码
            dot.flush();
            drt.flush();
            dot.close();
            drt.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 按照宽度还是高度进行压缩
     * @param w int 最大宽度
     * @param h int 最大高度
     */
    public BufferedImage resizeFix(int w, int h) throws IOException {
        if (width / height > w / h) {
            return resizeByWidth(w);
        } else {
            return resizeByHeight(h);
        }
    }
    /**
     * 以宽度为基准，等比例放缩图片
     * @param w int 新宽度
     */
    public BufferedImage resizeByWidth(int w) throws IOException {
        int h = (int) (height * w / width);
        return  resize(w, h);
    }
    /**
     * 以高度为基准，等比例缩放图片
     * @param h int 新高度
     * @return BufferedImage 返回调整后的image对象
     */
    public BufferedImage resizeByHeight(int h) throws IOException {
        int w = (int) (width * h / height);
        return resize(w, h);
    }
    /**
     * 强制压缩/放大图片到固定的大小
     * @param w int 新宽度
     * @param h int 新高度
     * @return BufferedImage 返回调整后的image对象
     */
    public BufferedImage resize(int w, int h) throws IOException {
        // SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢
        BufferedImage image = new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB );
        image.getGraphics().drawImage(img, 0, 0, w, h, null); // 绘制缩小后的图
        //File destFile = new File("/home/fish/DSC_4299.JPG");
        //FileOutputStream out = new FileOutputStream(destFile); // 输出到文件流
        // 可以正常实现bmp、png、gif转jpg
        return image;
    }
}
