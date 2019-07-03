package toolbox;

import entities.Entity;
import entities.PhysicsEntity;
import org.lwjgl.util.vector.*;

import entities.Camera;

public class Maths {

	public static Matrix4f createTransformationMatrix (Vector3f translation, float rx, float ry,
			float rz, float scale){
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation,matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), matrix, matrix);
		return matrix;
		
	}

	public static Matrix4f createTransformationMatrix (Vector3f translation, Matrix4f rotationMatrix, float scale) {

		Matrix4f m = new Matrix4f();
		translate(translation, rotationMatrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), rotationMatrix, rotationMatrix);
		return rotationMatrix;
	}

	public static Matrix4f matrixFromEntity (Entity entity) {
		Matrix4f transformationMatrix;
		if (entity instanceof PhysicsEntity) {
			//use rotor based transformation matrix
			Matrix4f rotationMatrix = Maths.createRotationMatrix(((PhysicsEntity) entity).totalRot);
			transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
					rotationMatrix, entity.getScale());
		}
		else {
			//Default transformation matrix
			transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
					entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		}
		return transformationMatrix;
	}

	public static Matrix4f createRotationMatrix (Rotor3 rotor) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Vector3f rx = rotor.rotate(new Vector3f(1,0,0));
		Vector3f ry = rotor.rotate(new Vector3f(0,1,0));
		Vector3f rz = rotor.rotate(new Vector3f(0,0,1));

		matrix.m00 = rx.x;
		matrix.m01 = rx.y;
		matrix.m02 = rx.z;
		matrix.m10 = ry.x;
		matrix.m11 = ry.y;
		matrix.m12 = ry.z;
		matrix.m20 = rz.x;
		matrix.m21 = rz.y;
		matrix.m22 = rz.z;

		return matrix;
	}
	
	public static Matrix4f createViewMatrix (Camera camera) {
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1,0,0), viewMatrix, 
				viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0,1,0), viewMatrix, 
				viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x,-cameraPos.y,-cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
		
	}

	public static void translate (Vector3f translation, Matrix4f m) {
		m.m30 = translation.x;
		m.m31 = translation.y;
		m.m32 = translation.z;
	}


	public static Vector3f difference (Vector3f a, Vector3f b) {
		return new Vector3f(a.x-b.x, a.y-b.y, a.z-b.z);
	}

	public static Vector3f extendVector (Vector3f a, float b) {
		return new Vector3f(
				Math.copySign(Math.abs(a.x) + b, a.x),
				Math.copySign(Math.abs(a.y) + b, a.y),
				Math.copySign(Math.abs(a.z) + b, a.z));
	}

	public static Vector3f scale(Vector3f a, float b) {
		return new Vector3f(
				a.x *b,
				a.y *b,
				a.z *b);
	}

	public static Vector4f scale(Vector4f a, float b) {
		return new Vector4f(
				a.x *b,
				a.y *b,
				a.z *b,
				a.w *b);
	}

	public static Vector3f to3f (Vector4f a) {
		return new Vector3f(a.x, a.y, a.z);
	}

	public static Vector3f toEulerAngles (Vector3f rot) {
		return new Vector3f(
				(float) Math.atan2(rot.y,rot.x) ,
				(float) Math.asin(rot.z) ,
				0);
	}

	public static Vector3f angularToLinear (Vector3f w, Vector3f v, Vector3f pos) {

		float radius;
		//xy plane
		radius = (float) Math.sqrt( pos.y * pos.y + pos.x * pos.x);
		Vector3f xy = new Vector3f(-w.y * radius, -w.x * radius, 0);
		//xz
		radius = (float) Math.sqrt(pos.z *pos.z + pos.x + pos.x);
		Vector3f xz = new Vector3f(-w.z * radius,0,w.x * radius);
		//yz
		radius = (float) Math.sqrt(pos.z *pos.z + pos.y + pos.y);
		Vector3f yz = new Vector3f(0,-w.z * radius, w.y * radius);

		return new Vector3f(
				v.x + xy.x + xz.x + yz.x,
				v.y + xy.y + xz.y + yz.y,
				v.z + xy.z + xz.z + yz.z);

	}
}
