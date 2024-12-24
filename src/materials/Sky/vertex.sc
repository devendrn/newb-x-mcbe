#ifndef INSTANCING
  $input a_color0, a_position
  $output v_fogColor, v_worldPos, v_underwaterRainTime
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>

  //uniform vec4 SkyColor;
  uniform vec4 FogColor;
  uniform vec4 FogAndDistanceControl;
  uniform vec4 ViewPositionAndTime;
#endif

void main() {
  #ifndef INSTANCING
    v_underwaterRainTime.x = float(detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy));
    v_underwaterRainTime.y = detectRain(FogAndDistanceControl.xyz);
    v_underwaterRainTime.z = ViewPositionAndTime.w;

    // background quad
    vec4 pos = vec4(a_position.xzy, 1.0);
    pos.xy = 2.0*clamp(pos.xy, -0.5, 0.5);

    v_fogColor = FogColor.rgb;
    v_worldPos = mul(u_invViewProj, pos).xyz;

    gl_Position = pos;
  #else
    gl_Position = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
