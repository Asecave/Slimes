#version 430 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float dt;
uniform vec2 frameDimensions;

layout(location=0) out vec4 fragColor;

uint hash(uint state) {
    state ^= 2747636419u;
    state *= 2654435769u;
    state ^= state >> 16;
    state *= 2654435769u;
    state ^= state >> 16;
    state *= 2654435769u;
    return state;
}

float scaleToRange01(uint state) {
    return state / 4294967295f;
}

vec4 get(float offsetX, float offsetY) {
	return texture2D(u_texture, (v_texCoords.xy + vec2(offsetX, offsetY) / frameDimensions.xy));
}

float rgbaToInt(vec4 c) {
	return (c.a * 256f) + (c.b * 65536f) + (c.g * 16777216f) + (c.r * 4294967296f);
}

void main() {

	vec4 c = get(0, 0);



	float grayScaleRaw = rgbaToInt(c);
	float f = grayScaleRaw / 4294967296f;

	f -= 0.005f;

    vec4 newColor = vec4(f, f, f, 1f);
    
    fragColor = newColor;
}
