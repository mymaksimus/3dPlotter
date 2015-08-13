package de.mrotmann.facharbeitogl.vizualisation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Texture {
		
	private int id;
	private static int current;
	
	public Texture(BufferedImage image, boolean linearMapping){
		init(image, linearMapping);
	}
	
	private void init(BufferedImage image, boolean linearMapping){
		id = GL11.glGenTextures();
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(data.length);
        byteBuffer.put(data);
        byteBuffer.flip();
        bind();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        int mapping = linearMapping ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mapping);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mapping);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
	}

	public void bind(){
		if(id != current){
			current = id; 
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		}
	}
	
	public static void unbind(){
		current = 0;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}
