package physicsEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.*;
import org.lwjgl.util.vector.Vector3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.lwjgl.opencl.CL10.*;

public class CollisionProgram {
    //Loads collisionKernel

    public static CLContext context;

    CollisionProgram (String file) {

        CLProgram program;
        StringBuilder physicsSource = new StringBuilder();

        try{
            BufferedReader reader = new  BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null){
                physicsSource.append(line).append("\n");
            }
            reader.close();
        } catch(IOException e){
            System.err.println("Could not read file!");
            e.printStackTrace();
            System.exit(-1);
        }

        try {

            CL.create();
            CLPlatform platform = CLPlatform.getPlatforms().get(0);
            List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_GPU);
            context = CLContext.create(platform, devices, null, null, null);
            CLCommandQueue queue = clCreateCommandQueue(context, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);

            program = clCreateProgramWithSource(context, physicsSource.toString(), null);
            Util.checkCLError(clBuildProgram(program, devices.get(0), "", null));

        } catch (LWJGLException e) {
            System.err.println("Could not create Physics Program");
            e.printStackTrace();
            System.exit(-1);
        }



        //Uniform loading
        UniformLoader.initUniform(null, 3); //Velocity vector
        UniformLoader.initUniform(null, 3); //Omega vector
        UniformLoader.initUniform(null, 16); //Entity transform
        UniformLoader.initUniform(null, 16); //Object transform

    }

//    public void
}
