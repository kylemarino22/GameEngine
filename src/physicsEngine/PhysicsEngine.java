package physicsEngine;

import entities.PhysicsEntity;
import org.lwjgl.util.vector.Vector3f;
import toolbox.Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

public class PhysicsEngine {

    //Chunk Size
    private final static int CHUNK_X = 20;
    private final static int CHUNK_Y = 50;
    private final static int CHUNK_Z = 20;


    HashMap<Chunk, ArrayList<Physical>> physicsEntityMap = new HashMap<>();

    ArrayList<PhysicsEntity> physicalList;

    //Static, Dynamic, Active

    PhysicsEngine () {

    }

    public void collisionDetection (float delta_t) {

        for (PhysicsEntity physE : physicalList) {
            ArrayList<Physical> potentialCollisions = new ArrayList<>();
            prepareCollisionDetection(physE, potentialCollisions);

            //Call to Collision Program

        }
    }

    private void prepareCollisionDetection (PhysicsEntity physE, ArrayList<Physical> pceArray) {

        ArrayList<Physical> potentialCollisionEntities = physicsEntityMap.get(new Chunk(physE.getPosition()));

        ListIterator<Physical> pceIter = potentialCollisionEntities.listIterator();

        while(pceIter.hasNext()){
            Physical p = pceIter.next();

            if (p instanceof PhysicsEntity) {
                PhysicsEntity currentPhysE = (PhysicsEntity) p;
                float distance = Maths.difference(physE.getPosition(),currentPhysE.getPosition()).length();

                if (distance < currentPhysE.radius + physE.radius) {
                    pceArray.add(p);
                }
            }
        }
    }


    public void addPhysical(Vector3f pos, Physical p) {

        ArrayList<Physical> entityList = physicsEntityMap.get(new Chunk(pos));

        if (entityList == null) {
            entityList = new ArrayList<>();
        }

        entityList.add(p);
    }







    private class Chunk {
        public int x, y, z;
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
        public boolean equals(Object obj) {
            return ((Chunk)obj).x == x && ((Chunk)obj).y == y && ((Chunk)obj).z == z;
        }
    }
}
