package physicsEngine;

import com.sun.tools.javac.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class UniformLoader {

    private ArrayList<Pair<Object,Integer>> uniformList = new ArrayList<>();
    private int bufferLength = 0;

    public void initUniform (Object type, int size) {
        uniformList.add(new Pair(type,size));
        bufferLength += size;
    }

    //4x4 matrix
    public void loadUniform (int index, Matrix4f m) {
        uniformList.set(index, new Pair(m,16));
    }

    private FloatBuffer genUniform (Matrix4f m) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        m.store(buf);
        buf.flip();
        return buf;
    }

    //Vector 3f
    public void loadUniform (int index, Vector3f v) {
        uniformList.set(index, new Pair(v,3));
    }

    private FloatBuffer genUniform (Vector3f v) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(3);
        v.store(buf);
        buf.flip();
        return buf;
    }

    //float
    public void loadUniform (int index, float f) { uniformList.set(index, new Pair(new Float(f),1));}

    private FloatBuffer genUniform (float f) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(1).put(f);
        buf.flip();
        return buf;
    }


    public FloatBuffer genUniformArrays () {
        FloatBuffer uniformFloats = BufferUtils.createFloatBuffer(bufferLength);

        for(Pair<Object, Integer> uniform : uniformList) {
            if (uniform.fst instanceof Matrix4f) {
                uniformFloats.put(genUniform((Matrix4f)uniform.fst));
            }
            else if (uniform.fst instanceof Vector3f) {
                uniformFloats.put(genUniform((Vector3f) uniform.fst));
            }
            else if (uniform.fst instanceof Float) {
                uniformFloats.put(genUniform((Float) uniform.fst));
            }
        }
//        KernelLoader.print(uniformFloats);
        uniformFloats.flip();
        return uniformFloats;
    }

    //method to dump into floatbuffer
}
