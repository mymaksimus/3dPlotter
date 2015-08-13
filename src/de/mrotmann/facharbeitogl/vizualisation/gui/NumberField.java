package de.mrotmann.facharbeitogl.vizualisation.gui;

import org.lwjgl.util.vector.Vector2f;

import de.mrotmann.facharbeitogl.vizualisation.fontrendering.TrueTypeFont;

public class NumberField extends TextField {

	public NumberField(TrueTypeFont font, Vector2f position, Vector2f size){
		super(font, position, size);
		setCharFilter("[0-9.,-]");
		setContent("-1,1", true);
	}
	
	public float[] getValues(){
		String parts[] = getContent().split(",");
		float values[] = new float[parts.length];
		for(int i = 0; i < parts.length; i++){
			values[i] = Float.parseFloat(parts[i]);
		}
		return values;
	}
}
