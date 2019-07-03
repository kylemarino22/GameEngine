package physicsEngine;

import entities.Entity;
import entities.PhysicsEntity;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import toolbox.Maths;
import toolbox.Rotor3;

import java.lang.reflect.Array;
import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opencl.CL10.CL_MEM_COPY_HOST_PTR;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;

public class PhysicsEngine {

    private final static String vectorTransformerFile = "vectorTransformer";
    private final static String vertexValidatorFile = "vertexValidator";
    private final static String collisionDetectorFile = "collisionDetector";


    //Chunk Size
    private final static int CHUNK_X = 20;
    private final static int CHUNK_Y = 50;
    private final static int CHUNK_Z = 20;
    private final static float elasticity = 1;

    private static HashMap<Chunk, ArrayList<Physical>> physicsEntityMap = new HashMap<>();

    ArrayList<PhysicsEntity> physicalList;

    //Static, Dynamic, Active
    private VectorTransformProgram vertexTransformer;
    private VectorTransformProgram normalTransformer;
    private VectorTransformProgram edgeNormalTransformer;


    private VertexValidatorProgram vertexValidator;
    private CollisionDetectorProgram collisionDetector;


    public PhysicsEngine () {
        vertexTransformer = new VectorTransformProgram(vectorTransformerFile);
        vertexValidator = new VertexValidatorProgram(vertexValidatorFile);
        collisionDetector = new CollisionDetectorProgram(collisionDetectorFile);
    }

    public void collisionDetection (float delta_t) {


        for (Map.Entry<Chunk, ArrayList<Physical>> entry : physicsEntityMap.entrySet()) {
            ArrayList<Physical> chunkList = entry.getValue();

            //i loop is object
            //j loop is entity
            for (int i = 0; i < chunkList.size(); i++) {

                //TODO: Could be optimized. This will compare each object twice
                ArrayList<PhysicsEntity> pceList = prepareCollisionDetection((PhysicsEntity) chunkList.get(i), chunkList);

                PhysicsEntity currentEntity = (PhysicsEntity) chunkList.get(i);

                vertexTransformer.run(currentEntity, 1,
                        currentEntity.getModel().getRawModel().getVertexCount());

                FloatBuffer transformedVertices = vertexTransformer.getOutput();
//                KernelLoader.print(transformedVertices);

                //for E
                for (PhysicsEntity physE: pceList ) {

                    //for Verticies of O
                    vertexValidator.run(physE, transformedVertices);

                    FloatBuffer validatedVertices = vertexValidator.getOutput();
                    System.out.println("============================ VALID VERTICES ============================");
                    KernelLoader.print(validatedVertices);
                    System.out.println("========================================================================");


                    //Check if whole array is invalid
                    boolean noValidVertex = true;
                    for (int j = 0; j < validatedVertices.capacity()/3; j++) {
                        if (validatedVertices.get(3*j) != Float.MAX_VALUE) noValidVertex = false;
                    }
                    if (noValidVertex) break;

                    //TODO: Use actual equation to do this

                    Vector3f EBasis_axis = Maths.scale(Maths.to3f(physE.omegaVector), physE.omegaVector.w);
                    Vector3f EBasis_OCenter = Vector3f.sub(currentEntity.getPosition(), physE.getPosition(), null);
                    EBasis_OCenter = physE.totalRot.rotate(EBasis_OCenter);
                    EBasis_axis = physE.totalRot.rotate(EBasis_axis);

                    Vector3f collisionVector = new Vector3f(0,0,0);
                    Vector3f collisionVertex = new Vector3f(0,0,0);
                    Vector3f collisionVelocity = new Vector3f(0,0,0);

                    int cnumSum = 0;

                    //for valid vertex of O
                    for (int j = 0; j < validatedVertices.capacity()/3; j++) {
                        if (validatedVertices.get(3*j) == Float.MAX_VALUE) { continue; }

                        //TODO: Could Be optimized by moving validated vertex transformations to a gpu kernel
                        //Create velocity vector
                        Vector3f WBasis_point = new Vector3f(
                                validatedVertices.get(3*j),
                                validatedVertices.get(3*j+1),
                                validatedVertices.get(3*j+2));
                        Vector3f OBasis_point = Vector3f.sub(WBasis_point, currentEntity.getPosition(), null);
                        Vector3f axis = Maths.scale(Maths.to3f(currentEntity.omegaVector), currentEntity.omegaVector.w);
                        Vector3f velocityVec3f = Vector3f.cross(axis, OBasis_point, null);
                        velocityVec3f = Vector3f.add(currentEntity.velocity, velocityVec3f, null);

                        //transform into E-space

                        Vector3f EBasis_point = Vector3f.sub(WBasis_point, physE.getPosition(), null);
                        Vector3f EBasis_rotVel = Vector3f.cross(EBasis_axis, EBasis_point, null);

                        velocityVec3f = Vector3f.sub(velocityVec3f, physE.velocity, null);
                        velocityVec3f = Vector3f.sub(velocityVec3f, EBasis_rotVel, null);

                        /*
                        =============================== Old Velocity Calculation ====================================
                        axis = Maths.scale(Maths.to3f(physE.omegaVector), physE.omegaVector.w);
                        Vector3f rotEVelocity = phys

                        Vector4f velocityVec4f = new Vector4f(velocityVec3f.x, velocityVec3f.y, velocityVec3f.z, 1);
                        Vector4f point4f = new Vector4f(point.x, point.y, point.z, 1);

                        point4f = Matrix4f.transform(EtransformationMatrix, point4f, null);
                        velocityVec4f = Matrix4f.transform(EtransformationMatrix, velocityVec4f, null);
                        =============================================================================================
                        */

                        //Run collision detector

                        //TODO: Clean up collisionDetctor kernel (Easy)
                        collisionDetector.run(delta_t, EBasis_point, velocityVec3f, physE);
                        FloatBuffer collisionVertices = collisionDetector.getOutput();
                        System.out.println();
                        KernelLoader.print(collisionVertices);

                        Vector3f unitNorm = new Vector3f(0,0,0);
                        Vector3f c_location = new Vector3f(0,0,0);
                        int numSum = 0;

                        for (int k = 0; k < collisionVertices.capacity()/10; k++) {
                            if (collisionVertices.get(10*k) == Float.MAX_VALUE) { continue; }

                            unitNorm = Vector3f.add(unitNorm, new Vector3f(
                                    collisionVertices.get(10*k + 7),
                                    collisionVertices.get(10*k + 8),
                                    collisionVertices.get(10*k + 9)), null);

                            c_location = Vector3f.add(c_location, new Vector3f(
                                    collisionVertices.get(10*k + 1),
                                    collisionVertices.get(10*k + 2),
                                    collisionVertices.get(10*k + 3)), null);

                            numSum++;


                        }

                        if (numSum == 0) {
                            break;
                        }
                        else if (numSum > 1) {
                            unitNorm = Maths.scale(unitNorm, (float)1/numSum);
                            c_location = Maths.scale(c_location, (float)1/numSum);
                        }

                        collisionVector = Vector3f.add(collisionVector, unitNorm, null);
                        collisionVertex = Vector3f.add(collisionVertex, c_location, null);
                        collisionVelocity = Vector3f.add(collisionVelocity, velocityVec3f, null);


                        cnumSum ++;
                    }

                    if (cnumSum == 0) {
                        break;
                    }
                    else if (cnumSum > 1) {
                        collisionVector = Maths.scale(collisionVector, (float)1/cnumSum);
                        collisionVertex = Maths.scale(collisionVertex, (float)1/cnumSum);
                        collisionVelocity = Maths.scale(collisionVelocity, (float)1/cnumSum);
                    }

                    Vector3f WBasis_Norm = physE.totalRot.rotate(collisionVector);
//                    Vector3f ERot_Norm = physE.totalRot.inverse().rotate(collisionVector);
//                            Vector3f WBasis_c = physE.totalRot.rotate(c_location);

                    Vector3f OBasis_collsionVertex = Vector3f.sub(EBasis_OCenter, collisionVertex, null);

                    //evaluate collision impulse
                    float num = -(1 + elasticity) * Vector3f.dot(collisionVelocity, collisionVector);

                    float den = 1/currentEntity.mass + 1/physE.mass
                            + Vector3f.cross(collisionVertex, collisionVector, null).lengthSquared()/physE.InertiaTensor
                            + Vector3f.cross(OBasis_collsionVertex, collisionVector, null).lengthSquared()/currentEntity.InertiaTensor;

                    float a = Vector3f.cross(collisionVertex, collisionVector, null).lengthSquared()/physE.InertiaTensor;
                    float b = Vector3f.cross(OBasis_collsionVertex, collisionVector, null).lengthSquared()/currentEntity.InertiaTensor;


                    float jimpulse = num/den;


                    currentEntity.velocity = Vector3f.add(currentEntity.velocity, Maths.scale(WBasis_Norm, jimpulse/currentEntity.mass), null);
                    physE.velocity =  Vector3f.sub(physE.velocity ,Maths.scale(WBasis_Norm, jimpulse/physE.mass), null );
//                            currentEntity.velocity +=







                }
            }
        }
    }

