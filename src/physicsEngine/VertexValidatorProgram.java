package physicsEngine;

import entities.Entity;
import models.RawModel;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opencl.CL10.CL_MEM_COPY_HOST_PTR;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;

public class VertexValidatorProgram  extends KernelVBOProgram {

    private FloatBuffer out;

    VertexValidatorProgram (String file) {
        super(file);

        //setup uniforms
        uniformLoader.initUniform(null, 1); //E max radius
        uniformLoader.initUniform(null, 3); //E pos vector

    }

    public void run(Entity e, FloatBuffer vertices) {
        float rad = e.getModel().getRawModel().getRadius();
        float scale = e.getScale();
        uniformLoader.loadUniform(0, rad*rad*scale*scale * 1.1f);
        uniformLoader.loadUniform(1, e.getPosition());
        enqueueUniforms();
        loadMemory(1, vertices);
        out = BufferUtils.createFloatBuffer(vertices.capacity());
        loadMemory(2,out, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR);
        executeKernel(vertices.capacity());
    }

    public FloatBuffer getOutput () {
        getBuffer(2,out);
        return out;
    }
}
