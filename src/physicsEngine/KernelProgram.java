package physicsEngine;

import org.lwjgl.opencl.CLMem;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opencl.CL10.*;

public class KernelProgram extends KernelLoader {

    public ArrayList<CLMem> kernelMemory = new ArrayList<>();
    public UniformLoader uniformLoader = new UniformLoader();


    KernelProgram(String file) {
        super(file);
    }

    public void loadMemory (int location, float[] data) {
        FloatBuffer buf = toFloatBuffer(data);

        CLMem mem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, buf, null);
        clEnqueueWriteBuffer(queue, mem, 1, 0, buf, null, null);

        kernelMemory.add(location, mem);
    }

    public void loadMemory (int location, FloatBuffer buf) {
        CLMem mem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, buf, null);
        clEnqueueWriteBuffer(queue, mem, 1, 0, buf, null, null);

        kernelMemory.add(location, mem);
    }

    public void loadMemory (int location, FloatBuffer buf, long flags) {
        CLMem mem = clCreateBuffer(context, flags, buf, null);
        clEnqueueWriteBuffer(queue, mem, 1, 0, buf, null, null);

        kernelMemory.add(location, mem);
    }

    public void loadMemory(int location, CLMem mem) {
        kernelMemory.add(location, mem);
    }

    public void executeKernel (int size) {

        clFinish(queue);

        PointerBuffer kernel1DGlobalWorkSize = BufferUtils.createPointerBuffer(1);
        kernel1DGlobalWorkSize.put(0, size);

        //load args to kernel
        for(int i = 0; i < kernelMemory.size(); i++) {
            kernel.setArg(i,kernelMemory.get(i).getPointer());
        }

        clEnqueueNDRangeKernel(queue, kernel, 1, null, kernel1DGlobalWorkSize, null, null, null);

    }

    public void getBuffer (int location, FloatBuffer buf) {
        clEnqueueReadBuffer(queue, kernelMemory.get(location) , 1, 0, buf, null, null);
        clFinish(queue);

    }
}
