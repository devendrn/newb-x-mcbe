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

void main() {
#ifdef TRANSPARENT

#ifdef INSTANCING
  mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
  mat4 model = u_model[0];
#endif
  float rain = detectRain(FogAndDistanceControl.xyz);

  vec3 zenith_col = getZenithCol(rain, FogColor.rgb);
  vec3 horizon_col = getHorizonCol(rain, FogColor.rgb);
  vec3 fog_col = getHorizonEdgeCol(horizon_col, rain, FogColor.rgb);

  vec3 pos = a_position;
  pos.y *= 2.0 + rain;
  vec3 worldPos = mul(model, vec4(pos, 1.0)).xyz;

  vec4 color = vec4(zenith_col,1.0);
  color.rgb += fog_col*(0.2+0.8*a_position.y);
  color.rgb *= 1.0 - 0.5*rain;

  // fade out cloud layer
  color.a = NL_CLOUD_OPACITY;
  color.a *= clamp(2.0-2.0*length(worldPos.xyz)*0.004, 0.0, 1.0);

  color.rgb = colorCorrection(color.rgb);

  v_color0 = color;
  gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
  v_color0 = vec4(0.0,0.0,0.0,0.0);
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
