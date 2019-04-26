package EngineTester;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.RawModel;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
import terrains.Terrain;
import textures.ModelTexture;

public class MainGameLoop {

	public static void main(String[] args) {
		
		
		DisplayManager.createDisplay();
		Loader loader = new Loader();

		
		RawModel model = OBJLoader.loadObjModel("cup", loader);
		ModelTexture texture = new ModelTexture(loader.loadTexture("cuptexture"));
		TexturedModel texturedModel = new TexturedModel(model,texture);
		
		TexturedModel tree = new TexturedModel( OBJLoader.loadObjModel("tree", loader), new ModelTexture(loader.loadTexture("tree")));
		TexturedModel grass = new TexturedModel( OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUseFakeLighting(true);
		
		TexturedModel fern = new TexturedModel( OBJLoader.loadObjModel("fern", loader), new ModelTexture(loader.loadTexture("fern")));
		fern.getTexture().setHasTransparency(true);
		fern.getTexture().setUseFakeLighting(true);
		
		texture.setShineDamper(30);
		texture.setReflectivity(0.2f);
		
		Entity cup = new Entity(texturedModel, new Vector3f(0,0,-25),0,0,0,50);
		Light light =  new Light(new Vector3f(20000,20000,2000), new Vector3f(1,1,1));
		
		Terrain terrain = new Terrain(0,-1, loader, new ModelTexture(loader.loadTexture("grass")));
		Terrain terrain2 = new Terrain(-1,-1, loader, new ModelTexture(loader.loadTexture("grass")));

		
		Camera camera = new Camera();
		
		MasterRenderer renderer = new MasterRenderer();
		
		Random random = new Random();
		List<Entity> entities = new ArrayList<Entity>();
	
		for(int i = 0; i < 200; i++){
			entities.add(new Entity(tree, new Vector3f(random.nextFloat() * 800 - 400, 0,
					random.nextFloat() * -600), 0,0,0,3));
			entities.add(new Entity(grass, new Vector3f(random.nextFloat() * 800 - 400, 0,
					random.nextFloat() * -600), 0,0,0,1));
			entities.add(new Entity(fern, new Vector3f(random.nextFloat() * 800 - 400, 0,
					random.nextFloat() * -600), 0,0,0, 0.25f));
		}
		
		
		while(!Display.isCloseRequested()){
			
			
			for(Entity entity:entities){
                renderer.processEntity(entity);
            }
			cup.increasePosition(0, 0, -0.1f);
			camera.move();
			cup.increaseRotation(1,0,0);
			//game logic
			renderer.processTerrain(terrain2);
			renderer.processTerrain(terrain);
			renderer.processEntity(cup);
			renderer.render(light, camera);
			DisplayManager.updateDisplay();
			
			
		}

//		renderer.cleanUp();
//		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
