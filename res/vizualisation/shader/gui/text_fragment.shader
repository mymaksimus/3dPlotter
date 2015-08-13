#version 150

in vec2 fragmentTexCoords;

uniform sampler2D textImage;
uniform vec3 color;

out vec4 outColor;

void main(){
	outColor = vec4(color, texture2D(textImage, fragmentTexCoords).a);
}