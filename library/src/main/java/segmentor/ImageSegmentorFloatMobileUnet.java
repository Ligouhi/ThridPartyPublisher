/* Copyright 2018 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package segmentor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2BGR;

/** This segmentor works with the float mobile-unet model. */
public class ImageSegmentorFloatMobileUnet extends ImageSegmentor {

  /** The mobile net requires additional normalization of the used input. */
  private static final float IMAGE_MEAN = 0;//127.5f;

  private static final float IMAGE_STD = 255.f;//127.5f;
    private static final int mskthresh=50;
  /**
   * An array to hold inference results, to be feed into Tensorflow Lite as outputs. This isn't part
   * of the super class, because we need a primitive array here.
   */
//  private int[][][][][] segmap = null;
  private float[][] segmap = null;

  int opsize=128;
  int k=0;
  int frame_hieght=0;
  int frame_width=0;

  Bitmap resbmp, tmpbmp,harbmp ;
  Boolean prop_set = Boolean.FALSE;


  /**
   * Initializes an {@code ImageSegmentorFloatMobileUnet}.
   *
   * @param activity
   */
  public ImageSegmentorFloatMobileUnet(Activity activity) throws IOException {
    super(activity);
//      segmap = new int[1][opsize][opsize][1][1];
      segmap = new float[1][opsize*opsize];

  }

  @Override
  protected String getModelPath() {

//    return "frozen_model-31.tflite";
    return "deconv_fin_munet.tflite";

  }


  @Override
  protected int getImageSizeX() {
    return 128;
  }

  @Override
  protected int getImageSizeY() {
    return 128;
  }

