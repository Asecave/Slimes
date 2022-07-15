#version 460 core

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
	return texture2D(u_texture,
			(v_texCoords.xy + vec2(offsetX, offsetY) / frameDimensions.xy));
}

uint decode(vec3 v) {
	ivec3 bytes = ivec3(v * 255);
	return (bytes.r << 16) | (bytes.g << 8) | (bytes.b);
}

vec3 encode(uint v) {
	vec3 color;
	color.r = (v >> 16) % 256 / 255f;
	color.g = (v >> 8) % 256 / 255f;
	color.b = v % 256 / 255f;
	return color;
}

void main() {

	vec3 c = get(0, 0).rgb;

	uint v = decode(c);

	if (v > 50000){
		v -= 50000;
	} else {
		v = 0;
	}

	vec3 enc = encode(v);

	fragColor = vec4(enc, 1f);
}
