package de.mrotmann.facharbeitogl.functionparser.seperations.parts;


public abstract class AbstractFunctionPart<T> implements FunctionPart<T> {
	
	private T value;
	private String name;
	
	public AbstractFunctionPart(T value){
		this.value = value;
		this.name = "FunctionPart";
	}
	
	public String toString(){
		return "[" + name + "]{" + getValue() + "}";
	}
	
	public void setValue(T value){
		this.value = value;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public T getValue(){
		return value;
	}
}