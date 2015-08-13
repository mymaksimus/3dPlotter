#version 150

in vec3 fragmentVertex;

uniform vec4 color;

out vec4 outColor;

void main(){
	outColor = color;
}