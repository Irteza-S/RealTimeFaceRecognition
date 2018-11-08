package com.example.irteza.facerecognition;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Size;


import java.io.ByteArrayOutputStream;
import java.util.List;


import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    GraphicOverlay graphicOverlay;
    Button swapCam;
    static Boolean isFacingFront;
    FirebaseVisionFaceDetectorOptions options;
    FirebaseVisionFaceDetector detector;

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
        isFacingFront = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (CameraView)findViewById(R.id.camera_view);
        graphicOverlay = (GraphicOverlay) findViewById(R.id.graphic_overlay);
        waitingDialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("Please wait")
                .setCancelable(false)
                .build();

        cameraView.setFacing(Facing.FRONT);
        swapCam = (Button) findViewById(R.id.btn_detect);
        swapCam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isFacingFront==true)
                {
                    cameraView.setFacing(Facing.BACK);
                    isFacingFront=false;
                }
                else
                {
                    cameraView.setFacing(Facing.FRONT);
                    isFacingFront=true;
                }
            }
        });


        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build();

        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

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
    }
}
