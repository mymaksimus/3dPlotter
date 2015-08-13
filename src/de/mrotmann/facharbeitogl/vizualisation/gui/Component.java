package de.mrotmann.facharbeitogl.vizualisation.gui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import de.mrotmann.facharbeitogl.vizualisation.RenderObject;
import de.mrotmann.facharbeitogl.vizualisation.gui.GuiHandler.MouseUpdateEvent;

public abstract class Component extends RenderObject {
	
	private boolean hasFocus;
	private Vector2f position, size;
	private PredefinedColor color;
	
	public Component(Vector2f position, Vector2f size){
		super(GuiHandler.shader);
		position.y *= -1;
		float data[] = new float[]{
			0, 0,
			0, -size.y,
			size.x, 0,
			size.x, -size.y
		};
		this.position = position;
		this.size = size;
		init(data, GuiHandler.attributes);
		getModel().translate(position);
		setDrawMode(GL11.GL_TRIANGLE_STRIP);
		revalidate();
		GuiHandler.registerMouseUpdateEvent(new MouseUpdateEvent(){
			public void mouseUp(int button, int mousex, int mousey, Vector2f scenePosition){}
			public void mouseDown(int button, int mousex, int mousey, Vector2f scenePosition){
				if(!Mouse.isGrabbed()){
					if(scenePosition.x >= position.x && scenePosition.x <= position.x + size.x){
						if(scenePosition.y >= -position.y && scenePosition.y <= -position.y + size.y){
							switchFocus();
						}
					}
				}
			}
		});
	}
	
	public void switchFocus(){
		if(hasFocus) requestLooseFocus();
		else requestFocus();
	}
	
	public void revalidate(){
		requestFocus();
		requestLooseFocus();		
	}
	
	public void requestFocus(){
		if(!hasFocus){
			this.hasFocus = true;
			setColor(PredefinedColor.WHITE);
			if(GuiHandler.focusedComponent != null) GuiHandler.focusedComponent.requestLooseFocus();
			GuiHandler.focusedComponent = this;
		}
	}
	
	public void requestLooseFocus(){
		if(hasFocus){
			setColor(PredefinedColor.WHITE_TRANSPARENT);
			this.hasFocus = false;
			GuiHandler.focusedComponent = null;
		}
	}
	
	public static void requestGlobalLooseFocus(){
		if(GuiHandler.focusedComponent != null){
			GuiHandler.focusedComponent.requestLooseFocus();
		}
	}
	
	public boolean hasFocus(){
		return hasFocus;
	}
	
	public PredefinedColor getColor(){
		return color;
	}
	
	public abstract void update();
	
	public void render(){
		updateColorUniform();
		super.render();
	}
	
	public void setColor(PredefinedColor color){
		this.color = color;
		updateColorUniform();
	}
	
	public Vector2f getPosition(){
		return position;
	}
	
	public Vector2f getSize(){
		return size;
	}
	
	private void updateColorUniform(){
		GuiHandler.shader.setUniform4f("color", color.r, color.g, color.b, color.a);
	}
}