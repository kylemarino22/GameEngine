package physicsEngine;

import org.lwjgl.opencl.CLMem;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opencl.CL10.*;

public class KernelProgram extends KernelLoader {

    public ArrayList<CLMem> kernelMemory = new ArrayList<>();
    public HashMap<Integer, FloatBuffer> memoryBuffer = new HashMap<>();
    public UniformLoader uniformLoader = new UniformLoader();
    private static PointerBuffer kernel1DGlobalWorkSize = BufferUtils.createPointerBuffer(1);

    private static final int MAX_BUFFER_LENGTH = 1000;

    KernelProgram(String file) {
        super(file);
    }

    public void createCLMem (int num) {

        for (int i = 0; i < num; i++) {
            createMemoryBuffer(i);
            CLMem mem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, memoryBuffer.get(memoryBuffer.size()-1), null);
            kernelMemory.add(mem);
        }
    }

    public void createOutCLMem () {
        createMemoryBuffer(kernelMemory.size());
        CLMem mem = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, memoryBuffer.get(kernelMemory.size()), null);
        kernelMemory.add(mem);

    }

    public void createFromMemoryBuffer (FloatBuffer buf) {
        memoryBuffer.put(kernelMemory.size(), buf);
        CLMem mem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, memoryBuffer.get(memoryBuffer.size()-1), null);
        kernelMemory.add(mem);
    }

    private void createMemoryBuffer (int key) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(MAX_BUFFER_LENGTH);
        memoryBuffer.put(key, buf);
    }

    /* ======================= OUTDATED MEMORY LOADING =======================
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

    ========================================================================== */

    public void loadMemory (int location) {
        clEnqueueWriteBuffer(queue, kernelMemory.get(location), 0, 0, memoryBuffer.get(location), null, null);
    }

    public void loadMemory (int location, CLMem mem) {
        kernelMemory.set(location, mem);
    }

    //used for unloading vaos from kernelMemory
    public void unloadMemory (int location) {
        kernelMemory.remove(location);
    }

    //used for connecting kernel memory locations
//    public void linkKernelMemory (int inputLocation, int outputLocation, KernelProgram kernelToLink) {
//        kernelToLink.memoryBuffer.put(inputLocation, this.memoryBuffer.get(outputLocation));
//        kernelToLink.kernelMemory
//    }

    public void executeKernel (int size) {

        clFinish(queue);

        kernel1DGlobalWorkSize.put(0, size);

        //load args to kernel
        for(int i = 0; i < kernelMemory.size(); i++) {
            kernel.setArg(i,kernelMemory.get(i).getPointer());
        }

        clEnqueueNDRangeKernel(queue, kernel, 1, null, kernel1DGlobalWorkSize, null, null, null);

    }

//    public void releaseCLMem (int location) {
//        clReleaseMemObject(kernelMemory.get(location));
//        kernelMemory.set(location,null);
//    }

//    public void releaseKernelMem () {
//        kernelMemory = new ArrayList<>();
//    }

    public void getBuffer (int location) {
        clEnqueueReadBuffer(queue, kernelMemory.get(location) , 1, 0, memoryBuffer.get(location), null, null);
        clFinish(queue);

    }
}
