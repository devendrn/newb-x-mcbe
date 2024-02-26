$input a_position, a_texcoord0
$output v_texcoord0, v_color0, v_color1, v_color2, v_color3

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform mat4 CubemapRotation;

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;

void main() {
  // will be clamped in fragment shader
  float fade = (a_position.y - 0.15)*10.0;

  vec3 wPos = mul(u_model[0], vec4(a_position, 1.0)).xyz;
  bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);

  if (underWater) {
    vec3 fogcol = getUnderwaterCol(FogColor.rgb);
    v_color0 = fogcol;
    v_color1 = fogcol;
    v_color2.rgb = fogcol;
  } else {
    float rainFactor = detectRain(FogAndDistanceControl.xyz);
    v_color0 = getZenithCol(rainFactor, FogColor.rgb);
    v_color1 = getHorizonCol(rainFactor, FogColor.rgb);
    v_color2.rgb = getHorizonEdgeCol(v_color1, rainFactor, FogColor.rgb);
  }

  v_color2.a = float(underWater);

  v_color3 = vec4(wPos, fade);
  v_texcoord0 = a_texcoord0;
  gl_Position = mul(u_modelViewProj, mul(CubemapRotation, vec4(a_position, 1.0)));
}
