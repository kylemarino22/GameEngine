import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opencl.CL10.*;

public class CL_Loader {

    public static void main(String[] args) {
        KernelProgram k1 = new KernelProgram("Kernel1");
        KernelProgram k2 = new KernelProgram("Kernel2");
        float[] a = new float[] {1,2,4,1,5,1,5,2,3,1,5,1,3,2,5,1,5,1,5,2,3,1,2,3,4,1,2,3,4,1,5,1};
        float[] b = new float[] {1,2,4,1,5,1,5,2,3,1,5,1,3,2,5,1,5,1,5,2,3,1,2,3,4,1,2,3,4,1,5,1};

        //load constant memory

        k2.loadMemory(0, b);


        FloatBuffer out = BufferUtils.createFloatBuffer(32);
        k1.loadMemory(0,out, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR);


        k1.executeKernel();
        k1.getBuffer(0, out);
        print(out);

        k2.loadMemory(1,out);

        FloatBuffer out1 = BufferUtils.createFloatBuffer(32);
        k2.loadMemory(2,out1, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR);

        k2.executeKernel();
        k2.getBuffer(2,out1);

        print(out1);


//        k2.executeKernel();
//
//        print(out);
    }

    static void print(FloatBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.print(buffer.get(i)+" ");
        }
        System.out.println("");
    }
}
