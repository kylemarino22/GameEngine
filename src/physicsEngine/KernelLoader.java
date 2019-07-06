package physicsEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.lwjgl.opencl.Util;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.CLKernel;
import java.nio.FloatBuffer;
import java.util.List;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLPlatform;
import static org.lwjgl.opencl.CL10.*;


public abstract class KernelLoader {

    public static boolean debug = false;

    private boolean init = false;
    protected CLContext context;
    protected CLCommandQueue queue;
    private static CLDevice device;

    public CLKernel kernel;

    public KernelLoader(String file) {

        if (!init) {
            initCL();
        }

        StringBuilder programSource = new StringBuilder();
        try{
            BufferedReader reader = new  BufferedReader(new FileReader("src/physicsEngine/" +file + ".txt"));
            String line;
            while((line = reader.readLine()) != null){
                if(line.contains("#")) continue;

                programSource.append(line).append("\n");
            }
            reader.close();
        } catch(IOException e){
            System.err.println("Could not read file!");
            e.printStackTrace();
            System.exit(-1);
        }

        CLProgram program = clCreateProgramWithSource(context, programSource.toString(), null);
        Util.checkCLError(clBuildProgram(program, device, "", null));

        kernel = clCreateKernel(program, file, null);


    }

    private void initCL () {

        try {
            CL.create();
            CLPlatform platform = CLPlatform.getPlatforms().get(0);
            List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_GPU);
            device = devices.get(0);
            context = CLContext.create(platform, devices, null, null, null);
            queue = clCreateCommandQueue(context, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);
        }
        catch (Exception e) {
            System.out.println("Failed to get lit");
        }

    }

    protected static FloatBuffer toFloatBuffer(float[] floats) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(floats.length).put(floats);
        buf.rewind();
        return buf;
    }

    static void print(FloatBuffer buffer, int size) {

        if (!debug) return;

        for (int i = 0; i < buffer.capacity(); i++) {
            if (i % size == 0) {
                System.out.println("");
            }
            System.out.format("%-15s", buffer.get(i));
        }
        System.out.println("");
    }

    static void debugPrint (String s) {

        if (!debug) return;
        System.out.println(s);
    }
}
