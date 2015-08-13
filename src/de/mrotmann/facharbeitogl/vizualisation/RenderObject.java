package de.mrotmann.facharbeitogl.vizualisation;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

public class RenderObject implements Iterable<RenderObject> {
	
	private int vertexArrayObjectId;
	private int drawMode = GL11.GL_POINTS;
	private int vboRowSize;
	private Shader shader;
	private Texture texture;
	private Matrix4f model;
	private FloatBuffer modelBuffer;
	private ArrayList<RenderObject> subRenderObjects;
	private HashMap<String, Object> metaData;
	
	private static ArrayDeque<Matrix4f> matrixStack = new ArrayDeque<>();
	
	static {
		matrixStack.push(new Matrix4f());
	}
	
	public RenderObject(Shader shader){
		model = new Matrix4f();
		this.shader = shader;
		metaData = new HashMap<>();
	}
	
	public void putMetaData(String key, Object value){
		metaData.put(key, value);
	}
	
	public Object getMetaData(String key){
		return metaData.get(key);
	}
	
	public RenderObject(float verticies[], Shader shader, VertexAttribute... attributes){
		this(shader);
		init(verticies, attributes);
	}
	
	public RenderObject(FloatBuffer buffer, Shader shader, VertexAttribute... attributes){
		buffer.flip();
		init(buffer, shader, attributes);
	}
	
	public void init(float verticies[], VertexAttribute attributes[]){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(verticies.length);
		buffer.put(verticies);
		buffer.flip();
		init(buffer, attributes);
	}
	
	public void init(FloatBuffer buffer, VertexAttribute attributes[]){
		init(buffer, shader, attributes);
	}
	
	private void init(FloatBuffer buffer, Shader shader, VertexAttribute attributes[]){
		this.shader = shader;
		subRenderObjects = new ArrayList<>();
		model = new Matrix4f();
		modelBuffer = BufferUtils.createFloatBuffer(16);
		vertexArrayObjectId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vertexArrayObjectId);
		int vertexBufferObjectId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		
		int totalSize = 0, currentSize = 0;
		for(VertexAttribute attribute : attributes){
			totalSize += attribute.getSize();
		}
		for(VertexAttribute attribute : attributes){
			int attribLocation = GL20.glGetAttribLocation(shader.getId(), attribute.getName());
			GL20.glEnableVertexAttribArray(attribLocation);
			GL20.glVertexAttribPointer(attribLocation, attribute.getSize(), GL11.GL_FLOAT, false, totalSize * Float.SIZE / 8, currentSize * Float.SIZE / 8);
			currentSize += attribute.getSize();
		}
//		int attribLocation = GL20.glGetAttribLocation(shader.getId(), "vertexIn");
//		GL20.glEnableVertexAttribArray(attribLocation);
//		GL20.glVertexAttribPointer(attribLocation, 3, GL11.GL_FLOAT, false, 0 * Float.SIZE / 8, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		vboRowSize = buffer.capacity() / currentSize;
	}
	
	public void setModel(Matrix4f model){
		this.model = model;
	}
	
	public Matrix4f getModel(){
		return model;
	}
	
	public void setTexture(Texture texture){
		this.texture = texture;
	}
	
	public void setDrawMode(int drawMode){
		this.drawMode = drawMode;
	}
	
	public void addSubRenderObject(RenderObject object){
		subRenderObjects.add(object);
	}
	
	public void clearSubRenderObjects(){
		subRenderObjects.clear();
	}
	
	public void removeSubRenderObject(RenderObject object){
		subRenderObjects.remove(object);
	}
	
	public void render(){
		if(texture != null) texture.bind();
		Matrix4f matrix = new Matrix4f();
		Matrix4f.mul(matrixStack.peek(), getModel(), matrix);
		matrixStack.push(matrix);
		matrix.store(modelBuffer);
		modelBuffer.flip();
		shader.setUniformMat4(shader.getUniformId("model"), modelBuffer);
		GL20.glUseProgram(shader.getId());
		GL30.glBindVertexArray(vertexArrayObjectId);
		GL11.glDrawArrays(drawMode, 0, vboRowSize);
		for(RenderObject o : subRenderObjects){
			o.render();
		}
		matrixStack.pop();
	}

	public Iterator<RenderObject> iterator(){
		return subRenderObjects.iterator();
	}
}