package de.mrotmann.facharbeitogl.vizualisation.fontrendering;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import de.mrotmann.facharbeitogl.vizualisation.RenderObject;
import de.mrotmann.facharbeitogl.vizualisation.Shader;
import de.mrotmann.facharbeitogl.vizualisation.Texture;
import de.mrotmann.facharbeitogl.vizualisation.VertexAttribute;

public class TrueTypeFont {
	
	private HashMap<Character, TtfCharacter> characters;
	private BufferedImage fontImage;
	private Texture fontTexture;
	
	public TrueTypeFont(Font font, boolean antialiasing, Shader shader, VertexAttribute... attributes){
		createFontMap(font, antialiasing);
	}
	
	public void createFontMap(Font font, boolean antialiasing){
		characters = new HashMap<>();
		fontImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = fontImage.createGraphics();
		if(antialiasing) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(font.deriveFont(Font.BOLD));
		g2d.setColor(Color.RED);
		FontMetrics metrics = g2d.getFontMetrics();
		int x = 0, y = 0;
		for(int i = 0; i < 255; i++){
			char current = (char) i;
			int width = metrics.charWidth(current) + 2, height = metrics.getHeight();
			TtfCharacter ttfChar = new TtfCharacter(x, y, width, height, font.getSize2D() * 0.06f);
			characters.put(current, ttfChar);
			g2d.drawString(String.valueOf(current), x + 1, y + metrics.getAscent());
			x += width;
			if(x + metrics.charWidth((char) (i + 1)) > fontImage.getWidth()){
				x = 0;
				y += height;
			}
		}
		fontTexture = new Texture(fontImage, antialiasing);

//		File f = new File("C:\\Users\\SkySoldier\\Desktop\\map.png");
//		try{
//			f.createNewFile();
//			ImageIO.write(fontImage, "png", f);
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
	}
	
	public RenderObject createRenderObjects(String s, boolean is3d, boolean zOffset2d, Shader shader, VertexAttribute attributes[]){
		int x = 0;
		RenderObject rootObject = null, currentObject = null;
		float lastCharWidth = 0;
		for(char c : s.toCharArray()){
			TtfCharacter ttfChar = characters.get(c);
			float sw = ttfChar.getWidth(), sh = ttfChar.getHeight();
			float stFactor = 1.0f / fontImage.getWidth();
			float stLeft = ttfChar.getMapx() * stFactor;
			float stRight = stLeft + ttfChar.getWidth() * stFactor;
			float stTop = ttfChar.getMapy() * stFactor;
			float stBottom = stTop + ttfChar.getHeight() * stFactor;
			float quadData[];
			if(is3d){
				quadData = new float[]{
					0, 0, 0,			stLeft, stTop,	
					0, -sh, 0,			stLeft, stBottom,
					sw, 0, 0,		stRight, stTop,
					sw, -sh, 0,		stRight, stBottom
				};
			}
			else {
				quadData = new float[]{
					0, 0,			stLeft, stTop,	
					0, -sh,			stLeft, stBottom,
					sw, 0,		stRight, stTop,
					sw, -sh,	stRight, stBottom
				};
			}
//			System.out.println(stLeft + ", " + stRight + ", " + stTop + ", " + stBottom);
			x += sw + ttfChar.getSpacingx();
			RenderObject object = new RenderObject(quadData, shader, attributes);
			if(zOffset2d) object.getModel().translate(new Vector3f(0, 0, 0.1f));
			object.getModel().translate(new Vector3f(lastCharWidth, 0, 0));
			lastCharWidth = sw;
			object.setDrawMode(GL11.GL_TRIANGLE_STRIP);
			object.setTexture(fontTexture);
			if(rootObject == null){
				rootObject = object;
				currentObject = object;
			}
			else {
				currentObject.addSubRenderObject(object);
				currentObject = object;
			}
		}
		rootObject.putMetaData("fullwidth", (float) x);
		return rootObject;
	}
	
	public BufferedImage getFontImage(){
		return fontImage;
	}
	
	class TtfCharacter {
		
		private int mapx;
		private int mapy;
		private int width;
		private int height;
		private float spacingx;
		
		public TtfCharacter(int mapx, int mapy, int width, int height, float spacingx){
			this.mapx = mapx;
			this.mapy = mapy;
			this.width = width;
			this.height = height;
			this.spacingx = spacingx;
		}
		
		public int getMapx(){
			return mapx;
		}
		
		public int getMapy(){
			return mapy;
		}
		
		public int getWidth(){
			return width;
		}
		
		public int getHeight(){
			return height;
		}
		
		public float getSpacingx(){
			return spacingx;
		}
	}
}
