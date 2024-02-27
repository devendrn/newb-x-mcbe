$input a_texcoord0, a_position
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_posTime, v_texcoord0

#include <bgfx_shader.sh>
#include <newb/main.sh>

//uniform vec4 FogColor;
uniform vec4 ViewPositionAndTime;

void main() {

#ifdef INSTANCING
  mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
  mat4 model = u_model[0];
#endif

  vec3 pos = mul(model, vec4(a_position, 1.0)).xyz;

  // pi/1800 (one complete rotation per hour)
  highp float t = 0.00174532925*ViewPositionAndTime.w;

  // rotate skybox
  float sinA = sin(t);
  float cosA = cos(t);
  pos.xz = mul(mtxFromRows(vec2(cosA,-sinA),vec2(sinA,cosA)), pos.xz);

  v_texcoord0 = 2.0*a_texcoord0;
  v_posTime = vec4(pos, ViewPositionAndTime.w);
  gl_Position = mul(u_viewProj, vec4(pos, 1.0));
}
