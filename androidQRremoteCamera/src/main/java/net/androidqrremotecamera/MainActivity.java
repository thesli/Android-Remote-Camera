package net.androidqrremotecamera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import net.androidqrremotecamera.asyncTasks.AsyncWriteStuff;

import java.io.File;
import java.util.List;


public class MainActivity extends Activity implements Camera.PictureCallback {
    Button poll;
    Intent i;
    String DEBUG_TAG = "MainActivityDebug";
    private Camera mCam;
    private MirrorView mCamPreview;
    private int mCameraId = 0;
    private FrameLayout mPreviewLayout;
    private Messenger msger;
    private Handler h;
    private Handler f;
    File pictureFile;
    Button takePic;
    Button restart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void restartCam(){
        mCam.setPreviewCallback(null);
        mCam.stopPreview();
        mCam.release();
        mCam = null;
        startCam();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCam == null && mPreviewLayout != null) {
            mPreviewLayout.removeAllViews();
            startCameraInLayout(mPreviewLayout, mCameraId);
        }
    }

    @Override
    protected void onPause() {
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }
        i = new Intent(MainActivity.this, IOServices.class);
        stopService(i);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void startCameraInLayout(FrameLayout layout, int cameraId) {

        // TODO pull this out of the UI thread.
        mCam = Camera.open(cameraId);
        Camera.Parameters para = mCam.getParameters();
        para.setJpegQuality(100);
        para.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        para.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        List<Camera.Size> sizes = para.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i).width >= size.width)
                size = sizes.get(i);
        }
        para.setPictureSize(size.width, size.height);
        mCam.setParameters(para);
        if (mCam != null) {
            mCamPreview = new MirrorView(this, mCam);
            layout.addView(mCamPreview);
        }
    }

    public void init() {
        poll = (Button) findViewById(R.id.poll);
        restart = (Button) findViewById(R.id.restartbtn);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartCam();
            }
        });
        h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
//                Toast.makeText(getApplicationContext(),msg.what,Toast.LENGTH_SHORT).show();
                if(msg.what == R.string.trythis){
                    takePic.performClick();
                }
            }
        };
        msger = new Messenger(h);
        i = new Intent(MainActivity.this, IOServices.class);
        i.putExtra("messenger",msger);
        startService(i);
        mPreviewLayout = (FrameLayout) findViewById(R.id.camframe);
        startCam();
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        File pictureFileDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SimpleSelfCam");

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(DEBUG_TAG, "Can't create directory to save image");
            Toast.makeText(this, "Can't make path to save pic.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        String filename = pictureFileDir.getPath() + File.separator
                + "latest_mug.jpg";

        pictureFile = new File(filename);

        f = new Handler(getApplicationContext().getMainLooper());
        f.post(new Runnable() {
            @Override
            public void run() {
                AsyncWriteStuff w = new AsyncWriteStuff(pictureFile,data,getApplicationContext());
                w.execute();
            }
        });
    }

    public void startCam(){

        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera feature on this device",
                    Toast.LENGTH_LONG).show();
        } else {

            mCameraId = findRearCamera();

            if (mCameraId >= 0) {
                mPreviewLayout = (FrameLayout) findViewById(R.id.camframe);
                mPreviewLayout.removeAllViews();

                startCameraInLayout(mPreviewLayout, mCameraId);

                takePic = (Button) findViewById(R.id.snap);
                takePic.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        mCam.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                mCam.takePicture(null, null, MainActivity.this);
                            }
                        });


                    }
                });
            } else {
                Toast.makeText(this, "No rear camera found.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private int findRearCamera() {
        int foundId = -1;
        // find the first front facing camera
        int numCams = Camera.getNumberOfCameras();
        for (int camId = 0; camId < numCams; camId++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(camId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(DEBUG_TAG, "Found front facing camera");
                foundId = camId;
                break;
            }
        }
        return foundId;
    }

    public class MirrorView extends SurfaceView implements
            SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public MirrorView(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception error) {
                Log.d(DEBUG_TAG,
                        "Error starting mPreviewLayout: " + error.getMessage());
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            if (mHolder.getSurface() == null) {
                return;
            }

            // can't make changes while mPreviewLayout is active
            try {
                mCamera.stopPreview();
            } catch (Exception e) {

            }

            try {
                // set rotation to match device orientation
                setCameraDisplayOrientationAndSize();

                // start up the mPreviewLayout
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception error) {
                Log.d(DEBUG_TAG,
                        "Error starting mPreviewLayout: " + error.getMessage());
            }
        }

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        public void setCameraDisplayOrientationAndSize() {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = rotation * 90;

            /*
             * // the above is just a shorter way of doing this, but could break
             * if the values change switch (rotation) { case Surface.ROTATION_0:
             * degrees = 0; break; case Surface.ROTATION_90: degrees = 90;
             * break; case Surface.ROTATION_180: degrees = 180; break; case
             * Surface.ROTATION_270: degrees = 270; break; }
             */

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }
            mCamera.setDisplayOrientation(result);

            Camera.Size previewSize = mCam.getParameters().getPreviewSize();
            if (result == 90 || result == 270) {
                // swap - the physical camera itself doesn't rotate in relation
                // to the screen ;)
                mHolder.setFixedSize(previewSize.height, previewSize.width);
            } else {
                mHolder.setFixedSize(previewSize.width, previewSize.height);

            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }


    }

}
