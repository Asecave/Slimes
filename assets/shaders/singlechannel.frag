#version 460 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform int channel;

layout(location=0) out vec4 fragColor;

uint decode(vec4 v) {
	return (uint(255 * v.r) << 24) | (uint(255 * v.g) << 16)
			| (uint(255 * v.b) << 8) | (uint(255 * v.a));
}

void main() {

	vec4 raw = texture2D(u_texture, v_texCoords.xy);
	float c = decode(raw) / 4294967295f;

	switch (channel) {
	case 0:
		fragColor = vec4(c, 0f, 0f, 1f);
		break;
	case 1:
		fragColor = vec4(0f, c, 0f, 1f);
		break;
	case 2:
		fragColor = vec4(0f, 0f, c, 1f);
	}
}
