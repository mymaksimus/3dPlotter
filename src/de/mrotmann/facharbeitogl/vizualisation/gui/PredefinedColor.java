package de.mrotmann.facharbeitogl.vizualisation.gui;

import org.lwjgl.util.vector.Vector4f;

public class PredefinedColor extends Vector4f {
	
	public static PredefinedColor WHITE = new PredefinedColor(1, 1, 1, 1);
	public static PredefinedColor WHITE_TRANSPARENT = new PredefinedColor(1, 1, 1, 0.5f);
	
	public float r = x, g = y, b = z, a = w;
	
	public PredefinedColor(float r, float g, float b, float a){
		super(r, g, b, a);
	}
}
