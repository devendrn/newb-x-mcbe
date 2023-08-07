$input a_color0, a_position
$output v_color0, v_color1, v_color2, v_color3

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

//uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;

void main() {
#if defined(OPAQUE)

  vec3 pos = a_position;

  // make sky more spherical
  pos.y -= 0.4*a_color0.r*a_color0.r;

  vec3 wPos = pos.xyz;
  wPos.y += 0.148;

  bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);

  // sky colors
  if (underWater) {
    vec3 fogcol = getUnderwaterCol(FogColor.rgb);
    v_color0 = fogcol;
    v_color1 = fogcol;
    v_color2 = fogcol;
  } else {
    float rainFactor = detectRain(FogAndDistanceControl.xyz);
    v_color0 = getZenithCol(rainFactor, FogColor.rgb);
    v_color1 = getHorizonCol(rainFactor, FogColor.rgb);
    v_color2 = getHorizonEdgeCol(v_color1, rainFactor, FogColor.rgb);
  }

  v_color3 = wPos;
  gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));

#else
  v_color0 = vec3(0.0,0.0,0.0);
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
