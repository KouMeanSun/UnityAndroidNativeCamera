using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Rendering;

public class CameraBackground : MonoBehaviour
{
    public Shader m_shader;
    Texture m_texture;
    CommandBuffer m_CommandBuffer;
    Camera m_Camera;
    
    Material m_material;

    private void Awake()
    {
        m_Camera = GetComponent<Camera>();
        m_material = new Material(m_shader);
    }
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }


    public void setTexture(Texture texture)
    {
        m_texture = texture;
        if(m_CommandBuffer == null)
        {
            m_CommandBuffer = new CommandBuffer();
            configCommandBuffer();
        }
        m_material.SetTexture("_MainTex", texture);
    }

    private void configCommandBuffer()
    {
        Debug.Log("configCommandBuffer");
        m_CommandBuffer.ClearRenderTarget(true, true, Color.clear);
        m_CommandBuffer.Blit(m_texture, BuiltinRenderTextureType.CameraTarget, m_material);
        m_Camera.AddCommandBuffer(CameraEvent.BeforeForwardOpaque, m_CommandBuffer);
        m_Camera.AddCommandBuffer(CameraEvent.BeforeGBuffer, m_CommandBuffer);
    }




}
