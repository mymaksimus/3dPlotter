package de.mrotmann.facharbeitogl.vizualisation;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import de.mrotmann.facharbeitogl.functionparser.FunctionParser;
import de.mrotmann.facharbeitogl.vizualisation.fontrendering.TrueTypeFont;
import de.mrotmann.facharbeitogl.vizualisation.gui.Component;
import de.mrotmann.facharbeitogl.vizualisation.gui.GuiHandler;
import de.mrotmann.facharbeitogl.vizualisation.gui.GuiHandler.KeyUpdateEvent;
import de.mrotmann.facharbeitogl.vizualisation.gui.GuiHandler.MouseUpdateEvent;
import de.mrotmann.facharbeitogl.vizualisation.gui.NumberField;
import de.mrotmann.facharbeitogl.vizualisation.gui.TextField;

public class VizualisationOpenGl {
	
	private Shader functionShader, lineShader, textShader;
	private FloatBuffer cameraUniformBuffer = BufferUtils.createFloatBuffer(16);
	private VertexAttribute attributes[];
	
	private RenderObject rootRenderObject;
	private RenderObject wireframeRenderObject;
	private ArrayList<RenderObject> functionRenderObjectLines;
	private HashMap<Character, Double> variableValues;
	
	private TrueTypeFont font;
	private FunctionView view;
	
	private static float displayAspect;
	
	public VizualisationOpenGl() throws Exception {
		createDisplay();
		createShader();
		createRenderObjects();
		createPerspectiveProjectionMatrix(60f, 0.1f, 1000f);
		drawLoop();
	}
	
	private void createDisplay() throws Exception {
		PixelFormat format = new PixelFormat();
		ContextAttribs attribs = new ContextAttribs(3, 0);
		DisplayMode mode = new DisplayMode(800, 600);
		displayAspect = 800.0f / 600.0f;
		Display.setDisplayMode(mode);
		Display.create(format, attribs);
	}
	
	private void createShader(){
		String vertexShaderFileName = getShaderDirection("vertex.shader");
		String fragmentShaderFileName = getShaderDirection("fragment.shader");
		String basicFragmentShaderFileName = getShaderDirection("basic_fragment.shader");
		String textVertexShaderFileName = getShaderDirection("text_vertex.shader");
		String textFragmentShaderFileName = getShaderDirection("text_fragment.shader");
		functionShader = new Shader(vertexShaderFileName, fragmentShaderFileName);
		lineShader = new Shader(vertexShaderFileName, basicFragmentShaderFileName);
		textShader = new Shader(textVertexShaderFileName, textFragmentShaderFileName);
		functionShader.loadUniform("projection");
		functionShader.loadUniform("camera");
		functionShader.loadUniform("model");
		functionShader.loadUniform("boxSize");
		lineShader.loadUniform("projection");
		lineShader.loadUniform("camera");
		lineShader.loadUniform("model");
		lineShader.loadUniform("color");
		textShader.loadUniform("projection");
		textShader.loadUniform("camera");
		textShader.loadUniform("model");
		textShader.loadUniform("color");
		attributes = new VertexAttribute[]{
			new VertexAttribute("vertexIn", 3),
			new VertexAttribute("texCoords", 2)
		};
	}
	
