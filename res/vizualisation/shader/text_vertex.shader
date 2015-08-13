#version 150

in vec3 vertexIn;
in vec2 texCoords;

uniform mat4 projection;
uniform mat4 camera;
uniform mat4 model;

out vec2 fragmentTexCoords;

void main(){
	fragmentTexCoords = texCoords;
	gl_Position = projection * camera * model * vec4(vertexIn, 1);
}