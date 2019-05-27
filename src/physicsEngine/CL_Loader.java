//package physicsEngine;
//
//import entities.Entity;
//import entities.PhysicsEntity;
//import org.lwjgl.BufferUtils;
//import org.lwjgl.opencl.CLCommandQueue;
//import org.lwjgl.opencl.CLContext;
//import org.lwjgl.opencl.CLMem;
//import org.lwjgl.util.vector.Matrix4f;
//import toolbox.Maths;
//
//import java.nio.FloatBuffer;
//import java.util.ArrayList;
//
//import static org.lwjgl.opencl.CL10.*;
//
//
//public class CL_Loader{
//
//    public static ArrayList<cl_vbo> vbos = new ArrayList<>();
//    private static CLMem uniformBuffer;
//    private static CLContext context;
//    private static CLCommandQueue queue;
//    private static ArrayList<KernelProgram> vboKernels = new ArrayList;
//
//    public static int createVAO(float[] positions, float[] normals, int[] indices) {
//
//        cl_vbo objToLoad = new cl_vbo(indices);
//        objToLoad.storeDataInAttributeList(0, positions);
//        objToLoad.storeDataInAttributeList(1, normals);
//        vbos.add(objToLoad);
//        return vbos.size() - 1;
//
//    }
//
//    public static void attachKernel (KernelProgram kernel) {
//        vboKernels.add(kernel);
//    }
//
//    public static class cl_vbo {
//        public ArrayList<CLMem> vao;
//        public int[] indicesBuffer;
//
//        cl_vbo (int[] indicesBuffer) {
//            this.indicesBuffer = indicesBuffer;
//        }
//
//        void storeDataInAttributeList(int attributeNumber, float[] data) {
//            FloatBuffer buf = toFloatBuffer(data);
//            vao.add(attributeNumber, clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, buf, null));
//            clEnqueueWriteBuffer(queue, vao.get(attributeNumber), 1, 0, buf, null, null);
//
//        }
//
//        private static FloatBuffer toFloatBuffer(float[] floats) {
//            FloatBuffer buf = BufferUtils.createFloatBuffer(floats.length).put(floats);
//            buf.rewind();
//            return buf;
//        }
//
//    }
//
//    public static void prepareInstances (PhysicsEntity O, Entity E) {
//
//        UniformLoader.loadUniform(0, O.velocity);
//        UniformLoader.loadUniform(1, O.omega);
//        Matrix4f transformationMatrix = Maths.createTransformationMatrix(O.getPosition(),
//                O.getRotX(), O.getRotY(), O.getRotZ(), O.getScale());
//        UniformLoader.loadUniform(2, transformationMatrix);
//        transformationMatrix = Maths.createTransformationMatrix(E.getPosition(),
//                E.getRotX(), E.getRotY(), E.getRotZ(), E.getScale());
//        UniformLoader.loadUniform(3, transformationMatrix);
//
//        FloatBuffer buf = UniformLoader.genUniformArrays();
//        uniformBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, buf, null);
//        clEnqueueWriteBuffer(queue, uniformBuffer, 1, 0, buf, null, null);
//
//    }
//
//
//
//
//}
//
//
//
//
//
//
