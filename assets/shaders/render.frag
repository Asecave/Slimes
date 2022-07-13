#version 430 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec2 frameDimensions;
uniform sampler2D channelR;
uniform sampler2D channelG;
uniform sampler2D channelB;

layout(location=0) out vec4 fragColor;

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

	vec4 rawR = texture2D(channelR, v_texCoords.xy);
//	vec4 rawG = texture2D(channelG, v_texCoords.xy);
//	vec4 rawB = texture2D(channelB, v_texCoords.xy);

	float r = min(decode(rawR) / 4294967295f, 1f);
//	float g = min(decode(rawG) / 4294967295f, 1f);
//	float b = min(decode(rawB) / 4294967295f, 1f);

	fragColor = texture2D(u_texture, v_texCoords.xy); //vec4(r, 0f, 0f, 1f);
}
