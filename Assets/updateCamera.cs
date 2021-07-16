using System;
using System.Collections;
using System.Runtime.InteropServices;
using UnityEngine;
public class updateCamera : MonoBehaviour
{

    [DllImport("NativeCameraPlugin")]
    private static extern void SetTextureFromUnity(IntPtr texture);

    [DllImport("NativeCameraPlugin")]
    private static extern IntPtr GetRenderEventFunc();

    private AndroidJavaObject _androidJavaPlugin = null;

    public CameraBackground m_CameraBackground;

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
   
        Texture2D tex = new Texture2D(1280, 720, TextureFormat.RGBA32, false);
        tex.filterMode = FilterMode.Point;

        tex.Apply();

        SetTextureFromUnity(tex.GetNativeTexturePtr());
        m_CameraBackground.setTexture(tex);
        EnablePreview(true);
    }

   

    private IEnumerator CallPluginAtEndOfFrames()
    {
        while (true)
        {
 
            yield return new WaitForEndOfFrame();
            GL.IssuePluginEvent(GetRenderEventFunc(), 1);
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

}
