$input a_position, a_texcoord0
$output v_texcoord0, v_fogColor, v_worldPos, v_underwaterRainTime

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform mat4 CubemapRotation;

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
  v_underwaterRainTime.x = float(detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy));
  v_underwaterRainTime.y = detectRain(FogAndDistanceControl.xyz);
  v_underwaterRainTime.z = ViewPositionAndTime.w;

  v_fogColor = FogColor.rgb;
  v_texcoord0 = a_texcoord0;
  v_worldPos = mul(u_model[0], vec4(a_position, 1.0)).xyz; 
  gl_Position = mul(u_modelViewProj, mul(CubemapRotation, vec4(a_position, 1.0)));
}
