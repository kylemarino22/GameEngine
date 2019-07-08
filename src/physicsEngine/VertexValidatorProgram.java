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

    VertexValidatorProgram (String file, FloatBuffer transformedVertices) {
        super(file);

        //kernel memory
        createCLMem(1);
        createFromMemoryBuffer(transformedVertices);
        createOutCLMem();
        loadMemory(2);

        //setup uniforms
        uniformLoader.initUniform(null, 1); //E max radius
        uniformLoader.initUniform(null, 3); //E pos vector

    }

    public void run(Entity e, int size) {
        float rad = e.getModel().getRawModel().getRadius();
        float scale = e.getScale();

        //1.01 times to encapsulate edge vertices
        uniformLoader.loadUniform(0, rad*rad*scale*scale * 1.01f);
        uniformLoader.loadUniform(1, e.getPosition());
        enqueueUniforms();
        loadMemory(1);

        executeKernel(size);
    }

    public FloatBuffer getOutput () {
        getBuffer(2);
        return out;
    }
}