	private void createRenderObjects(){
		view = new FunctionView(10, 10, 10, 100);
		rootRenderObject = view.createMainRaster(lineShader, attributes[0]);
		variableValues = new HashMap<>();
		variableValues.put('x', 0d);
		variableValues.put('z', 0d);
		variableValues.put('e', Math.E);
		String functionString = "(sin(x)+sin(z))*(0.3(x+z))";
		FunctionParser parser = new FunctionParser(functionString);
		createFunctionRenderObjects(variableValues, parser);
		//Font f = new Font("Courier New", Font.BOLD, 30);
		try{
			Font f = Font.createFont(Font.TRUETYPE_FONT, getResource("vizualisation/fonts/COURBD.TTF")).deriveFont(30.0f);
			font = new TrueTypeFont(f, true, textShader);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void createFunctionRenderObjects(HashMap<Character, Double> variableValues, FunctionParser parser){
		ArrayList<RenderObject> newFunctionRenderObjectLines = view.createFunction(variableValues, parser, functionShader, attributes[0]);
		RenderObject newWireframeRenderObject = view.createWireframe(lineShader, attributes[0]);
		if(wireframeRenderObject != null){
			for(RenderObject r : functionRenderObjectLines){
				rootRenderObject.removeSubRenderObject(r);
			}
			rootRenderObject.removeSubRenderObject(wireframeRenderObject);
		}
		for(RenderObject r : newFunctionRenderObjectLines){
			rootRenderObject.addSubRenderObject(r);
		}
		rootRenderObject.addSubRenderObject(newWireframeRenderObject);
		functionRenderObjectLines = newFunctionRenderObjectLines;
		wireframeRenderObject = newWireframeRenderObject;
	}
	
	private void createPerspectiveProjectionMatrix(float fov, float near, float far){
		double f = (1.0 / Math.tan(Math.toRadians(fov * 0.5f)));
		float aspect = Display.getWidth() / (float) Display.getHeight();
		Matrix4f projection = new Matrix4f();
		projection.m00 = (float) (f / aspect);
        projection.m11 = (float) f;
        projection.m22 = (far + near) / (near - far);
        projection.m23 = -1;
        projection.m32 = (2 * far + near) / (near - far);
        projection.m33 = 0;
        FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
        projection.store(projectionBuffer);
        projectionBuffer.flip();
        functionShader.setUniformMat4(functionShader.getUniformId("projection"), projectionBuffer);
        lineShader.setUniformMat4(lineShader.getUniformId("projection"), projectionBuffer);
        textShader.setUniformMat4(textShader.getUniformId("projection"), projectionBuffer);
    }
	
	private void lookAt(Vector3f eye, Vector3f at, Vector3f up){
		Vector3f forward = new Vector3f();
        Vector3f side = new Vector3f();
        Vector3f.sub(at, eye, forward);
        forward.normalise();
        Vector3f.cross(forward, up, side);
        side.normalise();
        Vector3f upCopy = new Vector3f(up);
        Vector3f.cross(side, forward, upCopy);
        Matrix4f lookAt = new Matrix4f();
        lookAt.m00 = side.x;
        lookAt.m01 = side.y;
        lookAt.m02 = side.z;
        lookAt.m10 = upCopy.x;
        lookAt.m11 = upCopy.y;
        lookAt.m12 = upCopy.z;
        lookAt.m20 = -forward.x;
        lookAt.m21 = -forward.y;
        lookAt.m22 = -forward.z;
        lookAt.transpose();
        lookAt.translate(new Vector3f(-eye.x, -eye.y, -eye.z));
        lookAt.store(cameraUniformBuffer);
        cameraUniformBuffer.flip();
        functionShader.setUniformMat4(functionShader.getUniformId("camera"), cameraUniformBuffer);
        lineShader.setUniformMat4(lineShader.getUniformId("camera"), cameraUniformBuffer);
        textShader.setUniformMat4(textShader.getUniformId("camera"), cameraUniformBuffer);
	}
	
	public static String getShaderDirection(String name){
		return "vizualisation/shader/" + name;
	}
	
	private void drawLoop(){
		int boxSize = 100;
		float distanceEye = boxSize * 2;
		Vector3f eye = new Vector3f(boxSize * 0.5f, boxSize * 0.5f, distanceEye), at = new Vector3f(boxSize * 0.5f, boxSize * 0.5f, boxSize * 0.5f), up = new Vector3f(0, 1, 0);
		lookAt(eye, at, up);
		long currentFrame, lastFrame = System.nanoTime();
		float deltaSeconds;
		double timer = 0;
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glPolygonOffset(1, 1);
		GL11.glLineWidth(1);
		GL11.glPointSize(1);
		Mouse.setGrabbed(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LESS);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Vector3f xaxis = new Vector3f(1, 0, 0), yaxis = new Vector3f(0, 1, 0);
		float anglex = 0, angley = 0;
		ArrayList<Component> guiElements = new ArrayList<>();
		TextField functionTextField = new TextField(font, new Vector2f(-100 * getDisplayAspect(), -100), new Vector2f(200 * getDisplayAspect(), 10));
		NumberField rangeFieldX = new NumberField(font, new Vector2f(-100 * getDisplayAspect() + 10, -80), new Vector2f(30, 10));
		NumberField rangeFieldY = new NumberField(font, new Vector2f(-100 * getDisplayAspect() + 10, -60), new Vector2f(30, 10));
		NumberField rangeFieldZ = new NumberField(font, new Vector2f(-100 * getDisplayAspect() + 10, -40), new Vector2f(30, 10));
		guiElements.add(functionTextField);
		guiElements.add(rangeFieldX);
		guiElements.add(rangeFieldY);
		guiElements.add(rangeFieldZ);
		GuiHandler.registerKeyUpdateEvent(new KeyUpdateEvent(){
			public void keyUp(int keyId, char keyChar){}
			public void keyDown(int keyId, char keyChar){
				if(keyId == Keyboard.KEY_RETURN){
					float xValues[] = rangeFieldX.getValues();
					float yValues[] = rangeFieldY.getValues();
					float zValues[] = rangeFieldZ.getValues();
					view.setValues(xValues[1], xValues[0], yValues[1], yValues[0], zValues[1], zValues[0]);
					createFunctionRenderObjects(variableValues, new FunctionParser(functionTextField.getContent()));
					Component.requestGlobalLooseFocus();
					Mouse.setGrabbed(true);
				}
				else if(keyId == Keyboard.KEY_ESCAPE){
					Mouse.setGrabbed(!Mouse.isGrabbed());
					Component.requestGlobalLooseFocus();
				}
			}
		});
		GuiHandler.registerMouseUpdateEvent(new MouseUpdateEvent(){
			public void mouseDown(int button, int mousex, int mousey, Vector2f scenePosition){

			}
			public void mouseUp(int button, int mousex, int mousey, Vector2f scenePosition){
			
			}
		});
		while(!Display.isCloseRequested()){
			currentFrame = System.nanoTime();
			deltaSeconds = (currentFrame - lastFrame) / 1e9f;
			lastFrame = currentFrame;
			timer += deltaSeconds;
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glClearColor(0, 0, 0, 1);
			if(Mouse.isGrabbed()){
			boolean rot = Mouse.isButtonDown(0);
				float rotatey = (float) Math.toRadians(360) * deltaSeconds * (0 + 0.1f * Mouse.getDX());
				float rotatex = (float) Math.toRadians(360) * deltaSeconds * (0 + 0.1f * Mouse.getDY());
				if(rot){
					angley += rotatey;
					anglex += -rotatex;
				}
			}
			int wheel = Mouse.getDWheel();
			if(wheel != 0){
				float change = wheel * deltaSeconds * 300;
				eye.z -= change;
//				if(eye.z < 100) eye.z = 100;
//				else if(eye.z > 300) eye.z = 300;
//				System.out.println(eye.z);
				lookAt(eye, at, up);
			}
			Matrix4f newRootModel = new Matrix4f();
			newRootModel.setIdentity();
			newRootModel.translate(new Vector3f(boxSize * 0.5f, boxSize * 0.5f, boxSize * 0.5f));
			newRootModel.rotate(anglex, xaxis);
			newRootModel.rotate(angley, yaxis);
			newRootModel.translate(new Vector3f(-boxSize * 0.5f, -boxSize * 0.5f, -boxSize * 0.5f));
			rootRenderObject.setModel(newRootModel);
			rootRenderObject.render();
			GuiHandler.updateInputDevices();
			for(Component c : guiElements){
				c.update();
				c.render();
			}
			if(timer >= 0.25){
				Display.setTitle("fps: " + 1.0d / deltaSeconds);
				timer = timer - 0.25;
			}
//			Display.sync(120);
			Display.update();
		}
	}
	
	public static String readResource(String resourceName){
		BufferedReader reader = new BufferedReader(new InputStreamReader(VizualisationOpenGl.class.getClassLoader().getResourceAsStream(resourceName)));
		String line;
		StringBuilder content = new StringBuilder();
		String separator = System.getProperty("line.separator");
		try{
			while((line = reader.readLine()) != null){
				content.append(line + separator);
			}
			return content.toString();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public InputStream getResource(String name){
		return getClass().getClassLoader().getResourceAsStream(name);
	}
	
	public static float getDisplayAspect(){
		return displayAspect;
	}
	
	public static Matrix4f getOrthogonalProjectionMatrix(float top, float right, float near, float far){
		Matrix4f m = new Matrix4f();
		m.m00 = 2 / (right - -right);
        m.m03 = -(right + -right) / (right - -right);
        m.m11 = 2 / (top - -top);  
        m.m13 = -(top + -top) / (top - -top);
        m.m22 = -2 / (far - near);
        return m;
	}
	
	public static FloatBuffer matrixToBuffer(Matrix4f matrix){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		matrix.store(buffer);
		buffer.flip();
		return buffer;
	}
	
	public static void main(String[] args){
		try{
			new VizualisationOpenGl();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}


//String functionString = "10*((0.2*sin(5x)+abs(x)^0.5)+(0.2*sin(5z)+abs(z)^0.5))";
//String functionString = "sqrt(100-x^2-z^2)";													