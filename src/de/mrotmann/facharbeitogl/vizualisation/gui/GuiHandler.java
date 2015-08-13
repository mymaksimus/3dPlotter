package de.mrotmann.facharbeitogl.vizualisation.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import de.mrotmann.facharbeitogl.vizualisation.Shader;
import de.mrotmann.facharbeitogl.vizualisation.VertexAttribute;
import de.mrotmann.facharbeitogl.vizualisation.VizualisationOpenGl;

public class GuiHandler {
	
	public static Shader shader, textShader;
	public static VertexAttribute attributes[], textAttributes[];
	public static Component focusedComponent;
	private static Matrix4f projectionMatrix;
	private static IntBuffer viewPort;
	private static FloatBuffer worldPosition;
	
	static {
		String vertexShaderFileName = VizualisationOpenGl.getShaderDirection("gui/component_vertex.shader");
		String fragmentShaderFileName = VizualisationOpenGl.getShaderDirection("gui/component_fragment.shader");
		String textVertexShaderFileName = VizualisationOpenGl.getShaderDirection("gui/text_vertex.shader");
		String textFragmentShaderFileName = VizualisationOpenGl.getShaderDirection("gui/text_fragment.shader");
		shader = new Shader(vertexShaderFileName, fragmentShaderFileName);
		textShader = new Shader(textVertexShaderFileName, textFragmentShaderFileName);
		attributes = new VertexAttribute[]{
			new VertexAttribute("vertexIn", 2)	
		};
		textAttributes = new VertexAttribute[]{
			attributes[0],
			new VertexAttribute("texCoords", 2)
		};
		shader.loadUniform("projection");
		shader.loadUniform("camera");
		shader.loadUniform("model");
		shader.loadUniform("color");
		textShader.loadUniform("projection");
		textShader.loadUniform("camera");
		textShader.loadUniform("model");
		
		projectionMatrix = VizualisationOpenGl.getOrthogonalProjectionMatrix(100, 100 * VizualisationOpenGl.getDisplayAspect(), 0.1f, 1000.0f);
		Matrix4f cameraMatrix = new Matrix4f();
		shader.setUniformMat4("projection", VizualisationOpenGl.matrixToBuffer(projectionMatrix));
		shader.setUniformMat4("camera", VizualisationOpenGl.matrixToBuffer(cameraMatrix));
		textShader.setUniformMat4("projection", VizualisationOpenGl.matrixToBuffer(projectionMatrix));
		textShader.setUniformMat4("camera", VizualisationOpenGl.matrixToBuffer(cameraMatrix));
	
		viewPort = BufferUtils.createIntBuffer(4);
		viewPort.put(0);
		viewPort.put(0);
		viewPort.put(800);
		viewPort.put(600);
		viewPort.flip();
		
		worldPosition = BufferUtils.createFloatBuffer(3);
	}
	
	public static Vector3f unProject(int winx, int winy){
		GLU.gluUnProject(winx, winy, 0, 
				VizualisationOpenGl.matrixToBuffer(new Matrix4f()), 
				VizualisationOpenGl.matrixToBuffer(projectionMatrix), 
				viewPort, worldPosition);
		Vector3f worldPoint = new Vector3f(worldPosition.get(), -worldPosition.get(), worldPosition.get());
		worldPosition.flip();
		return worldPoint;
	}
	
	private static ArrayList<KeyUpdateEvent> keyEvents;
	private static ArrayList<MouseUpdateEvent> mouseEvents;
	
	static {
		keyEvents = new ArrayList<>();
		mouseEvents = new ArrayList<>();
	}
	
	public static void registerKeyUpdateEvent(KeyUpdateEvent event){
		keyEvents.add(event);
	}
	
	public static void registerMouseUpdateEvent(MouseUpdateEvent event){
		mouseEvents.add(event);
	}
	
	public static void updateInputDevices(){
		while(Mouse.next()){
			int button = Mouse.getEventButton();
			int mousex = Mouse.getEventX();
			int mousey = Mouse.getEventY();
			Vector3f unprojected = unProject(mousex, mousey);
			Vector2f unprojected2d = new Vector2f(unprojected.x, unprojected.y);
			if(Mouse.getEventButtonState()){
				for(MouseUpdateEvent event : mouseEvents) event.mouseDown(button, mousex, mousey, unprojected2d);
			}
			else {
				for(MouseUpdateEvent event : mouseEvents) event.mouseUp(button, mousex, mousey, unprojected2d);
			}
		}
		while(Keyboard.next()){
			int keyId = Keyboard.getEventKey();
			char keyChar = Keyboard.getEventCharacter();
			if(Keyboard.getEventKeyState()){
				for(KeyUpdateEvent event : keyEvents) event.keyDown(keyId, keyChar); 
			}
			else {
				for(KeyUpdateEvent event : keyEvents) event.keyUp(keyId, keyChar); 
			}
		}
	}
	
	public interface KeyUpdateEvent {
		void keyDown(int keyId, char keyChar);
		void keyUp(int keyId, char keyChar);
	}
	
	public interface MouseUpdateEvent {
		void mouseDown(int button, int mousex, int mousey, Vector2f scenePosition);
		void mouseUp(int button, int mousex, int mousey, Vector2f scenePosition);
	}
}