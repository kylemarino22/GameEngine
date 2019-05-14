package EngineTester;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entities.PhysicsEntity;
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

		
		RawModel model = OBJLoader.loadObjModel("block", loader, true);
		ModelTexture texture = new ModelTexture(loader.loadTexture("drock079"));
		TexturedModel texturedModel = new TexturedModel(model,texture);
//		texturedModel.getTexture().setUseFakeLighting(true);
		
		TexturedModel tree = new TexturedModel( OBJLoader.loadObjModel("tree", loader, false), new ModelTexture(loader.loadTexture("tree")));
		TexturedModel grass = new TexturedModel( OBJLoader.loadObjModel("grassModel", loader, false), new ModelTexture(loader.loadTexture("grassTexture")));
		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUseFakeLighting(true);
		
		TexturedModel fern = new TexturedModel( OBJLoader.loadObjModel("fern", loader, false), new ModelTexture(loader.loadTexture("fern")));
		fern.getTexture().setHasTransparency(true);
		fern.getTexture().setUseFakeLighting(true);
		
		texture.setShineDamper(30);
		texture.setReflectivity(0.2f);
		
		PhysicsEntity cup = new PhysicsEntity(texturedModel, new Vector3f(0,0,-25),0,0,0,1);

		cup.acceleration.y = 0;
		cup.velocity.y = 0;
		cup.velocity.x = 0;
		cup.omega.x = 0;
		cup.alpha.x = 0;

		Light light =  new Light(new Vector3f(-10000,20000,10000), new Vector3f(1,1,1));
		
		Terrain terrain = new Terrain(0,-1, loader, new ModelTexture(loader.loadTexture("grass")));
		Terrain terrain2 = new Terrain(-1,-1, loader, new ModelTexture(loader.loadTexture("grass")));

		
		Camera camera = new Camera();
		
		MasterRenderer renderer = new MasterRenderer();
		
		Random random = new Random();
		List<Entity> entities = new ArrayList<>();
	
		for (int i = 0; i < 200; i++) {
			entities.add(new Entity(tree, new Vector3f(random.nextFloat() * 800 - 400, 0,
					random.nextFloat() * -600), 0,0,0,3));
			entities.add(new Entity(grass, new Vector3f(random.nextFloat() * 800 - 400, 0,
					random.nextFloat() * -600), 0,0,0,1));
			entities.add(new Entity(fern, new Vector3f(random.nextFloat() * 800 - 400, 0,
					random.nextFloat() * -600), 0,0,0, 0.25f));
		}

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				entities.add(new Entity(texturedModel, new Vector3f(i* 20, 0, -j*20), 0, 0, 0,0.5f));
			}
		}


		long time = System.currentTimeMillis();
		long delta_t = 0;
		while(!Display.isCloseRequested()){
			
			
			for(Entity entity:entities){
                renderer.processEntity(entity);
            }
//			cup.increasePosition(0, 0, 0.0f);
			camera.move();
//			cup.increaseRotation(1,0,0);
			//game logic
			renderer.processTerrain(terrain2);
			renderer.processTerrain(terrain);
			renderer.processEntity(cup);
			renderer.render(light, camera);
			DisplayManager.updateDisplay();

			cup.calculatePhysics((float) delta_t/1000f);

			delta_t = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();

			
		}

//		renderer.cleanUp();
//		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}