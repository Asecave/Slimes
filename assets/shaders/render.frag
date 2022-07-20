#version 330 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

layout(location=0) out vec4 fragColor;

uniform sampler2D channelR;
uniform sampler2D channelG;
uniform sampler2D channelB;

int decode(vec3 v) {
	ivec3 bytes = ivec3(v * 255);
	return (bytes.r << 16) | (bytes.g << 8) | (bytes.b);
}

void main() {

	vec3 rawR = texture2D(channelR, v_texCoords.xy).rgb;
	vec3 rawG = texture2D(channelG, v_texCoords.xy).rgb;
	vec3 rawB = texture2D(channelB, v_texCoords.xy).rgb;

	float r = decode(rawR) / 16777215f;
	float g = decode(rawG) / 16777215f;
	float b = decode(rawB) / 16777215f;

	fragColor = vec4(0f, g, b, 1f);
}
