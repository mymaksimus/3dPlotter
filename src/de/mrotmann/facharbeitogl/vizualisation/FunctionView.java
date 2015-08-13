package de.mrotmann.facharbeitogl.vizualisation;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import de.mrotmann.facharbeitogl.functionparser.FunctionParser;

public class FunctionView {
	
	private int boxSize;
	private float maxx;
	private float minx;
	private float maxy;
	private float miny;
	private float maxz;
	private float minz;
	private float xValueFactor;
	private float zValueFactor;
	private Vector3f functionPointRows[][];
	
	public FunctionView(float maxx, float maxy, float maxz, int boxSize){
		this(maxx, -maxx, maxy, -maxy, maxz, -maxz, boxSize);
	}
	
	public FunctionView(float maxx, float minx, float maxy, float miny, float maxz, float minz, int boxSize){
		this.boxSize = boxSize;
		setValues(maxx, minx, maxy, miny, maxz, minz);
	}
	
	public RenderObject createMainRaster(Shader lineShader, VertexAttribute... attributes){
		float rasterVerticies[] = new float[]{
			0, 0, 0, 0, boxSize, 0,
			boxSize, 0, 0, boxSize, boxSize, 0,
			boxSize, 0, boxSize, boxSize, boxSize, boxSize,
			0, 0, boxSize, 0, boxSize, boxSize,
			0, 0, 0, boxSize, 0, 0,
			0, 0, 0, 0, 0, boxSize,
			boxSize, 0, boxSize, 0, 0, boxSize,
			boxSize, 0, boxSize, boxSize, 0, 0,
			0, boxSize, 0, boxSize, boxSize, 0,
			0, boxSize, 0, 0, boxSize, boxSize,
			boxSize, boxSize, boxSize, 0, boxSize, boxSize,
			boxSize, boxSize, boxSize, boxSize, boxSize, 0
		};
		RenderObject raster = new RenderObject(rasterVerticies, lineShader, attributes);
		raster.setDrawMode(GL11.GL_LINES);
		return raster;
	}
	
	public ArrayList<RenderObject> createFunction(HashMap<Character, Double> variableValues, FunctionParser parser, Shader functionShader, VertexAttribute... attributes){
		ArrayList<RenderObject> objects = new ArrayList<>();
		boxSize++;
		functionPointRows = new Vector3f[boxSize][boxSize];
		float maxyv = Float.MIN_VALUE;
		for(int gridx = 0; gridx < boxSize; gridx++){
			Vector3f[] verticiesRow = new Vector3f[boxSize];
			for(int gridz = 0; gridz < boxSize; gridz++){
				float xCurrentValue = minx + gridx * xValueFactor;
				float zCurrentValue = minz + gridz * zValueFactor;
				variableValues.put('x', (double) xCurrentValue);
				variableValues.put('z', (double) zCurrentValue);
				float yEvaluationValue = (float) parser.evaluate(variableValues);
				if(yEvaluationValue > maxyv) maxyv = yEvaluationValue;
				float yDrawValue = ((yEvaluationValue - miny) / (maxy - miny)) * (boxSize - 1);
				verticiesRow[gridz] = new Vector3f(gridx, yDrawValue, gridz);
			}
			functionPointRows[gridx] = verticiesRow;
		}
		boxSize--;
		for(int i = 0; i < functionPointRows.length - 1; i++){
			Vector3f row[] = functionPointRows[i];
			FloatBuffer buffer = BufferUtils.createFloatBuffer(row.length * 3 * 2);
			for(int j = 0; j < row.length; j++){
				row[j].store(buffer);
				functionPointRows[i + 1][j].store(buffer);
			}
			RenderObject functionLine = new RenderObject(buffer, functionShader, attributes);
			functionLine.setDrawMode(GL11.GL_TRIANGLE_STRIP);
			objects.add(functionLine);
		}
		functionShader.setUniform1f(functionShader.getUniformId("boxSize"), boxSize);
		return objects;
	}
	
	public void setValues(float maxx, float minx, float maxy, float miny, float maxz, float minz){
		this.maxx = maxx;
		this.minx = minx;
		this.maxy = maxy;
		this.miny = miny;
		this.maxz = maxz;
		this.minz = minz;
		xValueFactor = (maxx - minx) / boxSize;
		zValueFactor = (maxz - minz) / boxSize;
	}
	
	public RenderObject createWireframe(Shader lineShader, VertexAttribute... attributes){
		FloatBuffer data = BufferUtils.createFloatBuffer((2 * 3 * (2 * functionPointRows[0].length - 2) * functionPointRows.length));
		for(Vector3f row[] : functionPointRows){
			for(int i = 1; i < row.length; i++){
				row[i - 1].store(data);
				row[i].store(data);
			}
		}
		for(int i = 0; i < functionPointRows[0].length; i++){
			for(int j = 1; j < functionPointRows.length; j++){
				functionPointRows[j][i].store(data);
				functionPointRows[j - 1][i].store(data);
			}
		}
		RenderObject wireframe = new RenderObject(data, lineShader, attributes);
		wireframe.setDrawMode(GL11.GL_LINES);
		lineShader.setUniform4f(lineShader.getUniformId("color"), 1, 1, 1, 0.4f);
		return wireframe;
	}
}