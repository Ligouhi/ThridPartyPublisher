package segmentor;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ImageUtils {
//水平垂直翻转bitmap
    public static  Bitmap horverBitmap(Bitmap bitmap, boolean H, boolean V)
    {
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();

        if (H)
            matrix.postScale(-1, 1);   //水平翻转H

        if (V)
            matrix.postScale(1, -1);   //垂直翻转V

        if (H && V)
            matrix.postScale(-1, -1);   //水平&垂直翻转HV

        return Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);

        //matrix.postRotate(-90);  //旋转-90度
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap Bitmap
     */
    public static void saveBitmap(Bitmap bitmap,String path) {
        String savePath;
        File filePic;
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = path;
        } else {
            Log.e("tag", "saveBitmap failure : sdcard not mounted");
            return;
        }
        try {
            filePic = new File(root,savePath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("tag", "saveBitmap: " + e.getMessage());
            return;
        }
        Log.i("tag", "saveBitmap success: " + filePic.getAbsolutePath());
    }

    /**
     * @方法描述 Bitmap转RGBA
     */
    public static byte[] bitmap2RGBA(Bitmap bitmap,int width,int height) {
        if (bitmap != null && bitmap.getWidth() >= width && bitmap.getHeight() >= height) {
        int[] argb = new int[width * height];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);

        int bindex = width * height * 4-1;
        int index = 0;
        byte[] rgba = new byte[width * height * 4];
        for (int j = height-1; j >= 0; --j) {
            for (int i = width-1; i >= 0; --i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int A = (argb[index] & 0xFF000000) >> 24;
                rgba[bindex--] = (byte)(R);
                rgba[bindex--] = (byte)(G);
                rgba[bindex--] = (byte)(B);
                rgba[bindex--] = (byte)(A);
                ++index;
            }
        }
        return rgba;
        }
        else {
            Log.e("ImageUtils","bitmapwith:"+bitmap.getWidth()+"bitmapheight:"+bitmap.getHeight()+"width:"+width+"height:"+height);
            return null;
        }

//        //2argb
//        byte[] pixels = new byte[rgba.length ];
//
//        int count = rgba.length / 4;
//
//        //Bitmap像素点的色彩通道排列顺序是ARGB
//        for (int i = 0; i < count; i++) {
//
//            pixels[i * 4] = rgba[i * 4 + 1];
//            pixels[i * 4 + 1] = rgba[i * 4+2];
//            pixels[i * 4 + 2] = rgba[i * 4 + 3];
//            pixels[i * 4 + 3] = rgba[i * 4];
//        }
//
//        return pixels;
    }
        /**
         * @方法描述 将RGBA字节数组转换成Bitmap，
         */
    static public Bitmap rgba2Bitmap(byte[] data, int width, int height) {
        int[] colors = convertByteToColor(data);    //取RGBA值转换为int数组
        if (colors == null) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        return bmp;
    }


    // 将一个byte数转成int
    // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
    public static int convertByteToInt(byte data) {

        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }


    // 将纯RGB数据数组转化成int像素数组
    public static int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 4 != 0) {
            arg = 1;
        }

        // 一般RGBA字节数组的长度应该是4的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size / 4 + arg];
        int red, green, blue,alpha;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = colorLen-1; i >= 0 ; --i) {
                alpha = convertByteToInt(data[i * 4]);
                blue = convertByteToInt(data[i * 4 + 1]);
                green = convertByteToInt(data[i * 4 + 2]);
                red = convertByteToInt(data[i * 4 + 3]);
                // 获取RGB分量值通过按位或生成int的像素值
                color[colorLen - 1 - i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
            }
        } else {
            for (int i = colorLen-1; i >= 0 ; --i) {
                alpha = convertByteToInt(data[i * 4]);
                blue = convertByteToInt(data[i * 4 + 1]);
                green = convertByteToInt(data[i * 4 + 2]);
                red = convertByteToInt(data[i * 4 + 3]);
                color[colorLen - 1 - i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }

    //bitmap 旋转
    public static Bitmap adjustPhotoRotation(Bitmap bitmap, int orientationDegree) {



        Matrix matrix = new Matrix();
        matrix.setRotate(orientationDegree, (float) bitmap.getWidth() / 2,
                (float) bitmap.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bitmap.getHeight();
            targetY = 0;
        } else {
            targetX = bitmap.getHeight();
            targetY = bitmap.getWidth();
        }


        final float[] values = new float[9];
        matrix.getValues(values);


        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];


        matrix.postTranslate(targetX - x1, targetY - y1);


        Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(),
                Bitmap.Config.ARGB_8888);


        Paint paint = new Paint();
        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawBitmap(bitmap, matrix, paint);


        return canvasBitmap;
    }


    //旋转180
    public static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;
        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }
