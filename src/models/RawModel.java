package models;

public class RawModel {

	private int vaoID;
	private int cl_vaoID;
	private int faceCount;
	private int vertexCount;
	private float radius;
	
	public RawModel(int vaoID, float radius){
		this.vaoID = vaoID;
		this.radius = radius;
	}
	
	public int getVaoID() {
		return vaoID;
	}

	public void setVaoID(int vaoID) {
		this.vaoID = vaoID;
	}

	public int getCl_vaoID() { return cl_vaoID; }

	public void setCl_vaoID(int cl_vaoID) { this.cl_vaoID = cl_vaoID; }

	public int getFaceCount() {
		return faceCount;
	}

	public void setFaceCount(int faceCount) {
		this.faceCount = faceCount;
	}

	public float getRadius() { return radius; }

	public int getVertexCount() { return vertexCount; }

	public void setVertexCount(int vertexCount) { this.vertexCount = vertexCount; }
}
