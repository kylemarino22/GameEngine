package physicsEngine;

import entities.PhysicsEntity;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import toolbox.Maths;

import java.nio.FloatBuffer;

import static org.lwjgl.opencl.CL10.CL_MEM_COPY_HOST_PTR;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;

public class VectorTransformProgram extends KernelVBOProgram{

    private FloatBuffer out;

    VectorTransformProgram(String file) {
        super(file);

        //setup memory buffers

        createCLMem(1);
        kernelMemory.add(1,null);
        createOutCLMem();
        loadMemory(2);

        //setup uniforms
        uniformLoader.initUniform(null, 16); //Object transform

    }

    public void run(PhysicsEntity physE, int vaoIndex, int size) {

        Matrix4f transformationMatrix = Maths.matrixFromEntity(physE);
        uniformLoader.loadUniform(0, transformationMatrix);
        enqueueUniforms();
        int vaoID = physE.getModel().getRawModel().getCl_vaoID();
        enqueueVAO(vaoID, 1, vaoIndex);
        executeKernel(size);
    }

    public FloatBuffer getOutput () {
        getBuffer(2);
        return memoryBuffer.get(2);
    }
}
