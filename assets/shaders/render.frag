#version 460 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

layout(location=0) out vec4 fragColor;

layout(binding=1) uniform sampler2D channelR;
layout(binding=2) uniform sampler2D channelG;
layout(binding=3) uniform sampler2D channelB;

uint decode(vec4 v) {
	return (uint(255 * v.r) << 24) | (uint(255 * v.g) << 16)
			| (uint(255 * v.b) << 8) | (uint(255 * v.a));
}

void main() {

	vec4 rawR = texture2D(channelR, v_texCoords.xy);
	vec4 rawG = texture2D(channelG, v_texCoords.xy);
	vec4 rawB = texture2D(channelB, v_texCoords.xy);

	float r = decode(rawR) / 4294967295f;
	float g = decode(rawG) / 4294967295f;
	float b = decode(rawB) / 4294967295f;

	fragColor = vec4(r, g, b, 1f);
}
