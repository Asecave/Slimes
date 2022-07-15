#version 460 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float dt;
uniform vec2 frameDimensions;
uniform vec2 agentDimensions;
uniform int agentCount;

layout(binding=1) uniform sampler2D agents;

layout(location=0) out vec4 fragColor;

const float diffuseRate = 20f;

const int white = 16777215;

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

int getScreen(int offsetX, int offsetY) {
	vec2 coords = v_texCoords.xy + vec2(offsetX, offsetY) / frameDimensions.xy;
	return decode(texture2D(u_texture, coords).rgb);
}

int getAgent(int id, int field) {
	vec2 coords = vec2(id + 0.5f, field + 0.5f) / agentDimensions.xy;
	return decode(texture2D(agents, coords).rgb);
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
	for (int i = 0; i < agentCount; i++) {
		ivec2 pixel = ivec2(v_texCoords * frameDimensions);
		int agentX = getAgent(i, 0);
		int agentY = getAgent(i, 1);
		if (pixel.x == agentX && pixel.y == agentY) {
			return white;
		}
	}
	return v;
}

void main() {

	int v = blur();

	v = drawAgents(v);

	if (v < 0)
		v = 0;

	vec3 enc = encode(v);

	fragColor = vec4(enc, 1f);
}