    //TODO: This could be optimized through opencl. Not sure if worth it
    private ArrayList<PhysicsEntity> prepareCollisionDetection (PhysicsEntity physE, ArrayList<Physical> pceArray) {

        ListIterator<Physical> pceIter = pceArray.listIterator();
        ArrayList<PhysicsEntity> output = new ArrayList<>();

        while(pceIter.hasNext()){
            Physical p = pceIter.next();

            if (physE.equals(p)) continue;

            if (p instanceof PhysicsEntity) {
                PhysicsEntity currentPhysE = (PhysicsEntity) p;
                float distance = Maths.difference(physE.getPosition(),currentPhysE.getPosition()).length();

                if (distance < currentPhysE.radius *currentPhysE.getScale() + physE.radius * physE.getScale()) {
                    output.add(currentPhysE);
                }
            }
        }

        return output;
    }


    public static void addPhysical(Vector3f pos, Physical p) {

        Chunk c = new Chunk(pos);
        ArrayList<Physical> entityList = physicsEntityMap.get(c);

        if (entityList == null) {
            entityList = new ArrayList<>();
        }

        entityList.add(p);
        physicsEntityMap.put(c,entityList);
    }

    private static class Chunk {
        private int x, y, z;
        Chunk(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Chunk (Vector3f pos) {
            this.x =(int) pos.x / CHUNK_X;
            this.y = (int) pos.y / CHUNK_Y;
            this.z = (int) pos.z / CHUNK_Z;
        }

        @Override
        public int hashCode() {
            return (x*31 + y)*29 + z;
        }

        @Override
        public boolean equals(Object obj) {
            return ((Chunk)obj).x == x && ((Chunk)obj).y == y && ((Chunk)obj).z == z;
        }
    }
}
