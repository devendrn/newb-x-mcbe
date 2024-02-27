$input a_color0, a_position
#ifdef OPAQUE
$output v_zenithCol, v_horizonColTime, v_horizonEdgeColUnderwater, v_worldPos
#endif

#include <bgfx_shader.sh>
#include <newb/main.sh>

//uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
#ifdef OPAQUE
  vec3 pos = a_position;

  // make sky more spherical
  pos.y -= 0.4*a_color0.r*a_color0.r;

  bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);

  // sky colors
  if (underWater) {
    vec3 fogcol = getUnderwaterCol(FogColor.rgb);
    v_zenithCol = fogcol;
    v_horizonColTime.rgb = fogcol;
    v_horizonEdgeColUnderwater.rgb = fogcol;
  } else {
    float rainFactor = detectRain(FogAndDistanceControl.xyz);
    v_zenithCol = getZenithCol(rainFactor, FogColor.rgb);
    v_horizonColTime.rgb = getHorizonCol(rainFactor, FogColor.rgb);
    v_horizonEdgeColUnderwater.rgb = getHorizonEdgeCol(v_horizonColTime.rgb, rainFactor, FogColor.rgb);
  }

  v_horizonEdgeColUnderwater.a = float(underWater);
  v_horizonColTime.a = ViewPositionAndTime.w;

  v_worldPos = mul(u_model[0], vec4(pos, 1.0)).xyz;
  gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));
#else
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
