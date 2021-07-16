using System;
using System.Collections;
using System.Runtime.InteropServices;
using UnityEngine;
using UnityEngine.UI;
public class updateCamera : MonoBehaviour
{

    [DllImport("NativeCameraPlugin")]
    private static extern void SetTextureFromUnity(IntPtr texture);

    [DllImport("NativeCameraPlugin")]
    private static extern IntPtr GetRenderEventFunc();

    public Material displayMaterial;
    private AndroidJavaObject _androidJavaPlugin = null;

    public RawImage rawImage;
    private WebCamTexture webCamTexture;
    private Vector2 resolution = new Vector2(1920, 1440);
   // private Vector2 resolution = new Vector2(3840, 2880);
    private int fps = 60;


    IEnumerator Start()
    {
        Debug.LogError("u3d c# Start");
        using (AndroidJavaClass javaClass = new AndroidJavaClass("gmy.camera.CameraPluginActivity"))
            {
                _androidJavaPlugin = javaClass.GetStatic<AndroidJavaObject>("_context");
            }

            CreateTextureAndPassToPlugin();
            yield return StartCoroutine("CallPluginAtEndOfFrames");
        
    }

    private void CreateTextureAndPassToPlugin()
    {
        //Debug.LogError("u3d c# CreateTextureAndPassToPlugin");
        Texture2D tex = new Texture2D(1280, 720, TextureFormat.RGBA32, false);
        // Set point filtering just so we can see the pixels clearly
        tex.filterMode = FilterMode.Point;
        // Call Apply() so it's actually uploaded to the GPU
        tex.Apply();

        //displayMaterial.mainTexture = tex;
        rawImage.texture = tex;

        SetTextureFromUnity(tex.GetNativeTexturePtr());

        EnablePreview(true);
    }

    //垂直翻转图片
    public static Texture2D VerticalFlipPic(Texture2D texture2d)
    {
        int width = texture2d.width;//得到图片的宽度.   
        int height = texture2d.height;//得到图片的高度 

        Texture2D NewTexture2d = new Texture2D(width, height);//创建一张同等大小的空白图片 

        int i = 0;

        while (i < height)
        {
            NewTexture2d.SetPixels(0, i, width, 1, texture2d.GetPixels(0, width - i - 1, width, 1));
            i++;
        }
        NewTexture2d.Apply();

        return NewTexture2d;
    }

    private IEnumerator CallPluginAtEndOfFrames()
    {
        while (true)
        {
            // Wait until all frame rendering is done
            yield return new WaitForEndOfFrame();

            // Issue a plugin event with arbitrary integer identifier.
            // The plugin can distinguish between different
            // things it needs to do based on this ID.
            // For our simple plugin, it does not matter which ID we pass here.
            GL.IssuePluginEvent(GetRenderEventFunc(), 1);
            //Debug.LogError("u3d c# CallPluginAtEndOfFrames");
            // skip one frame
            yield return new WaitForEndOfFrame();
        }
    }

    public void EnablePreview(bool enable)
    {
        if (_androidJavaPlugin != null)
        {
            _androidJavaPlugin.Call("enablePreviewUpdater", enable);
        }
    }

    //IEnumerator OpenBGCamera()
    //{
    //    Debug.Log("u3d c# Test");
    //    //获取摄像头权限
    //    yield return Application.RequestUserAuthorization(UserAuthorization.WebCam);
    //    if (Application.HasUserAuthorization(UserAuthorization.WebCam))
    //    {
    //        //停止正在使用的摄像头
    //        if (webCamTexture != null)
    //        {
    //            webCamTexture.Stop();
    //        }
    //        //判断时候有摄像头
    //        if (WebCamTexture.devices.Length != 0)
    //        {
    //            //new一个后置摄像头并且设置分辨率和FPS，渲染到UI上
    //            webCamTexture = new WebCamTexture(WebCamTexture.devices[0].name, (int)resolution.x, (int)resolution.y, fps);
    //            int num = WebCamTexture.devices[0].availableResolutions.Length;
    //            //   Debug.Log("resolution" + WebCamTexture.devices[0].kind);
    //            //for (int i = 0; i < num; i++)
    //            //{
    //            //    Debug.Log("resolution" + WebCamTexture.devices[0].availableResolutions[i].width + WebCamTexture.devices[0].availableResolutions[i].height);
    //            //}
    //            rawImage.texture = webCamTexture;
    //            webCamTexture.Play();
    //            //saveTexture();
    //        }
    //    }

    //}

}
