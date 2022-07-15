#version 460 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

layout(binding=1) uniform sampler2D channel;
uniform int clear;

layout(location=0) out vec4 fragColor;

void main() {

	vec4 prev = texture2D(u_texture, v_texCoords.xy);
	vec4 c = texture2D(channel, v_texCoords.xy);

	if (clear == 1)
		fragColor = c;
//	else
//		fragColor = vec4(prev.r + c.r, prev.g + c.g, prev.b + c.b, 1f);
}
