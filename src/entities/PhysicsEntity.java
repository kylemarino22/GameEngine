package entities;

import models.TexturedModel;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import physicsEngine.Physical;
import physicsEngine.PhysicsEngine;
import toolbox.Maths;
import toolbox.Rotor3;

import java.util.ArrayList;

public class PhysicsEntity extends Entity implements Physical {

    public float mass = 1.0f;
    public float InertiaTensor = 1.2f;

    public Vector3f acceleration = new Vector3f(0,0.0f,0);

    //TODO: Switch to axis angle?


    public Vector3f velocity = new Vector3f(0,0,0);

    //TODO: Store as axis angle
    public Vector4f alphaVector = new Vector4f(0,0,0,0);
    public Vector4f omegaVector = new Vector4f(0,0,0,0);

    //Store total rotation as rotor
    public Rotor3 totalRot = new Rotor3();
    public Rotor3 omegaRotor = new Rotor3();

    public float radius;

    public PhysicsEntity(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(model, position, rotX, rotY, rotZ, scale);
        radius = model.getRawModel().getRadius();
        PhysicsEngine.addPhysical(position, this);
    }

    public void calculatePhysics(float delta_t) {

        Vector3f newPosition = new Vector3f();
        Vector3f oldPosition = super.getPosition();

        velocity.x += acceleration.x * delta_t;
        velocity.y += acceleration.y * delta_t;
        velocity.z += acceleration.z * delta_t;

        newPosition.x = oldPosition.x + velocity.x * delta_t;
        newPosition.y = oldPosition.y + velocity.y * delta_t;
        newPosition.z = oldPosition.z + velocity.z * delta_t;

        float delta_v = alphaVector.w * delta_t;
        if (delta_v != 0) {
            omegaVector.w += delta_v ;
            omegaRotor = new Rotor3(Maths.to3f(omegaVector), omegaVector.w);
        }
        totalRot = totalRot.multiply(omegaRotor);

//        omega.x += alpha.x * delta_t;
//        omega.y += alpha.y * delta_t;
//        omega.z += alpha.z * delta_t;
//
//        float newRotX = super.getRotX() + omega.x * delta_t;
//        float newRotY = super.getRotY() + omega.y * delta_t;
//        float newRotZ = super.getRotZ() + omega.z * delta_t;

        super.setPosition(newPosition);
//        super.setRotX(newRotX);
//        super.setRotY(newRotY);
//        super.setRotZ(newRotZ);
    }

    public void setOmega (Vector4f omega) {
        omegaVector = omega;
        omegaRotor = new Rotor3(Maths.to3f(omega), omega.w);

    }

}
