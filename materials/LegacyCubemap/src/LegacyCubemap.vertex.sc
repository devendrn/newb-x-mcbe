$input a_position, a_texcoord0
$output v_texcoord0, v_zenithCol, v_horizonColTime, v_horizonEdgeColUnderwater, v_worldPos

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform mat4 CubemapRotation;

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
  bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
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

  v_horizonEdgeColUnderwater.w = float(underWater);
  v_horizonColTime.w = ViewPositionAndTime.w;

  v_texcoord0 = a_texcoord0;
  v_worldPos = mul(u_model[0], vec4(a_position, 1.0)).xyz; 
  gl_Position = mul(u_modelViewProj, mul(CubemapRotation, vec4(a_position, 1.0)));
}
