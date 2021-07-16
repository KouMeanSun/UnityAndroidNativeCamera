package gmy.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CaptureRequest.Builder;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Build.VERSION;
import android.renderscript.RenderScript;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.unity3d.player.UnityPlayerActivity;
import java.nio.IntBuffer;
import java.util.Arrays;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static android.opengl.GLES10.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;

/**
 * @author 高明阳 on 2021-06-15
 * @Description:
 * @Copyright © 2020 gaomingyang. All rights reserved.
 */
public class CameraPluginActivity extends UnityPlayerActivity {
    private static final String TAG = "CameraPluginActivity";
    private static final int REQUEST_PERMISSION = 233;
    public static CameraPluginActivity _context;
    private boolean _update;
    private static final int MAX_IMAGES = 4;
    private static final int CONVERSION_FRAME_RATE = 60;
    private Size _previewSize = new Size(1280, 720);
    private CameraDevice _cameraDevice;
    private CameraCaptureSession _captureSession;
    private ImageReader _imagePreviewReader;
    private RenderScript _renderScript;
    private YuvToRgb _conversionScript;
    private Surface _previewSurface;
    private HandlerThread _handlerThread;
    private final StateCallback _cameraStateCallback = new StateCallback() {
        public void onOpened(CameraDevice camera) {
            CameraPluginActivity.this._cameraDevice = camera;
            CameraPluginActivity.this.setupCameraDevice();
        }

        public void onDisconnected(CameraDevice camera) {
            Log.w("CameraPluginActivity", "CameraDevice.StateCallback onDisconnected");
        }

        public void onError(CameraDevice camera, int error) {
            Log.e("CameraPluginActivity", "CameraDevice.StateCallback onError[" + error + "]");
        }
    };
    private android.hardware.camera2.CameraCaptureSession.StateCallback _sessionStateCallback = new android.hardware.camera2.CameraCaptureSession.StateCallback() {
        public void onConfigured(CameraCaptureSession session) {
            CameraPluginActivity.this._captureSession = session;

            try {
                session.setRepeatingRequest(CameraPluginActivity.this.createCaptureRequest(), (CaptureCallback)null, (Handler)null);
            } catch (CameraAccessException var3) {
                var3.printStackTrace();
            }

        }

        public void onConfigureFailed(CameraCaptureSession session) {
            Log.e("CameraPluginActivity", "CameraCaptureSession.StateCallback onConfigureFailed");
        }
    };

    public CameraPluginActivity() {
    }

    public native void nativeInit();

    public native void nativeRelease();

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.commonInit();
    }

    private void commonInit(){
        if(checkPermission(Manifest.permission.CAMERA,REQUEST_PERMISSION)) {
            System.loadLibrary("NativeCameraPlugin");
            this.nativeInit();
            setContext(this);
            this._renderScript = RenderScript.create(this);

            this._handlerThread = new HandlerThread("CameraPluginActivity");
            this._handlerThread.start();
            this.startCamera();
        }
    }
    private static synchronized void setContext(CameraPluginActivity context) {
        _context = context;
    }
    private boolean checkPermission(String permission,int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    finish();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    permission,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            requestCode);
                }
                return false;
            }else return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   this.commonInit();
                }else {
                    finish();
                }
                break;
            default:
                finish();
                break;
        }
    }

    public void onResume() {
        super.onResume();

    }

    protected void onDestroy() {
        super.onDestroy();
        this.nativeRelease();
        setContext((CameraPluginActivity)null);
    }

    public void onPause() {
        this._handlerThread.quitSafely();

        try {
            this._handlerThread.join();
            this._handlerThread = null;
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        this.stopCamera();
        super.onPause();
    }

    private void requestJavaRendering(int texturePointer) {
        if (this._update) {

            int[] imageBuffer = new int[0];
            if (this._conversionScript != null) {
                imageBuffer = this._conversionScript.getOutputBuffer();
            }

            if (imageBuffer.length > 1) {
                GLES20.glBindTexture(GL_TEXTURE_2D, texturePointer);
                GLES20.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, this._previewSize.getWidth(), this._previewSize.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, IntBuffer.wrap(imageBuffer));
            }

        }
    }

    private void setupCameraDevice() {
        try {
            if (this._previewSurface != null) {
                this._cameraDevice.createCaptureSession(Arrays.asList(this._previewSurface), this._sessionStateCallback, (Handler)null);
            } else {
                Log.e(TAG, "u3d java failed creating preview surface");
            }
        } catch (CameraAccessException var2) {
            var2.printStackTrace();
        }

    }

    private CaptureRequest createCaptureRequest() {
        try {
            Builder builder = this._cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //把surface添加给CaptureRequest.Builder，使预览surface收到数据回调。
            builder.addTarget(this._previewSurface);
            return builder.build();
        } catch (CameraAccessException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    private void startCamera() {

        CameraManager manager = (CameraManager)this.getSystemService(CAMERA_SERVICE);

        try {
            if (VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }else {
            }
            if (VERSION.SDK_INT >= 22) {
                String pickedCamera = this.getCamera(manager);
                manager.openCamera(pickedCamera, _cameraStateCallback, null);
//                manager.openCamera(pickedCamera, this._cameraStateCallback, (Handler)null);
                int previewHeight = this._previewSize.getHeight();
                int previewWidth = this._previewSize.getWidth();
                Log.e(TAG,"u3d java previewWidth:"+previewWidth+",previewHeight:"+previewHeight);
                this._imagePreviewReader = ImageReader.newInstance(previewWidth, previewHeight, PixelFormat.RGBA_8888, MAX_IMAGES);
                this._conversionScript = new YuvToRgb(this._renderScript, this._previewSize, CONVERSION_FRAME_RATE);
                this._conversionScript.setOutputSurface(this._imagePreviewReader.getSurface());
                this._previewSurface = this._conversionScript.getInputSurface();
            }
        } catch (CameraAccessException var5) {
            var5.printStackTrace();
        } catch (SecurityException var6) {
            var6.printStackTrace();
        }

    }

    private void stopCamera() {
        try {
            this._captureSession.abortCaptures();
            this._captureSession.close();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        try {
            Image image = this._imagePreviewReader.acquireLatestImage();
            if (image != null) {
                image.close();
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        } finally {
            if (this._imagePreviewReader != null) {
                this._imagePreviewReader.close();
                this._imagePreviewReader = null;
            }

        }

        try {
            this._cameraDevice.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        this._conversionScript = null;
    }

    private String getCamera(CameraManager manager) {
        try {
            String[] var2 = manager.getCameraIdList();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String cameraId = var2[var4];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cameraOrientation = (Integer)characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == 1) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException var8) {
            var8.printStackTrace();
        }

        return null;
    }

    @UsedThroughReflection
    public void enablePreviewUpdater(boolean update) {
        this._update = update;
    }
}
