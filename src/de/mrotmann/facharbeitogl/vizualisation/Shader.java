package de.mrotmann.facharbeitogl.vizualisation;

import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
	
	private int id;
	private static int current;
	private HashMap<String, Integer> uniformMap;
	
	public Shader(String vertexShaderFileName, String fragmentShaderFileName){
		uniformMap = new HashMap<>();
		id = GL20.glCreateProgram();
		int vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		String vertexShaderCode = VizualisationOpenGl.readResource(vertexShaderFileName);
		GL20.glShaderSource(vertexShaderId, vertexShaderCode);
		GL20.glCompileShader(vertexShaderId);
		int fragmentShaderId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		String fragmentShaderCode = VizualisationOpenGl.readResource(fragmentShaderFileName);
		GL20.glShaderSource(fragmentShaderId, fragmentShaderCode);
		GL20.glCompileShader(fragmentShaderId);
		GL20.glAttachShader(id, vertexShaderId);
		GL20.glAttachShader(id, fragmentShaderId);
		GL20.glLinkProgram(id);
		int linkStatus = GL20.glGetProgrami(id, GL20.GL_LINK_STATUS);
		if(linkStatus == GL11.GL_FALSE){
			int logLength = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
			System.out.println(GL20.glGetProgramInfoLog(id, logLength));
		}
	}
	
	public int getId(){
		return id;
	}
	
	public void bind(){
		if(id != current){
			GL20.glUseProgram(id);
			current = id;
		}
	}
	
	public static void unbind(){
		current = 0;
		GL20.glUseProgram(0);
	}
	
	public void loadUniform(String name){
		bind();
		uniformMap.put(name, GL20.glGetUniformLocation(id, name));
	}
	
	public int getUniformId(String name){
		return uniformMap.get(name);
	}
	
	public void setUniform1f(int uniformId, float f){
		bind();
		GL20.glUniform1f(uniformId, f);
	}
	
	public void setUniform4f(String s, float f0, float f1, float f2, float f3){
		setUniform4f(getUniformId(s), f0, f1, f2, f3);
	}
	
	public void setUniform4f(int uniformId, float f0, float f1, float f2, float f3){
		bind();
		GL20.glUniform4f(uniformId, f0, f1, f2, f3);
	}
	
	public void setUniformMat4(String name, FloatBuffer matrix){
		setUniformMat4(getUniformId(name), matrix);
	}
	
	public void setUniformMat4(int uniformId, FloatBuffer matrix){
		bind();
		GL20.glUniformMatrix4(uniformId, false, matrix);
	}
}
