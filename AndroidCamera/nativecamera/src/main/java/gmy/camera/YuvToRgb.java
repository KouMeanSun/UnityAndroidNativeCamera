package gmy.camera;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;
import android.view.Surface;

/**
 * @author 高明阳 on 2021-06-15
 * @Description:
 * @Copyright © 2020 gaomingyang. All rights reserved.
 */
public class YuvToRgb implements Allocation.OnBufferAvailableListener {
    private Allocation mInputAllocation;
    private Allocation mOutputAllocation;
    private Allocation mOutputAllocationInt;
    private Size mInputSize;
    private ScriptC_yuv2rgb mScriptYuv2Rgb;
    private int[] mOutBufferInt;
    private long mLastProcessed;
    private final int mFrameEveryMs;

    YuvToRgb(RenderScript rs, Size dimensions, int frameMs) {
        this.mInputSize = dimensions;
        this.mFrameEveryMs = frameMs;
        this.createAllocations(rs);
        this.mInputAllocation.setOnBufferAvailableListener(this);
        this.mScriptYuv2Rgb = new ScriptC_yuv2rgb(rs);
        this.mScriptYuv2Rgb.set_gCurrentFrame(this.mInputAllocation);
        this.mScriptYuv2Rgb.set_gIntFrame(this.mOutputAllocationInt);
    }

    private void createAllocations(RenderScript rs) {
        int width = this.mInputSize.getWidth();
        int height = this.mInputSize.getHeight();
        this.mOutBufferInt = new int[width * height];
        Type.Builder yuvTypeBuilder = new Type.Builder(rs, Element.YUV(rs));
        yuvTypeBuilder.setX(width);
        yuvTypeBuilder.setY(height);
        yuvTypeBuilder.setYuvFormat(35);
        this.mInputAllocation = Allocation.createTyped(rs, yuvTypeBuilder.create(), 33);
        Type rgbType = Type.createXY(rs, Element.RGBA_8888(rs), width, height);
        Type intType = Type.createXY(rs, Element.U32(rs), width, height);
        this.mOutputAllocation = Allocation.createTyped(rs, rgbType, 65);
        this.mOutputAllocationInt = Allocation.createTyped(rs, intType, 1);
    }

    Surface getInputSurface() {
        return this.mInputAllocation.getSurface();
    }

    void setOutputSurface(Surface output) {
        this.mOutputAllocation.setSurface(output);
    }

    public void onBufferAvailable(Allocation a) {
        this.mInputAllocation.ioReceive();
        long current = System.currentTimeMillis();
        if (current - this.mLastProcessed >= (long)this.mFrameEveryMs) {
            this.mScriptYuv2Rgb.forEach_yuv2rgbFrames(this.mOutputAllocation);
            this.mOutputAllocationInt.copyTo(this.mOutBufferInt);
            this.mLastProcessed = current;
        }

    }

    public int[] getOutputBuffer() {
        return this.mOutBufferInt;
    }
}
