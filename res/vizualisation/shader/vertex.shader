#version 150

in vec3 vertexIn;

uniform mat4 projection;
uniform mat4 camera;
uniform mat4 model;

out vec3 fragmentVertex;

void main(){
	fragmentVertex = vertexIn;
	gl_Position = projection * camera * model * vec4(vertexIn, 1);
}