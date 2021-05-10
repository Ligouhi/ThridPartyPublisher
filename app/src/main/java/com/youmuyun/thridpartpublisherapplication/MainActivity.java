package com.youmuyun.thridpartpublisherapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import segmentor.ImageSegmentor;
import segmentor.ImageSegmentorFloatMobileUnet;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.seu.magicfilter.utils.MagicFilterType;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


    public  class MainActivity extends AppCompatActivity implements RtmpHandler.RtmpListener,
            SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {

        private static final String TAG = "Yasea";


        public final static int RC_CAMERA = 100;

        private Button btnPublish;
        private Button btnSwitchCamera;
        private Button btnShowFilter;
    //    private Button btnRecord;
    //    private Button btnSwitchEncoder;
    //    private Button btnPause;

        private SharedPreferences sp;
        private String rtmpUrl = null;
        private String rtmpUrl2 = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_11777756_9554883&key=facfb96d86434ef87543f8f22f5aaa25&schedule=rtmp";
        private String recPath = Environment.getExternalStorageDirectory().getPath() + "/test.mp4";

        private SrsPublisher mPublisher;

        private SrsCameraView mCameraView;

        private int mWidth = 1280;
        private int mHeight = 720;
        private boolean isPermissionGranted = false;

        private ImageView btnHideFilter;
        private LinearLayout mFilterLayout;
        private RecyclerView mFilterRecyclerView;
        private FilterAdapter filterAdapter;
        private final MagicFilterType[] types = new MagicFilterType[]{
                MagicFilterType.NONE,
                MagicFilterType.FAIRYTALE,
                MagicFilterType.SUNRISE,
                MagicFilterType.SUNSET,
                MagicFilterType.WHITECAT,
                MagicFilterType.BLACKCAT,
                MagicFilterType.SKINWHITEN,
                MagicFilterType.HEALTHY,
                MagicFilterType.SWEETS,
                MagicFilterType.ROMANCE,
                MagicFilterType.SAKURA,
                MagicFilterType.WARM,
                MagicFilterType.ANTIQUE,
                MagicFilterType.NOSTALGIA,
                MagicFilterType.CALM,
                MagicFilterType.LATTE,
                MagicFilterType.TENDER,
                MagicFilterType.COOL,
                MagicFilterType.EMERALD,
                MagicFilterType.EVERGREEN,
                MagicFilterType.CRAYON,
                MagicFilterType.SKETCH,
                MagicFilterType.AMARO,
                MagicFilterType.BRANNAN,
                MagicFilterType.BROOKLYN,
                MagicFilterType.EARLYBIRD,
                MagicFilterType.FREUD,
                MagicFilterType.HEFE,
                MagicFilterType.HUDSON,
                MagicFilterType.INKWELL,
                MagicFilterType.KEVIN,
                MagicFilterType.LOMO,
                MagicFilterType.N1977,
                MagicFilterType.NASHVILLE,
                MagicFilterType.PIXAR,
                MagicFilterType.RISE,
                MagicFilterType.SIERRA,
                MagicFilterType.SUTRO,
                MagicFilterType.TOASTER2,
                MagicFilterType.VALENCIA,
                MagicFilterType.WALDEN,
                MagicFilterType.XPROII
        };

        private ImageSegmentor segmentor;
        private ButtonViewGroup bgroup;
        private SegmentAdapter segAdapter;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.activity_main);

            // response screen rotation event
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

            requestPermission();


        }

        private void setsegmentor() {
            if (!OpenCVLoader.initDebug())
                Log.e("OpenCv", "Unable to load OpenCV");
            else
                Log.d("OpenCv", "OpenCV loaded");
            try {
                Log.i(TAG,"segmentor start set");
                if(segmentor == null)
                segmentor = new ImageSegmentorFloatMobileUnet(this);
                mPublisher.setSegmentor(segmentor);
                Log.i(TAG,"segmentor set complete");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG,e.toString());
            }


        }
        private void removesegmentor(){
            mPublisher.removeSegmentor();
        }

        private void requestPermission() {
            //1. 检查是否已经有该权限
            if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)) {
                //2. 权限没有开启，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_CAMERA);
            } else {
                //权限已经开启，做相应事情
                isPermissionGranted = true;
                //初始化
                init();
            }
        }

        //3. 接收申请成功或者失败回调
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == RC_CAMERA) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //权限被用户同意,做相应的事情
                    isPermissionGranted = true;
                    init();
                } else {
                    //权限被用户拒绝，做相应的事情
                    finish();
                }
            }
        }

        private void init() {

            final Intent liveconfig = getIntent();
            //设置横竖屏
            int pos = liveconfig.getIntExtra("OrienSpinner",-1);

            if(pos==1){
                MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            else if(pos==0){
                MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }


            btnPublish = (Button) findViewById(R.id.publish);
            btnSwitchCamera = (Button) findViewById(R.id.swCam);
            btnShowFilter = (Button) findViewById(R.id.btn_show_filter);//        btnRecord = (Button) findViewById(R.id.record);
    //        btnSwitchEncoder = (Button) findViewById(R.id.swEnc);
    //        btnPause = (Button) findViewById(R.id.pause);
    //        btnPause.setEnabled(false);
            mCameraView = (SrsCameraView) findViewById(R.id.glsurfaceview_camera);
            btnHideFilter = (ImageView)findViewById((R.id.btn_hide_filter));
            mFilterLayout = (LinearLayout)findViewById(R.id.filter_layout);
            mFilterRecyclerView = (RecyclerView) findViewById(R.id.filter_recyclerView);
            bgroup = (ButtonViewGroup)findViewById(R.id.group_mgc);

            //分辨率选项
            final ArrayList<String> viewtexts = new ArrayList<>();
            viewtexts.add("滤镜");
            viewtexts.add("背景替换");
            bgroup.setItemBGResPre(R.drawable.filter_item_selected);
            bgroup.setItemBGResNor(R.drawable.filer_item_nomal);
            bgroup.addItemViews(viewtexts, ButtonViewGroup.TEV_MODE);
            bgroup.chooseItemStyle(0);


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mFilterRecyclerView.setLayoutManager(linearLayoutManager);

            mPublisher = new SrsPublisher(mCameraView);
            mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
            mPublisher.setRtmpHandler(new RtmpHandler(this));
            mPublisher.setRecordHandler(new SrsRecordHandler(this));
            mPublisher.setPreviewResolution(mWidth, mHeight);
            mPublisher.setOutputResolution(mHeight, mWidth); // 这里要和preview反过来
            mPublisher.setVideoHDMode();
            mPublisher.startCamera();

            filterAdapter = new FilterAdapter(this, types);
            filterAdapter.setOnFilterChangeListener(new FilterAdapter.onFilterChangeListener(){
                @Override
                public void onFilterChanged(MagicFilterType filterType) {
                    mPublisher.switchCameraFilter(filterType);
                }
            });
            mFilterRecyclerView.setAdapter(filterAdapter);

            List<SegmentData> segmentDataList = new ArrayList<>();
            segmentDataList.add(new SegmentData("无",R.drawable.bg_none));
            segmentDataList.add(new SegmentData("落日",R.drawable.bg_sunset));
            segAdapter = new SegmentAdapter(this,segmentDataList);
            segAdapter.setOnItemClickListener(new SegmentAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String name) {
                    switch (name){
                        case "无":
                            removesegmentor();
                            break;
                        case "落日":
                            setsegmentor();
                            break;
                    }
                }
            });


            bgroup.setGroupClickListener(new ButtonViewGroup.OnGroupItemClickListener() {
                @Override
                public void onGroupItemClick(int item) {
                    switch (item){
                        case 0:
                            mFilterRecyclerView.setAdapter(filterAdapter);
                            break;
                        case 1:

                            mFilterRecyclerView.setAdapter(segAdapter);
                            break;

                    }
                }
            });

            btnShowFilter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mFilterLayout.setVisibility(View.VISIBLE);
                }
            });

            btnHideFilter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mFilterLayout.setVisibility(View.INVISIBLE);
                }
            });

            mCameraView.setCameraCallbacksHandler(new SrsCameraView.CameraCallbacksHandler() {
                @Override
                public void onCameraParameters(Camera.Parameters params) {
                    //params.setFocusMode("custom-focus");
                    //params.setWhiteBalance("custom-balance");
                    //etc...
                }
            });

            btnPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnPublish.getText().toString().contentEquals("开始直播")) {
                        rtmpUrl = liveconfig.getStringExtra("URL");

                        Log.d("pushrt", "url:" + rtmpUrl);

                        int resolusion = liveconfig.getIntExtra("resolusion",-1);
                        switch (resolusion){
                            case 0:
                                mPublisher.setOutputResolution(360,640);
                                break;
                            case 1:
                                mPublisher.setOutputResolution(720,1280);
                                break;
                            case 2:
                                mPublisher.setOutputResolution(1080,1920);
                                break;
                        }
                        int bitsrate = liveconfig.getIntExtra("bitsrate",-1);
                        switch (bitsrate){
                            case 0:
                                mPublisher.setVideoSmoothMode();
                                break;
                            case 1:
                                mPublisher.setVideoHDMode();
                                break;
                        }
                        mPublisher.startPublish(rtmpUrl);
                        btnPublish.setText("停止直播");

                    } else if (btnPublish.getText().toString().contentEquals("停止直播")) {
//                        mPublisher.pausePublish();
                        mPublisher.stopRecord();
                        btnPublish.setText("开始直播");
    //                    btnRecord.setText("record");
    //                    btnSwitchEncoder.setEnabled(true);
    //                    btnPause.setEnabled(false);
                    }
                }
            });


            btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPublisher.switchCameraFace((mPublisher.getCameraId() + 1) % Camera.getNumberOfCameras());

                }
            });

        }



        @Override
        protected void onStart() {
            super.onStart();
            if ( isPermissionGranted && mPublisher.getCamera() == null) {
                //if the camera was busy and available again
                mPublisher.startCamera();
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            final Button btn = (Button) findViewById(R.id.publish);
            btn.setEnabled(true);
            mPublisher.resumeRecord();
        }

        @Override
        protected void onPause() {
            super.onPause();
            mPublisher.pauseRecord();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mPublisher.stopPublish();
            mPublisher.stopRecord();
            mPublisher.stopCamera();
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            mPublisher.stopEncode();
            mPublisher.stopRecord();
            mPublisher.setScreenOrientation(newConfig.orientation);
            if (btnPublish.getText().toString().contentEquals("停止直播")) {
                mPublisher.startEncode();
            }
            mPublisher.startCamera();
        }

        private static String getRandomAlphaString(int length) {
            String base = "abcdefghijklmnopqrstuvwxyz";
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int number = random.nextInt(base.length());
                sb.append(base.charAt(number));
            }
            return sb.toString();
        }

        private static String getRandomAlphaDigitString(int length) {
            String base = "abcdefghijklmnopqrstuvwxyz0123456789";
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int number = random.nextInt(base.length());
                sb.append(base.charAt(number));
            }
            return sb.toString();
        }

        private void handleException(Exception e) {
            try {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                mPublisher.stopPublish();
                mPublisher.stopRecord();
                btnPublish.setText("开始直播");
    //            btnRecord.setText("record");
    //            btnSwitchEncoder.setEnabled(true);
            } catch (Exception e1) {
                //
            }
        }

        // Implementation of SrsRtmpListener.

        @Override
        public void onRtmpConnecting(String msg) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRtmpConnected(String msg) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRtmpVideoStreaming() {
        }

        @Override
        public void onRtmpAudioStreaming() {
        }

        @Override
        public void onRtmpStopped() {
            Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRtmpDisconnected() {
            Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRtmpVideoFpsChanged(double fps) {
            Log.i(TAG, String.format("Output Fps: %f", fps));
        }

        @Override
        public void onRtmpVideoBitrateChanged(double bitrate) {
            int rate = (int) bitrate;
            if (rate / 1000 > 0) {
                Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
            } else {
                Log.i(TAG, String.format("Video bitrate: %d bps", rate));
            }
        }

        @Override
        public void onRtmpAudioBitrateChanged(double bitrate) {
            int rate = (int) bitrate;
            if (rate / 1000 > 0) {
                Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
            } else {
                Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
            }
        }

        @Override
        public void onRtmpSocketException(SocketException e) {
            handleException(e);
        }

        @Override
        public void onRtmpIOException(IOException e) {
            handleException(e);
        }

        @Override
        public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
            handleException(e);
        }

        @Override
        public void onRtmpIllegalStateException(IllegalStateException e) {
            handleException(e);
        }

        // Implementation of SrsRecordHandler.

        @Override
        public void onRecordPause() {
            Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordResume() {
            Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordStarted(String msg) {
            Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordFinished(String msg) {
            Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordIOException(IOException e) {
            handleException(e);
        }

        @Override
        public void onRecordIllegalArgumentException(IllegalArgumentException e) {
            handleException(e);
        }

        // Implementation of SrsEncodeHandler.

        @Override
        public void onNetworkWeak() {
            Toast.makeText(getApplicationContext(), "Network weak", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNetworkResume() {
            Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
            handleException(e);
        }

    }

