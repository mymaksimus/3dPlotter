package de.mrotmann.facharbeitogl.functionparser.seperations.parts;

import de.mrotmann.facharbeitogl.functionparser.seperations.MathematicFunctionType;

public class MathematicFunction extends AbstractFunctionPart<MathematicFunctionType> {
	
	public MathematicFunction(MathematicFunctionType type){
		super(type);
		setName("MathFunc");
	}
	
	public MathematicFunction(String functionString){
		this(MathematicFunctionType.parseString(functionString));
	}
}
