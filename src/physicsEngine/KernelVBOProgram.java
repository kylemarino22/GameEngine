package physicsEngine;

import org.lwjgl.BufferUtils;
import org.lwjgl.opencl.CLMem;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opencl.CL10.*;

public class KernelVBOProgram extends KernelProgram{

    public ArrayList<cl_vbo> vbos = new ArrayList<>();

    private static ArrayList<KernelVBOProgram> kernelList = new ArrayList<>();

    KernelVBOProgram (String file) {
        super(file);
        kernelList.add(this);
    }

    public void enqueueUniforms () {
        FloatBuffer buf = uniformLoader.genUniformArrays();
        loadMemory(0,buf, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR);
    }

    public void enqueueVAO (int id, int location, int index) {
        cl_vbo vbo = vbos.get(id);
        loadMemory(location, vbo.vao.get(index));

    }

    public int getFaceCount (int id) {
        return vbos.get(id).faceCount;
    }

    public static int createVAO(float[] positions, float[] normals, int[] indices, float[] edgeNormals) {

        for (KernelVBOProgram k :kernelList) {
            cl_vbo objToLoad = new cl_vbo(k, indices.length/3);
            objToLoad.storeDataInAttributeList(0, indices);
            objToLoad.storeDataInAttributeList(1, positions);
            objToLoad.storeDataInAttributeList(2, normals);
            objToLoad.storeDataInAttributeList(3, edgeNormals);
            k.vbos.add(objToLoad);
        }
        return kernelList.get(0).vbos.size() - 1;
    }

    public static class cl_vbo {
        private ArrayList<CLMem> vao = new ArrayList<>();

        private KernelVBOProgram kernel;
        private int faceCount;

        cl_vbo (KernelVBOProgram kernel, int faceCount) {
            this.kernel = kernel;
            this.faceCount = faceCount;
        }

        void storeDataInAttributeList(int attributeNumber, float[] data) {
            FloatBuffer buf = toFloatBuffer(data);
            vao.add(attributeNumber, clCreateBuffer(kernel.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, buf, null));
            clEnqueueWriteBuffer(kernel.queue, vao.get(attributeNumber), 1, 0, buf, null, null);

        }

        void storeDataInAttributeList(int attributeNumber, int[] data) {
            IntBuffer buf = toIntBuffer(data);
            vao.add(attributeNumber, clCreateBuffer(kernel.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, buf, null));
            clEnqueueWriteBuffer(kernel.queue, vao.get(attributeNumber), 1, 0, buf, null, null);

        }
        private static FloatBuffer toFloatBuffer(float[] floats) {
            FloatBuffer buf = BufferUtils.createFloatBuffer(floats.length).put(floats);
            buf.rewind();
            return buf;
        }
        private static IntBuffer toIntBuffer(int[] ints) {
            IntBuffer buf = BufferUtils.createIntBuffer(ints.length).put(ints);
            buf.rewind();
            return buf;
        }

    }
}