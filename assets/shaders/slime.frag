#version 330 core
#ifdef GL_ES
precision mediump float;
#endif

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float totalTime;
uniform float dt;
uniform vec2 frameDimensions;

const float maxHash = 4294967295f;

// Hash function www.cs.ubc.ca/~rbridson/docs/schechter-sca08-turbulence.pdf
uint hash(uint state) {
    state ^= 2747636419u;
    state *= 2654435769u;
    state ^= state >> 16;
    state *= 2654435769u;
    state ^= state >> 16;
    state *= 2654435769u;
    return state;
}

float nhash(uint state) {
	return hash(state) / maxHash;
}

//returns the state of the current texel + x,y. We just need the states "alive" or "dead".
//so we just return an integer 0 or 1
int get(float x, float y)
{
    return int(texture2D(u_texture, (v_texCoords.xy + vec2(x, y) / frameDimensions)).b);
}

void main(void)
{
    //count the "living" neighbour texels
    int sum = get(-1, -1) +
              get(-1,  0) +
              get(-1,  1) +
              get( 0, -1) +
              get( 0,  1) +
              get( 1, -1) +
              get( 1,  0) +
              get( 1,  1);

    //if we have 3 living neighbours the current cell will live, if there are two,
    //we keep the current state. Otherwise the cell is dead.
    if (sum==3)
    {
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
    else if (sum== 2)
    {
        float current = float(get(0, 0));
        gl_FragColor = vec4(current, current, current, 1.0);
    }
    else
    {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}