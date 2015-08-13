#version 150

in vec2 vertexIn;

uniform mat4 projection;
uniform mat4 camera;
uniform mat4 model;

void main(){
	gl_Position = projection * camera * model * vec4(vertexIn, 0, 1);
}