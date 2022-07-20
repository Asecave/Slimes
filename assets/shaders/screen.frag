#version 330 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float dt;
uniform vec2 frameDimensions;
uniform int agentCount;

uniform sampler2D agents;

layout(location=0) out vec4 fragColor;

const float diffuseRate = 5f;
const float decayRate = 1f;

const int white = 16777215;

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

int getScreen(float offsetX, float offsetY) {
	vec2 coords = v_texCoords.xy + vec2(offsetX, offsetY) / frameDimensions.xy;
	return decode(texture2D(u_texture, coords).rgb);
}

int blur() {
	int sum = 0;

	int original = getScreen(0, 0);

	for (int offsetX = -1; offsetX <= 1; offsetX++) {
		for (int offsetY = -1; offsetY <= 1; offsetY++) {
			int c = getScreen(offsetX, offsetY);
			sum += c;
		}
	}

	int deltaBlur = sum / 9 - original;
	float diffuseWeight = clamp(diffuseRate * dt, 0f, 1f);
	return original + int(diffuseWeight * deltaBlur);
}

int drawAgents(int v) {
	int nv = v + decode(texture2D(agents, v_texCoords).rgb);
	nv = clamp(nv, 0, white);
	return nv;
}

void main() {

	int v = blur();

	v -= int(100000 * decayRate);

	v = drawAgents(v);

	if (v < 0)
		v = 0;

	vec3 enc = encode(v);

	fragColor = vec4(enc, 1f);
}
