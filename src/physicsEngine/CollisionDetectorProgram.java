package physicsEngine;

import entities.PhysicsEntity;
import models.RawModel;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import toolbox.Maths;

import java.nio.FloatBuffer;

import static org.lwjgl.opencl.CL10.CL_MEM_COPY_HOST_PTR;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;

public class CollisionDetectorProgram extends KernelVBOProgram{

    private FloatBuffer out;

    CollisionDetectorProgram (String file) {
        super(file);

        //setup uniforms
        uniformLoader.initUniform(null,1); //Delta t
        uniformLoader.initUniform(null, 3); //O Vertex Pos
        uniformLoader.initUniform(null, 3); //Speed Vector
        uniformLoader.initUniform(null, 16); //E transform for E velocity
    }

    public void run(float delta_t, Vector3f vec, Vector3f velocity,
                    PhysicsEntity E) {

        uniformLoader.loadUniform(0, delta_t);
        uniformLoader.loadUniform(1, vec);
        uniformLoader.loadUniform(2, velocity);

        Matrix4f transformationMatrix = Maths.createTransformationMatrix(
                Maths.scale(E.velocity, delta_t),
                E.omega.x * delta_t,
                E.omega.y * delta_t,
                E.omega.z * delta_t,
                E.getScale());
        uniformLoader.loadUniform(3, transformationMatrix);


        enqueueUniforms();

        int vaoID = E.getModel().getRawModel().getCl_vaoID();
        enqueueVAO(vaoID, 1,0); //indices
        enqueueVAO(vaoID, 2, 1); //Vertices
        enqueueVAO(vaoID, 3, 2); //Normals
        enqueueVAO(vaoID, 4, 3); //EdgeNormals

        int size = getFaceCount(vaoID) * 4;
        out = BufferUtils.createFloatBuffer(size);
        loadMemory(5, out, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR);
        executeKernel(size);
    }

    public FloatBuffer getOutput () {
        getBuffer(5,out);
        return out;
    }
}