  @Override
  protected int getNumBytesPerChannel() {
    return 4; // Float.SIZE / Byte.SIZE;
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN)/ IMAGE_STD);
    imgData.putFloat((((pixelValue >> 8) & 0xFF)  - IMAGE_MEAN)/ IMAGE_STD);
    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN)/ IMAGE_STD);
  }

  @Override
  protected void runInference( ) {

    tflite.run(imgData, segmap);
  }



  @Override
  public Bitmap imageblend(Bitmap fg_bmp, Bitmap bg_bmp, Boolean harmonize,int width, int height){

    //Set global frame sizes & bitmaps on init()
    if (!prop_set) {
        /* Set size of global frames and initialize bitmaps */
        frame_hieght = height;
        frame_width = width;
        resbmp = Bitmap.createBitmap(frame_width,frame_hieght, Bitmap.Config.ARGB_8888);
        tmpbmp = Bitmap.createBitmap(frame_width,frame_hieght, Bitmap.Config.ARGB_8888);
        harbmp = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);
        prop_set= Boolean.TRUE;
    }

    Mat fg = new Mat(frame_hieght,frame_width,CV_8UC3);
    Mat bg = new Mat(frame_hieght,frame_width,CV_8UC3);
    Mat bgcpy = new Mat(frame_hieght,frame_width,CV_8UC3);

    Mat mskmat = new Mat (opsize, opsize, CV_32F);
    Mat invmskmat = new Mat(frame_hieght,frame_width,CV_32FC3, new Scalar(1.0,1.0,1.0));

    Mat resmat = new Mat(frame_hieght,frame_width,CV_32FC3);

    if (segmap!=null){
//        float [][] tmp = new float[1][opsize*opsize];
//        for(int i=0;i<128;i++)
//            for(int j=0;j<128;j++){
//                tmp[0][i*128+j] = segmap[0][i][j][0][0];
//            }
//        mskmat.put(0,0,tmp[0]);
      mskmat.put(0,0,segmap[0]);


      Imgproc.threshold(mskmat,mskmat,mskthresh/100.0,1.0,Imgproc.THRESH_TOZERO);
      Imgproc.GaussianBlur(mskmat,mskmat,new Size(7,7),0);
      Core.pow(mskmat,2.0,mskmat);

      Imgproc.resize(mskmat,mskmat, new Size(frame_width, frame_hieght));
      Imgproc.cvtColor(mskmat,mskmat,Imgproc.COLOR_GRAY2BGR);

      Utils.bitmapToMat(fg_bmp, fg);
      Utils.bitmapToMat(bg_bmp, bg);

      Imgproc.cvtColor(fg,fg, COLOR_BGRA2BGR);
      Imgproc.cvtColor(bg,bg, COLOR_BGRA2BGR);



      Log.d("FG", String.valueOf(fg.type()));
      Log.d("BG", String.valueOf(bg.type()));

      fg.convertTo(fg,CV_32FC3,1.0/255.0);
      bg.convertTo(bg,CV_32FC3,1.0/255.0);

      //maskmat：人像部分 invmskmat:背景部分
      Core.subtract(invmskmat,mskmat,invmskmat);
      Core.multiply(fg,mskmat,fg);
      Core.multiply(bg,invmskmat, bg);
      Core.add(fg,bg,resmat);

      resmat.convertTo(resmat,CV_8UC3,255);

      Utils.matToBitmap(resmat,resbmp);

    }
    return resbmp;
  }


  @Override
  public Bitmap videobokeh(Bitmap fg_bmp,int width,int height){
    //Set global frame sizes & bitmaps on init()
    if (!prop_set) {
      /* Set size of global frames and initialize bitmaps */
      frame_hieght = height;
      frame_width = width;
      resbmp = Bitmap.createBitmap(frame_width,frame_hieght, Bitmap.Config.ARGB_8888);
      prop_set= Boolean.TRUE;
    }

    Mat fg = new Mat(frame_hieght,frame_width,CV_8UC3);
    Mat bg = new Mat(frame_hieght,frame_width,CV_8UC3);
    Mat mskmat = new Mat (opsize, opsize, CV_32F);
    Mat invmskmat = new Mat(frame_hieght,frame_width,CV_32FC3, new Scalar(1.0,1.0,1.0));

    Mat resmat = new Mat(frame_hieght,frame_width,CV_32FC3);

    if (segmap!=null){

//        int[] tmp = new int[opsize*opsize];
//        for(int i=0;i<128;i++)
//            for(int j=0;j<128;j++){
//                tmp[i*128+j] = segmap[0][i][j][0][0];
//            }
//        mskmat.put(0,0,tmp);
         mskmat.put(0,0,segmap[0]);

      // Prepare the mask
      Core.multiply(mskmat,new Scalar(2.0),mskmat);
      Imgproc.threshold(mskmat, mskmat,1.0,1.0,Imgproc.THRESH_TRUNC);
      Core.pow(mskmat,2.0,mskmat);

      Imgproc.resize(mskmat,mskmat, new Size(frame_width, frame_hieght));
      Imgproc.cvtColor(mskmat,mskmat,Imgproc.COLOR_GRAY2BGR);

      Utils.bitmapToMat(fg_bmp, fg);
      Utils.bitmapToMat(fg_bmp, bg);
      Imgproc.resize(bg,bg, new Size(frame_width, frame_hieght));
      Imgproc.resize(fg,fg, new Size(frame_width, frame_hieght));
      Imgproc.cvtColor(fg,fg, COLOR_BGRA2BGR);
      Imgproc.cvtColor(bg,bg, COLOR_BGRA2BGR);

      // Blur the mask
      Imgproc.resize(bg,bg, new Size(224,224));
      Imgproc.blur(bg,bg,new Size(11,11));
      Imgproc.resize(bg,bg, new Size(frame_width, frame_hieght));

      fg.convertTo(fg,CV_32FC3,1.0/255.0);
      bg.convertTo(bg,CV_32FC3,1.0/255.0);

      // Alpha blend fg with bg, using the mask
      Core.subtract(invmskmat,mskmat,invmskmat);
      Core.multiply(fg,mskmat,fg);
      Core.multiply(bg,invmskmat, bg);
      Core.add(fg,bg,resmat);

      resmat.convertTo(resmat,CV_8UC3,255);

      Utils.matToBitmap(resmat,resbmp);

    }
    return resbmp;
  }


    public void save (Bitmap bmp, String name){


        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;
        File file = new File(extStorageDirectory, name+k+".png");
        k++;
        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch(Exception e) {

        }

    }

}
