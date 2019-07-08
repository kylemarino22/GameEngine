package renderEngine;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import models.RawModel;
import physicsEngine.KernelVBOProgram;

public class Loader {
	
	private ArrayList<Integer> vaos = new ArrayList<Integer>();
	private ArrayList<Integer> vbos = new ArrayList<Integer>();
	private ArrayList<Integer> textures = new ArrayList<Integer>();

	
	public RawModel loadtoVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices, float radius){
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0,3,positions);
		storeDataInAttributeList(1,2,textureCoords);
		storeDataInAttributeList(2,3,normals);

		unbindVAO();

		RawModel model = new RawModel(vaoID, radius);

		if (radius > 0) {
			float[] edgeNormals = new float[indices.length * 3];
			normals = generateNormals(positions, indices, edgeNormals);
			KernelVBOProgram.createVAO(positions, normals, indices, edgeNormals, model);
		}
		return model;
	}
	
	public int loadTexture(String fileName){
		Texture texture = null;

		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream("res/"+fileName+".png"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
		
		
	}
	
	public void cleanUp(){
		for(int vao:vaos){
			GL30.glDeleteVertexArrays(vao);
		}
		for(int vbo:vbos){
			GL15.glDeleteBuffers(vbo);
		}
		
		for(int texture:textures){
			GL11.glDeleteTextures(texture);
		}
	}


	private float[] generateNormals(float[] vertices, int[] indices, float[] edgeNormals) {

		float[] out = new float[indices.length];

		for (int i = 0; i < indices.length/3; i++) {
			int index0 = indices[3*i];
			int index1 = indices[3*i+1];
			int index2 = indices[3*i+2];


			Vector3f a = new Vector3f(vertices[index0*3], vertices[index0*3+1], vertices[index0*3+2]);
			Vector3f b = new Vector3f(vertices[index1*3], vertices[index1*3+1], vertices[index1*3+2]);
			Vector3f c = new Vector3f(vertices[index2*3], vertices[index2*3+1], vertices[index2*3+2]);

			//Dir = (B - A) x (C - A)
			//Norm = Dir / len(Dir)
			Vector3f norm = Vector3f.cross(Vector3f.sub(b,a,null), Vector3f.sub(c,a,null), null);
			float len = norm.length();
			norm.scale(1/len);
			out[i*3] = norm.x;
			out[i*3+1] = norm.y;
			out[i*3+2] = norm.z;

			Vector3f edgeNormal0 = Vector3f.cross(norm, Vector3f.sub(a,b,null),null);
			Vector3f edgeNormal1 = Vector3f.cross(norm, Vector3f.sub(b,c,null),null);
			Vector3f edgeNormal2 = Vector3f.cross(norm, Vector3f.sub(c,a,null),null);


			edgeNormals[9*i] = edgeNormal0.x;
			edgeNormals[9*i+1] = edgeNormal0.y;
			edgeNormals[9*i+2] = edgeNormal0.z;
			edgeNormals[9*i+3] = edgeNormal1.x;
			edgeNormals[9*i+4] = edgeNormal1.y;
			edgeNormals[9*i+5] = edgeNormal1.z;
			edgeNormals[9*i+6] = edgeNormal2.x;
			edgeNormals[9*i+7] = edgeNormal2.y;
			edgeNormals[9*i+8] = edgeNormal2.z;
		}

		return out;
	}
	
	private int createVAO(){
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0,0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	
	private void unbindVAO(){
		GL30.glBindVertexArray(0);
	}
	
	private void bindIndicesBuffer(int[] indices){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data){
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
}
