#version 430 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

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

uint decode(vec4 v) {
	return (uint(255 * v.r) << 24) | (uint(255 * v.g) << 16)
			| (uint(255 * v.b) << 8) | (uint(255 * v.a));
}

vec4 encode(uint v) {
	float r = ((v & 0xff000000) >> 24) / 255f;
	float g = ((v & 0x00ff0000) >> 16) / 255f;
	float b = ((v & 0x0000ff00) >> 8) / 255f;
	float a = ((v & 0x000000ff)) / 255f;
	return vec4(r, g, b, a);
}

void main() {

	vec4 c = get(0, 0);

	uint v = decode(c);

//	v -= 100;

	vec4 enc = encode(v);

	fragColor = enc;
}
