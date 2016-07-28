package com.google.android.gms.samples.vision.face.googlyeyes;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.FaceDetector;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by Chin on 16/7/25.
 */
public class CustomFaceDetector extends Detector<Face> {

    private Detector<Face> mDelegate;
    private Bitmap bitmap;
    private int num = 0;
    private File appDir;
    public CustomFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    public SparseArray<Face> detect(Frame frame) {
        ByteBuffer byteBuffer = frame.getGrayscaleImageData();
        byte[] bytes = byteBuffer.array();
        int w = frame.getMetadata().getWidth();
        int h = frame.getMetadata().getHeight();
        // Byte array to Bitmap here

        YuvImage yuvimage=new YuvImage(bytes, ImageFormat.NV21, w, h, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, w, h), 100, baos); // Where 100 is the quality of the generated jpeg
        byte[] jpegArray = baos.toByteArray();
        bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
        int a = bitmap.getWidth();
        int b = bitmap.getHeight();
        int pixel = bitmap.getPixel(100, 100);
        String s1 = Integer.toString(a);
        String s2 = Integer.toString(b);


        //Log.d("red", Integer.toString(redValue));
        //Log.d("green", Integer.toString(greenValue));
        //Log.d("blue", Integer.toString(blueValue));
        appDir = new File(Environment.getExternalStorageDirectory()+File.separator+"EyeTracker");

        if(!appDir.exists() && !appDir.isDirectory()) {
            if (appDir.mkdirs()) { Log.i("CreateDir","App dir created"); }
            else { Log.w("CreateDir","Unable to create app dir!"); }
        }

        return mDelegate.detect(frame);
    }

    public void getMyEye(PointF rightEye, PointF leftEye) {
        Float scale = new Float(3.2);
        int rx = Math.round(rightEye.x/scale);
        int ry = Math.round(rightEye.y/scale);
        int lx = Math.round(leftEye.x/scale);
        int ly = Math.round(leftEye.y/scale);

        int w = 60;
        int h = 30;

        Bitmap cropRightEye = Bitmap.createBitmap(bitmap, 640 - rx - 22, ry - 25, w, h);
        Bitmap cropLeftEye = Bitmap.createBitmap(bitmap, 640 - lx - 22, ly - 25, w, h);
        Bitmap GrayRightEye = Bitmap.createBitmap(cropRightEye, 0, 0, w, h);
        Bitmap GrayLeftEye = Bitmap.createBitmap(cropLeftEye, 0, 0, w, h);


        String[] Rightdata = new String[1800];
        String[] Leftdata = new String[1800];
        File mRGray = new File(appDir, num + "RightGray.txt");
        File mLGray = new File(appDir, num + "LeftGray.txt");
        int cnt = 0;
        for (int y = 0; y < cropRightEye.getHeight(); y++) {
            for (int x = 0; x < cropRightEye.getWidth(); x++ ) {
                int pixel = cropRightEye.getPixel(x, y);
                //int A = Color.alpha(pixel);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                int gray = (int)(0.299 * R + 0.587 * G + 0.114 * B);
                Rightdata[cnt] = Integer.toString(gray) + " ";
                int pixel2 = cropLeftEye.getPixel(x, y);
                int R2 = Color.red(pixel2);
                int G2 = Color.green(pixel2);
                int B2 = Color.blue(pixel2);
                int gray2 = (int)(0.299 * R2 + 0.587 * G2 + 0.114 * B2);
                Leftdata[cnt] = Integer.toString(gray2) + " ";
                cnt ++;
                //GrayRightEye.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }

        try {
            FileOutputStream stream = new FileOutputStream(mRGray);
            for (String s : Rightdata) {
                stream.write(s.getBytes());
            }
            stream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        try {
            FileOutputStream stream = new FileOutputStream(mLGray);
            for (String s : Leftdata) {
                stream.write(s.getBytes());
            }
            stream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        String fname = num + "Right-" + ".jpg";
        File file = new File(appDir, fname);
        String fname2 = num + "Left-"  + ".jpg";
        File file2 = new File(appDir, fname2);
        /*String fname3 = num + "Original-"  + ".jpg";
        File file3 = new File(appDir, fname3);*/

        /*int width, height;
        height = cropRightEye.getHeight();
        width = cropLeftEye.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(cropRightEye, 0, 0, paint);*/

        try {
            FileOutputStream out = new FileOutputStream(file);
            FileOutputStream out2 = new FileOutputStream(file2);
            //FileOutputStream out3 = new FileOutputStream(file3);
            cropRightEye.compress(Bitmap.CompressFormat.JPEG, 100, out);
            GrayRightEye.compress(Bitmap.CompressFormat.JPEG, 100, out2);
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out3);

            out.flush();
            out.close();
            out2.flush();
            out2.close();
            //out3.flush();
            //out3.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        num ++;

        return;
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}
