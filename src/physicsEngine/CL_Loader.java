package physicsEngine;

import org.lwjgl.BufferUtils;
import org.lwjgl.opencl.CLMem;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opencl.CL10.CL_MEM_COPY_HOST_PTR;
import static org.lwjgl.opencl.CL10.CL_MEM_READ_ONLY;
import static org.lwjgl.opencl.CL10.clCreateBuffer;

public class CL_Loader{

    private static ArrayList<cl_vbo> vbos = new ArrayList<>();

    public static int createVAO(float[] positions, float[] normals, int[] indices) {

        cl_vbo objToLoad = new cl_vbo(indices);
        objToLoad.storeDataInAttributeList(0, positions);
        objToLoad.storeDataInAttributeList(1, normals);
        vbos.add(objToLoad);
        return vbos.size() - 1;

    }

    public static class cl_vbo {
        public ArrayList<CLMem> vao;
        public int[] indicesBuffer;

        cl_vbo (int[] indicesBuffer) {
            this.indicesBuffer = indicesBuffer;
        }

        void storeDataInAttributeList(int attributeNumber, float[] data) {
            FloatBuffer buf = toFloatBuffer(data);
            vao.add(attributeNumber, clCreateBuffer(CollisionProgram.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, buf, null));
        }

        private static FloatBuffer toFloatBuffer(float[] floats) {
            FloatBuffer buf = BufferUtils.createFloatBuffer(floats.length).put(floats);
            buf.rewind();
            return buf;
        }

    }




}






