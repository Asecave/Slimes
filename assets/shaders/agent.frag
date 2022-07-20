#version 330 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float dt;
uniform vec2 texDimensions;

layout(location=0) out vec4 fragColor;

const float moveSpeed = 1f;

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

int decode(vec3 v) {
	ivec3 bytes = ivec3(v * 255);
	return (bytes.r << 16) | (bytes.g << 8) | (bytes.b);
}

vec3 encode(int v) {
	vec3 color;
	color.r = (v >> 16) % 256 / 255f;
	color.g = (v >> 8) % 256 / 255f;
	color.b = v % 256 / 255f;
	return color;
}

int get(float component) {
	vec2 coords = v_texCoords.xy + vec2(0f, component - 1f) / texDimensions.xy;
	return decode(texture2D(u_texture, coords).rgb);
}

void main() {

	int current = int(v_texCoords.y * texDimensions.y);

	int v;

	v = get(current);
	float angle = get(3f) / 100000f;

	switch (current) {
	case 0:
		float directionX = cos(angle);
		v += int(directionX * moveSpeed * 1000);
		break;
	case 1:
		float directionY = sin(angle);
		v += int(directionY * moveSpeed * 1000);
		break;
	case 2:

		break;
	}

	if (v < 0)
		v = 0;

	vec3 enc = encode(v);

	fragColor = vec4(enc, 1f);
}
