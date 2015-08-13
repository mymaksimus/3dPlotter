package de.mrotmann.facharbeitogl.vizualisation.gui;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import de.mrotmann.facharbeitogl.vizualisation.RenderObject;
import de.mrotmann.facharbeitogl.vizualisation.fontrendering.TrueTypeFont;
import de.mrotmann.facharbeitogl.vizualisation.gui.GuiHandler.KeyUpdateEvent;

public class TextField extends Component {

	private String content;
	private TrueTypeFont font;
	private RenderObject textChars;
	private Pattern charFilter;
	
	private static float textScaleFactor = 1f / 3f;
	
	public TextField(TrueTypeFont font, Vector2f position, Vector2f size){
		super(position, size);
		this.font = font;
		content = "";
		setCharFilter("[a-zA-Z0-9*/+\\-.%()^]");
		GuiHandler.registerKeyUpdateEvent(new KeyUpdateEvent(){
			public void keyUp(int keyId, char keyChar){}
			public void keyDown(int keyId, char keyChar){
				if(hasFocus()){
					switch(keyId){
						case Keyboard.KEY_BACK:
							requestDeleteLastCharacter();
							break;
						case Keyboard.KEY_DELETE:
							clearSubRenderObjects();
							setContent("", false);
							break;
						default:
							char c = Keyboard.getEventCharacter();
							if(charFilter.matcher(String.valueOf(c)).matches()) 
								append(c);
							break;
					}
				}
			}
		});
	}
	
	private void requestDeleteLastCharacter(){
		if(content.length() > 0){
			deleteLastCharacter(this);
			setContent(content.substring(0, content.length() - 1), false);
		}		
	}
	
	private void createTextRenderObjects(){
		clearSubRenderObjects();
		textChars = font.createRenderObjects(content, false, true, GuiHandler.textShader, GuiHandler.textAttributes);
		addSubRenderObject(textChars);
		textChars.getModel().scale(new Vector3f(textScaleFactor, textScaleFactor, textScaleFactor));
		float diff = ((Float) textChars.getMetaData("fullwidth")) * textScaleFactor - getSize().x;
		if(diff < 0) diff = 0;
		textChars.getModel().translate(new Vector3f(-diff * (1f / textScaleFactor), 0, 0));
	}
	
	public void setCharFilter(String regex){
		charFilter = Pattern.compile(regex);
	}
	
	public void update(){
		
	}
	
	private boolean deleteLastCharacter(RenderObject object){
		Iterator<RenderObject> iterator = object.iterator();
		if(iterator.hasNext()){
			if(deleteLastCharacter(iterator.next())){
				object.clearSubRenderObjects();
			}
			return false;
		}
		return true;
	}
	
	public void setContent(String content, boolean createObjects){
		this.content = content;
		if(createObjects) createTextRenderObjects();
	}
	
	public void append(char c){
		append(String.valueOf(c));
	}
	
	public void append(String s){
		setContent(content + s, true);
	}
	
	public String getContent(){
		return content;
	}
}
