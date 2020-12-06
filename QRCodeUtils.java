package com.gzy.qrcode.utils;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * <h1>二维码工具类<h1/>
 * 用于二维码的编码与解码
 * @author GaoZiYang
 * @date 2020年12月06日 17:43:34
 */
public class QRCodeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(QRCodeUtils.class);

    private static final String CHARSET = "UTF-8";
    private static final String DEFAULT_FORMAT_NAME = "JPG";
    private static final int DEFAULT_QRCODE_SIZE = 600;
    private static final int DEFAULT_IMG_SIZE = 120;

    /** 二维码的尺寸 */
    private static int size = DEFAULT_QRCODE_SIZE;
    /** 二维码中间Logo图片的尺寸 */
    private static int logoSize = DEFAULT_IMG_SIZE;
    /** 二维码的图片格式 */
    private static String formatName = DEFAULT_FORMAT_NAME;

    /**
     * 创建二维码图片
     * @param content 要在二维码中编码的内容
     * @param imgPath 要插入的Logo图片路径
     * @param needCompress 是否压缩Logo图片的尺寸
     * @return 二维码图片
     * @throws Exception 文件相关异常
     */
    private static BufferedImage createImage(String content, String imgPath, boolean needCompress) throws Exception {
        // 二维码编码器的参数
        HashMap hints = new HashMap();
        // 设置矫正级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置字符集
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        // 设置外边距（这里其实相当于内边距）
        hints.put(EncodeHintType.MARGIN, 1);

        // 编码二维码的二进制2D矩阵
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        // 根据二进制2D矩阵初始化二维码图片
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 根据二进制2D矩阵给这张二维码图片填色
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        // 判断是否需要插入Logo图片
        if (imgPath == null || "".equals(imgPath)) {
            return image;
        }
        // 插入Logo图片
        insertImage(image, imgPath, needCompress);
        return image;
    }

    /**
     * 向二维码中间插入Logo图片
     * @param source 二维码图片
     * @param imgPath Logo图片路径
     * @param needCompress 是否开启压缩Logo图片尺寸
     * @throws Exception 文件相关异常
     */
    private static void insertImage(BufferedImage source, String imgPath, boolean needCompress) throws Exception {
        // 文件校验
        File file = new File(imgPath);
        if (!file.exists()) {
            LOGGER.warn(imgPath + " 该文件不存在！");
            return;
        }

        // 初始化Logo图片
        Image src = ImageIO.read(new File(imgPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);

        // 压缩Logo图片
        if (needCompress) {
            if (width > logoSize) {
                width = logoSize;
            }
            if (height > logoSize) {
                height = logoSize;
            }
            Image image = src.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            // 绘制缩小后的Logo图
            g.drawImage(image, 0, 0, null);
            g.dispose();
            src = image;
        }

        // 绘制Logo图片
        Graphics2D graph = source.createGraphics();
        int x = (size - width) / 2;
        int y = (size - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, height, 12, 12);
        // 开启图像和文字的抗锯齿效果
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graph.setStroke(new BasicStroke(10f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * 编码二维码图片，以指定文件路径输出
     * @param content 要在二维码中编码的内容
     * @param imgPath 要插入的Logo图片路径
     * @param destPath 二维码图片输出的文件路径
     * @param needCompress 是否压缩Logo图片尺寸
     * @param cSize 二维码图片大小
     * @param lSize Logo图片大小
     * @throws Exception 文件相关异常
     */
    public static void encode(String content, String imgPath, String destPath, boolean needCompress, int cSize, int lSize) throws Exception {
        size = cSize;
        logoSize = lSize;

        File file = new File(destPath);
        // 当文件路径不存在时，mkdirs方法会自动创建多层目录
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        LOGGER.info("文件已保存至：" + file.getAbsoluteFile());
        ImageIO.write(createImage(content, imgPath, needCompress), formatName, file);
    }

    /**
     * 编码二维码图片，以指定文件路径输出
     * @param content 要在二维码中编码的内容
     * @param imgPath 要插入的Logo图片路径
     * @param destPath 二维码图片输出的文件路径
     * @param needCompress 是否压缩Logo图片尺寸
     * @throws Exception 文件相关异常
     */
    public static void encode(String content, String imgPath, String destPath, boolean needCompress) throws Exception {
        File file = new File(destPath);
        // 当文件路径不存在时，mkdirs方法会自动创建多层目录
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        LOGGER.info("文件已保存至：" + file.getAbsoluteFile());
        ImageIO.write(createImage(content, imgPath, needCompress), formatName, file);
    }

    /**
     * 编码二维码图片，以流的形式输出
     * @param content 要在二维码中编码的内容
     * @param imgPath 要插入的Logo图片路径
     * @param output 二维码图片输出的流
     * @param needCompress 是否压缩Logo图片尺寸
     * @throws Exception 文件相关异常
     */
    public static void encode(String content, String imgPath, OutputStream output, boolean needCompress) throws Exception {
        ImageIO.write(createImage(content, imgPath, needCompress), formatName, output);
    }

    /**
     * 编码二维码图片，默认开启Logo图片尺寸压缩
     * @param content 要在二维码中编码的内容
     * @param imgPath 要插入的Logo图片路径
     * @param destPath 二维码图片路径
     * @throws Exception 文件相关异常
     */
    public static void encode(String content, String imgPath, String destPath) throws Exception {
        encode(content, imgPath, destPath, true);
    }

    /**
     * 编码二维码图片，默认以当前系统时间作为文件名
     * @param content 要在二维码中编码的内容
     * @param imgPath 要插入的Logo图片路径
     * @throws Exception 文件相关异常
     */
    public static void encode(String content, String imgPath) throws Exception {
        String destPath = "src/main/resources/" + System.currentTimeMillis() + "." + formatName;
        encode(content, imgPath, destPath, true);
    }

    /**
     * 编码二维码图片，默认不插入Logo图片
     * @param content 要在二维码中编码的内容
     * @throws Exception 文件相关异常
     */
    public static void encode(String content) throws Exception {
        String destPath = "src/main/resources/" + System.currentTimeMillis() + "." + formatName;
        encode(content, null, destPath, true);
    }

    /**
     * 解码二维码图片
     * @param file 二维码图片文件
     * @return 二维码中编码的内容
     * @throws Exception 文件相关异常
     */
    public static String decode(File file) throws Exception {
        // 文件校验
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            return null;
        }

        // 根据二维码图片获取对应的二进制2D矩阵
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        // 二维码编码器的参数
        HashMap hints = new HashMap();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);

        // 解码二维码的二进制2D矩阵
        Result result = new MultiFormatReader().decode(binaryBitmap, hints);
        return result.getText();
    }

    /**
     * 解码二维码图片
     * @param path 二维码图片的路径
     * @return 二维码中编码的内容
     * @throws Exception 文件相关异常
     */
    public static String decode(String path) throws Exception {
        return decode(new File(path));
    }

    /**
     * 用于解码二维码图片
     */
    private static class BufferedImageLuminanceSource extends LuminanceSource {
        private final BufferedImage image;
        private final int left;
        private final int top;

        public BufferedImageLuminanceSource(BufferedImage image) {
            this(image, 0, 0, image.getWidth(), image.getHeight());
        }

        public BufferedImageLuminanceSource(BufferedImage image, int left, int top, int width, int height) {
            super(width, height);

            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();
            if (left + width > sourceWidth || top + height > sourceHeight) {
                throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
            }

            for (int y = top; y < top + height; y++) {
                for (int x = left; x < left + width; x++) {
                    if ((image.getRGB(x, y) & 0xFF000000) == 0) {
                        image.setRGB(x, y, 0xFFFFFFFF);
                    }
                }
            }

            this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);
            this.image.getGraphics().drawImage(image, 0, 0, null);
            this.left = left;
            this.top = top;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            if (y < 0 || y >= getHeight()) {
                throw new IllegalArgumentException("Requested row is outside the image: " + y);
            }
            int width = getWidth();
            if (row == null || row.length < width) {
                row = new byte[width];
            }
            image.getRaster().getDataElements(left, top + y, width, 1, row);
            return row;
        }

        @Override
        public byte[] getMatrix() {
            int width = getWidth();
            int height = getHeight();
            int area = width * height;
            byte[] matrix = new byte[area];
            image.getRaster().getDataElements(left, top, width, height, matrix);
            return matrix;
        }

        @Override
        public boolean isCropSupported() {
            return true;
        }

        @Override
        public LuminanceSource crop(int left, int top, int width, int height) {
            return new BufferedImageLuminanceSource(image, this.left + left, this.top + top, width, height);
        }

        @Override
        public boolean isRotateSupported() {
            return true;
        }

        @Override
        public LuminanceSource rotateCounterClockwise() {
            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();
            AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);
            BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = rotatedImage.createGraphics();
            g.drawImage(image, transform, null);
            g.dispose();
            int width = getWidth();
            return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
        }
    }
}
