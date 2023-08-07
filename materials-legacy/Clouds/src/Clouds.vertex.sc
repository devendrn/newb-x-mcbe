$input a_color0, a_position
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 CloudColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
#ifdef TRANSPARENT
#ifdef INSTANCING
  mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
  mat4 model = u_model[0];
#endif

  vec3 pos = a_position;
  pos.xz = 0.7*(pos.xz - 32.0);
  pos.y *= 0.1;
  vec3 worldPos;
  worldPos.x = pos.x*mtxElement(model, 0, 0);
  worldPos.y = pos.y+mtxElement(model, 3, 1);
  worldPos.z = pos.z*mtxElement(model, 2, 2);

  // make cloud plane sperical
  float len = length(worldPos.xz)*0.01;
  worldPos.y -= len*len*clamp(0.2*worldPos.y, -1.0, 1.0);

  highp float t = ViewPositionAndTime.w;

  float rain = detectRain(FogAndDistanceControl.xyz);

  vec3 zenith_col = getZenithCol(rain, FogColor.rgb);
  vec3 horizon_col = getHorizonCol(rain, FogColor.rgb);
  vec3 fog_col = getHorizonEdgeCol(horizon_col, rain, FogColor.rgb);

  vec4 color = renderClouds(worldPos.xyz, t, rain, zenith_col, horizon_col, fog_col);

  // cloud depth
  worldPos.y -= NL_CLOUD_DEPTH*color.a*3.3;

  color.a *= NL_CLOUD_OPACITY;

#ifdef NL_AURORA
  color += renderAurora(worldPos.xz, t, rain, FogColor.rgb)*(1.0-0.7*color.a);
#endif

  // fade out cloud layer
  color.a *= clamp(2.0-2.0*length(worldPos.xyz)*0.0023, 0.0, 1.0);

  color.rgb = colorCorrection(color.rgb);

  v_color0 = color;
  gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
  v_color0 = vec4(0.0,0.0,0.0,0.0);
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
