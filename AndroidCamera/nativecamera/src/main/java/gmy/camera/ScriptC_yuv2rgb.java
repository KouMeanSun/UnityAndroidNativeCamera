package gmy.camera;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptC;

/**
 * @author 高明阳 on 2021-06-15
 * @Description:
 * @Copyright © 2020 gaomingyang. All rights reserved.
 */
public class ScriptC_yuv2rgb extends ScriptC {
    private static final String __rs_resource_name = "yuv2rgb";
    private Element __ALLOCATION;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private static final int mExportVarIdx_gCurrentFrame = 0;
    private Allocation mExportVar_gCurrentFrame;
    private static final int mExportVarIdx_gIntFrame = 1;
    private Allocation mExportVar_gIntFrame;
    private static final int mExportForEachIdx_yuv2rgbFrames = 1;

    public ScriptC_yuv2rgb(RenderScript rs) {
        super(rs, "yuv2rgb", yuv2rgbBitCode.getBitCode32(), yuv2rgbBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(rs);
        this.__U8_4 = Element.U8_4(rs);
    }

    public synchronized void set_gCurrentFrame(Allocation v) {
        this.setVar(0, v);
        this.mExportVar_gCurrentFrame = v;
    }

    public Allocation get_gCurrentFrame() {
        return this.mExportVar_gCurrentFrame;
    }

    public FieldID getFieldID_gCurrentFrame() {
        return this.createFieldID(0, (Element)null);
    }

    public synchronized void set_gIntFrame(Allocation v) {
        this.setVar(1, v);
        this.mExportVar_gIntFrame = v;
    }

    public Allocation get_gIntFrame() {
        return this.mExportVar_gIntFrame;
    }

    public FieldID getFieldID_gIntFrame() {
        return this.createFieldID(1, (Element)null);
    }

    public KernelID getKernelID_yuv2rgbFrames() {
        return this.createKernelID(1, 58, (Element)null, (Element)null);
    }

    public void forEach_yuv2rgbFrames(Allocation aout) {
        this.forEach_yuv2rgbFrames(aout, (LaunchOptions)null);
    }

    public void forEach_yuv2rgbFrames(Allocation aout, LaunchOptions sc) {
        if (!aout.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else {
            this.forEach(1, (Allocation)null, aout, (FieldPacker)null, sc);
        }
    }
}
