$input a_color0, a_position
#ifdef OPAQUE
$output v_fogColor, v_worldPos, v_underwaterRainTime
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

  v_underwaterRainTime.x = float(detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy));
  v_underwaterRainTime.y = detectRain(FogAndDistanceControl.xyz);
  v_underwaterRainTime.z = ViewPositionAndTime.w;

  v_fogColor = FogColor.rgb;
  v_worldPos = mul(u_model[0], vec4(pos, 1.0)).xyz;
  gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));
#else
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
