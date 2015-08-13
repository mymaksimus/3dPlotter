package de.mrotmann.facharbeitogl.functionparser.seperations;

public enum MathematicFunctionType {
	
	COSINUS("cos", new MathematicFunctionEvaluation(){
		public Double evaluate(Double d1){
			return Math.cos(d1);
		}
	}),
	SINUS("sin", new MathematicFunctionEvaluation(){
		public Double evaluate(Double d1){
			return Math.sin(d1);
		}
	}),
	ABSOLUTE("abs", new MathematicFunctionEvaluation(){
		public Double evaluate(Double d1){
			return Math.abs(d1);
		}
	}),
	SQUARE_ROOT("sqrt", new MathematicFunctionEvaluation(){
		public Double evaluate(Double d1){
			return Math.sqrt(d1);
		}
	});
	
	private String operatorString;
	private MathematicFunctionEvaluation evaluation;
	
	private MathematicFunctionType(String functionString, MathematicFunctionEvaluation evaluation){
		this.operatorString = functionString;
		this.evaluation = evaluation;
	}
	
	public static MathematicFunctionType parseString(String operatorString){
		MathematicFunctionType types[] = MathematicFunctionType.values();
		for(MathematicFunctionType type : types){
			if(type.getFunctionString().equals(operatorString)) return type;
		}
		return null;
	}
	
	public Double evaluate(Double d1){
		return evaluation.evaluate(d1);
	}
	
	public String getFunctionString(){
		return operatorString;
	}
	
	static interface MathematicFunctionEvaluation {
		Double evaluate(Double d1);
	}
}
