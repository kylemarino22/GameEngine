package physicsEngine;

import entities.Entity;
import entities.PhysicsEntity;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import toolbox.Maths;

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
                KernelLoader.print(transformedVertices);

                //for E
                for (PhysicsEntity physE: pceList ) {

                    //for Verticies of O
                    vertexValidator.run(physE, transformedVertices);

                    FloatBuffer validatedVertices = vertexValidator.getOutput();
                    KernelLoader.print(validatedVertices);

                    //Check if whole array is invalid
                    boolean noValidVertex = true;
                    for (int j = 0; j < validatedVertices.capacity()/3; j++) {
                        if (validatedVertices.get(3*j) != Float.MAX_VALUE) noValidVertex = false;
                    }
                    if (noValidVertex) break;

                    //TODO: Use actual equation to do this
                    //for Velocity of O
                    Matrix4f OtransformationMatrix = Maths.createTransformationMatrix(
                            Maths.scale(currentEntity.velocity, delta_t),
                            currentEntity.omegaVector.x * delta_t,
                            currentEntity.omegaVector.y * delta_t,
                            currentEntity.omegaVector.z * delta_t,
                            currentEntity.getScale());

                    //for transforming O-vec and O-velocity
                    Matrix4f EtransformationMatrix = Maths.createTransformationMatrix(physE.getPosition(),
                            physE.getRotX(), physE.getRotY(), physE.getRotZ(), physE.getScale());
                    EtransformationMatrix = Matrix4f.invert(EtransformationMatrix, null);

                    //for valid vertex of O
                    for (int j = 0; j < validatedVertices.capacity()/3; j++) {
                        if (validatedVertices.get(3*j) == Float.MAX_VALUE) { continue; }

                        //Create velocity vector
                        Vector4f vec = new Vector4f(
                                validatedVertices.get(3*j),
                                validatedVertices.get(3*j+1),
                                validatedVertices.get(3*j+2), 1);
                        Vector4f newPos = Matrix4f.transform(OtransformationMatrix, vec, null);

                        Vector4f velocity = new Vector4f(newPos.x-vec.x, newPos.y-vec.y, newPos.z-vec.z, 0);
                        velocity = Maths.scale(velocity, 1/delta_t);

                        //transform into E-space

                        vec = Matrix4f.transform(EtransformationMatrix, vec, null);
                        velocity = Matrix4f.transform(EtransformationMatrix, velocity, null);

                        //Run collision detector
                        collisionDetector.run(delta_t, Maths.to3f(vec), Maths.to3f(velocity), physE);
                        FloatBuffer collisionVertices = collisionDetector.getOutput();
                        System.out.println();
                        KernelLoader.print(collisionVertices);

                        for (int k = 0; k < collisionVertices.capacity(); k++) {
                            if (collisionVertices.get(10*k) == Float.MAX_VALUE) { continue; }

                            Vector3f relativeVelocity = new Vector3f(
                                    collisionVertices.get(10*k + 4),
                                    collisionVertices.get(10*k + 5),
                                    collisionVertices.get(10*k + 6));

                            Vector3f unitNorm = new Vector3f(
                                    collisionVertices.get(10*k + 7),
                                    collisionVertices.get(10*k + 8),
                                    collisionVertices.get(10*k + 9));
                            unitNorm = unitNorm.normalise(null);



                            //evaluate collision
                            float num = -(1 + elasticity) * Vector3f.dot(unitNorm, relativeVelocity);


                        }
                    }
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
