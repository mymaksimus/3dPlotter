#version 150

in vec3 fragmentVertex;

uniform float boxSize;

out vec4 outColor;

void main(){
	float amount = fragmentVertex.y / boxSize;
	outColor = vec4(0, amount, 1 - amount, 1.0);
}