$input a_position,a_texcoord0
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_texcoord0, CubePos, WorldPos, FOG_COLOR, FOGC, TIME

#include <bgfx_shader.sh>

uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

uniform mat4 CubemapRotation;

void main() {

mat4 model;
#ifdef INSTANCING
    model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
    model = u_model[0];
#endif

  v_texcoord0 = a_texcoord0;
  FOG_COLOR = FogColor;
  FOGC = FogAndDistanceControl.xy;
  TIME = ViewPositionAndTime.w;
  vec3 fPos = mul(model, vec4(a_position,1.0)).xyz;
  CubePos = fPos;
  vec4 WorldCube = mul(u_viewProj, vec4(fPos,1.0));
  WorldPos = WorldCube.xyz;
  gl_Position = WorldCube;

}