//旋转90度
public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }
    /**
     * Bitmap转化为ARGB数据，再转化为NV21数据
     *
     * @param src    传入ARGB_8888的Bitmap
     * @param width  NV21图像的宽度
     * @param height NV21图像的高度
     * @return nv21数据
     */
    public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
        if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
            int[] argb = new int[width * height];
            src.getPixels(argb, 0, width, 0, 0, width, height);
            return argbToNv21(argb, width, height);
        } else {
            return null;
        }
    }

    /**
     * ARGB数据转化为NV21数据
     *
     * @param argb   argb数据
     * @param width  宽度
     * @param height 高度
     * @return nv21数据
     */
    public static byte[] argbToNv21(int[] argb, int width, int height) {
        int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int index = 0;
        byte[] nv21 = new byte[width * height * 3 / 2];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
                int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
                int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
                }

                ++index;
            }
        }
        return nv21;
    }
    private static void copy(ByteBuffer data, ByteBuffer uv, int yl) {
        int l = uv.remaining();
        int quarter = yl / 4;
        int half = yl / 2;
        if (l == quarter) {
            // 魅族M8，长度刚好是y的四分之一，直接写入。
            data.put(uv);
        } else if (quarter < l && l <= half) {
            // 华为荣耀，实际读取到的长度是y的(1 / 2 - 1)
            for (int i = 0; i < l; i++) {
                byte b = uv.get();
                if (i % 2 == 0) {
                    data.put(b);
                }
            }
        } else if (l > half) {
            // 未发现此种情况，先预留着
            for (int i = 0; i < l; i++) {
                byte b = uv.get();
                if (i % 4 == 0) {
                    data.put(b);
                }
            }
        }
    }

    public static Mat yuv420(Image image) {
        // yuv420图片有三个通道，按顺序下来分别对应YUV
        // 转换需要把三个通道的数据按顺序合并在一个数组里，
        // 即全部Y，随后全部U，再随后全部是V，
        // 再由此数组生成Yuv420的Mat，
        // 之后可以利用opencv将其转为其他格式
        Image.Plane[] plans = image.getPlanes();
        ByteBuffer y = plans[0].getBuffer();
        ByteBuffer u = plans[1].getBuffer();
        ByteBuffer v = plans[2].getBuffer();
        // 此处需要把postition移到0才能读取
        y.position(0);
        u.position(0);
        v.position(0);
        int yl = y.remaining();
        // yuv420即4个Y对应1个U和一个V，即4:1:1的关系，长度刚好是Y的1.5倍
        ByteBuffer data = ByteBuffer.allocateDirect(yl * 3 / 2);
        // y通道直接全部插入
        data.put(y);
        copy(data, u, yl);
        copy(data, v, yl);
        // 生成Yuv420格式的Mat
        int rows = image.getHeight();
        int cols = image.getWidth();
        return new Mat(rows * 3 / 2, cols, CvType.CV_8UC1, data);
    }
    /**
     * YUV数据转化为bitmap
     *
     * @param data   yuv数据
     * @param pwidth  宽度
     * @param pheight 高度
     * @return bitmap
     */
    public static Bitmap yuv2Bitmap(byte[] data, int pwidth, int pheight){
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, pwidth, (int) (pheight), null);//20、20分别是图的宽度与高度
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, pwidth, pheight), 50, baos);//80--JPG图片的质量[0-100],100最高
        byte[] jdata = baos.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);


        return bitmap;
    }

    /**
     * bitmap resize
     *
     * @param bitmap   yuv数据
     * @param width  宽度
     * @param height 高度
     * @return bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap,int width,int height){
        //resize
        int oldwidth = bitmap.getWidth();
        int oldheight = bitmap.getHeight();
        float scaleWidth = ((float) width) / oldwidth;
        float scaleHeight = ((float) height) / oldheight;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, oldwidth, oldheight, matrix, false);
        return resizedBitmap;
    }
}

