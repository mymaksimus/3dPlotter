#version 150

in vec2 fragmentTexCoords;

uniform sampler2D textImage;
uniform vec4 color;

out vec4 outColor;

void main(){
	outColor = texture2D(textImage, fragmentTexCoords);
}