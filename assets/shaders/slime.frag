#version 330 core

in vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float totalTime;
uniform float dt;
uniform vec2 frameDimensions;

//returns the state of the current texel + x,y. We just need the states "alive" or "dead".
//so we just return an integer 0 or 1
float get(float x, float y) {
	
	vec4 c = texture2D(u_texture, (v_texCoords.xy + vec2(x, y) / frameDimensions.xy));

	return c.r;
}

int getbool(float x, float y) {
	float c = get(x, y);
	return int(round(c));
}

void main() {
    //count the "living" neighbour texels
    int sum = getbool(-1, -1) +
              getbool(-1,  0) +
              getbool(-1,  1) +
              getbool( 0, -1) +
              getbool( 0,  1) +
              getbool( 1, -1) +
              getbool( 1,  0) +
              getbool( 1,  1);

    //if we have 3 living neighbours the current cell will live, if there are two,
    //we keep the current state. Otherwise the cell is dead.
    
    vec4 newColor;
    
    float current = float(get(0, 0));
    
    if (sum == 3) {
        newColor = vec4(1f, 1f, 1f, 1f);
    } else if (sum == 2) {
        newColor = vec4(current, current, current, 1f);
    } else {
    	if (current == 1f) {
        	newColor = vec4(0.4f, 0.4f, 0.4f, 1f);
        } else {
        	float decay = 0.002f; 
        	newColor = vec4(current - decay, current - decay, current - decay, 1f);
        }
    }
    
    gl_FragColor = newColor;
}