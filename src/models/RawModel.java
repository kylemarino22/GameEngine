package models;

public class RawModel {

	private int vaoID;
	private int cl_vaoID;
	private int vertexCount;
	private float radius;
	
	public RawModel(int vaoID, int cl_vaoID, int vertexCount, float radius){
		this.vaoID = vaoID;
		this.cl_vaoID = cl_vaoID;
		this.vertexCount = vertexCount;
		this.radius = radius;
	}
	
	public int getVaoID() {
		return vaoID;
	}

	public int getCl_vaoID() { return cl_vaoID; }

	public void setVaoID(int vaoID) {
		this.vaoID = vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}

	public float getRadius() { return radius; }
}
