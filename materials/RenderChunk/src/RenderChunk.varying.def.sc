vec4 a_color0                  : COLOR0;
vec3 a_position               : POSITION;
vec2 a_texcoord0            : TEXCOORD0;
vec2 a_texcoord1            : TEXCOORD1;

vec4 i_data0                     : TEXCOORD7;
vec4 i_data1                     : TEXCOORD6;
vec4 i_data2                     : TEXCOORD5;

vec4 v_color0                   : COLOR0;
vec4 v_fog                        : COLOR2;
centroid vec2 v_texcoord0  : TEXCOORD0;
vec2 v_lightmapUV         : TEXCOORD1;

vec3 ChunkPos               : CHUNKED_POSITION;
vec3 WorldPos                : RELATIVE_POSITION;
vec3 ViewPos                  : RELATIVE_POSITION2;
vec3 ScreenPos              : RELATIVE_POSITION3;

vec4 FOG_COLOR           : v_fog;
vec2 FOGC                       : FOG_CONTROL.xy;

float TIME                        : FRAME_TIME_COUNTER;
float RENDER_CHUNKS : FAR_CHUNKS_DISTANCE;