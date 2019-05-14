package physicsEngine;

import com.sun.tools.javac.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class UniformLoader {

    private static ArrayList<Pair<Object,Integer>> uniformList = new ArrayList<>();
    private static int bufferLength = 0;

    public static void initUniform (Object type, int size) {
        uniformList.add(new Pair(type,size));
        bufferLength += size;
    }

    //4x4 matrix
    public static void loadUniform (int index, Matrix4f m) {
        uniformList.set(index, new Pair(m,16));
    }

    private static FloatBuffer genUniform (Matrix4f m) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        m.loadTranspose(buf);
        return buf;
    }

    //Vector 3f
    public static void loadUniform (int index, Vector3f v) {
        uniformList.set(index, new Pair(v,3));
    }

    private static FloatBuffer genUniform (Vector3f v) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        v.load(buf);
        return buf;
    }

    public static void genUniformArrays () {
        FloatBuffer uniformFloats = BufferUtils.createFloatBuffer(bufferLength + uniformList.size() + 1);
        uniformFloats.put(uniformList.size());

        for(Pair<Object, Integer> uniform : uniformList) {
            uniformFloats.put(uniform.snd);
        }

        for(Pair<Object, Integer> uniform : uniformList) {
            if (uniform.fst instanceof Matrix4f) {
                uniformFloats.put(genUniform((Matrix4f)uniform.fst));
            }
            else if (uniform.fst instanceof Vector3f) {
                uniformFloats.put(genUniform((Vector3f) uniform.fst));
            }
        }
    }
}
