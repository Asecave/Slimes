#version 430 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float dt;
uniform vec2 frameDimensions;

layout(location=0) out vec4 fragColor;

vec4 get(float offsetX, float offsetY) {
	return texture2D(u_texture, (v_texCoords.xy + vec2(offsetX, offsetY) / frameDimensions.xy));
}

void main() {

    vec4 newColor = vec4(get(0, 0).r, 0, 0, 1);
    
    fragColor = newColor;
}