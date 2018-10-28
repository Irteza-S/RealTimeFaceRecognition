package com.example.irteza.facerecognition;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.irteza.facerecognition.Helper.DotOverlay;
import com.example.irteza.facerecognition.Helper.GraphicOverlay;
import com.example.irteza.facerecognition.Helper.RectOverlay;
import com.example.irteza.facerecognition.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Size;


import java.util.List;


import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity {


    CameraView cameraView;
    GraphicOverlay graphicOverlay;
    Button btnDetect;
    FirebaseVisionPoint object;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;


    android.app.AlertDialog waitingDialog;

    @Override
    protected void onResume()
    {
        super.onResume();
        cameraView.start();

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        cameraView.stop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (CameraView)findViewById(R.id.camera_view);
        btnDetect = (Button)findViewById(R.id.btn_detect);
        graphicOverlay = (GraphicOverlay) findViewById(R.id.graphic_overlay);
        waitingDialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("Please wait")
                .setCancelable(false)
                .build();

        cameraView.addFrameProcessor(new FrameProcessor() {
                                         @Override
                                         @WorkerThread
                                         public void process(Frame frame)
                                         {
                                                graphicOverlay.clear();
                                                 byte[] data = frame.getData();
                                                 int rotation = frame.getRotation();
                                                 long time = frame.getTime();
                                                 Size size = frame.getSize();
                                                 int format = frame.getFormat();

                                                 FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                                                         .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                                                         .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                                                         .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                                                         .build();

                                                 FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                                                         .getVisionFaceDetector(options);

                                                 int frameRotation = rotation / 90;

                                                 FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                                                         .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                                                         .setWidth(size.getWidth())
                                                         .setRotation(frameRotation)
                                                         .setHeight(size.getHeight())
                                                         .build();

                                                 FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromByteArray(data, metadata);
                                                 Bitmap bitmapdebug = firebaseVisionImage.getBitmapForDebugging();
                                                 detector.detectInImage(firebaseVisionImage)
                                                         .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>()
                                                         {
                                                             @Override
                                                             public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces)
                                                             {
                                                                 if(firebaseVisionFaces.size()!=0)
                                                                 {
                                                                     //Toast.makeText(MainActivity.this, "Face detected", Toast.LENGTH_SHORT).show();
                                                                     FirebaseVisionFaceContour contour = firebaseVisionFaces.get(0).getContour(FirebaseVisionFaceContour.ALL_POINTS);
                                                                     DotOverlay rect = new DotOverlay(graphicOverlay, contour.getPoints());
                                                                     graphicOverlay.add(rect);
                                                                 }
                                                             }
                                                         })
                                                         .addOnFailureListener(new OnFailureListener()
                                                         {
                                                             @Override
                                                             public void onFailure(@NonNull Exception e)
                                                             {
                                                                 Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                             }
                                                         });
                                     }});

        /*
        btnDetect.setOnClickListener((view) -> {
            cameraView.start();
            cameraView.capturePicture();
           /* cameraView.addFrameProcessor {
                faceDetector.process(Frame(
                        data = it.data,
                        rotation = it.rotation,
                        size = Size(it.size.width, it.size.height),
                        format = it.format))
            }}
        );*/

        /*
        cameraView.addCameraListener(new CameraListener() {


            /**
             * Notifies about an error during the camera setup or configuration.
             * At the moment, errors that are passed here are unrecoverable. When this is called,
             * the camera has been released and is presumably showing a black preview.
             *
             * This is the right moment to show an error dialog to the user.

            @Override
            public void onCameraError(CameraException error) {}

            /**
             * Notifies that a picture previously captured with capturePicture()
             * or captureSnapshot() is ready to be shown or saved.
             *
             * If planning to get a bitmap, you can use CameraUtils.decodeBitmap()
             * to decode the byte array taking care about orientation.

            @Override
            public void onPictureTaken(byte[] picture)
            {
                bitmap = BitmapFactory.decodeByteArray(picture , 0, picture.length);
                waitingDialog.show();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();
                runFaceDetector(bitmap);
            }

        });
        */
    }
/*
    private void runFaceDetector(Bitmap bitmap)
    {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>()
            {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces)
            {
                //System.out.println("?????>>>>>>>>>>>>>>>>?????????");
                //object = firebaseVisionFaces.get(0).getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE).getPosition();
                //System.out.println("X :"+object.getX() +" Y :" +object.getY() +" Z :" +object.getZ());
                //System.out.println(contour.getPoints());
                //System.out.println("?????>>>>>>>>>>>>>>>>?????????");
                FirebaseVisionFaceContour contour = firebaseVisionFaces.get(0).getContour(FirebaseVisionFaceContour.ALL_POINTS);
                waitingDialog.dismiss();

                //Drawing points
                canvas = new Canvas(bitmap);
                paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.YELLOW);
                for(int i=0; i<contour.getPoints().size(); i++)
                {
                    canvas.drawCircle(contour.getPoints().get(i).getX(), contour.getPoints().get(i).getY(), 2, paint);
                }
                imageView.setImageBitmap(bitmap);

            }
            })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

*/
    /*
    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces)
    {
        int count =0;
        for(FirebaseVisionFace face : firebaseVisionFaces)
        {
            Rect bounds = face.getBoundingBox();

            count++;
            //Draw rectangle
            RectOverlay rect = new RectOverlay(graphicOverlay, bounds);
            graphicOverlay.add(rect);

        }

        waitingDialog.dismiss();
        Toast.makeText(this, String.format("Detected %d faces in image", count), Toast.LENGTH_SHORT).show();

    }*/

}
