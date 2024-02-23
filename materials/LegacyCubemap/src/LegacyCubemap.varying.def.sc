vec3 a_position    : POSITION;
vec2 a_texcoord0 : TEXCOORD0;

vec2 v_texcoord0 : TEXCOORD0;

vec3 CubePos      : CHUNKED_POSITION;
vec3 WorldPos     : RELATIVE_POSITION;

vec4 FOG_COLOR : v_fog;
vec2 FOGC             : FOG_CONTROL.xy;

float TIME              : FRAME_TIME_COUNTER;
