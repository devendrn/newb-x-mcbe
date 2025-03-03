$input a_color0, a_position
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0
#include <newb/config.h>
#if NL_CLOUD_TYPE >= 2
  $output v_color1, v_color2, v_fogColor
#endif

#include <bgfx_shader.sh>
#include <newb/main.sh>

// uniform vec4 CloudColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

float fog_fade(vec3 wPos) {
  return clamp(2.0-length(wPos*vec3(0.005, 0.002, 0.005)), 0.0, 1.0);
}

void main() {
  #ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
  #else
    mat4 model = u_model[0];
  #endif

  float t = ViewPositionAndTime.w;
  float rain = detectRain(FogAndDistanceControl.xyz);

  nl_skycolor skycol = nlOverworldSkyColors(rain, FogColor.rgb);
  vec3 pos = a_position;
  vec3 worldPos;

  #if NL_CLOUD_TYPE <= 2

    vec4 color;

    #if NL_CLOUD_TYPE == 0
      pos.y *= (NL_CLOUD0_THICKNESS + rain*(NL_CLOUD0_RAIN_THICKNESS - NL_CLOUD0_THICKNESS));
      worldPos = mul(model, vec4(pos, 1.0)).xyz;

      color.rgb = skycol.zenith + skycol.horizonEdge;
      color.rgb += dot(color.rgb, vec3(0.3,0.4,0.3))*a_position.y;
      color.rgb *= 1.0 - 0.8*rain;
      color.rgb = colorCorrection(color.rgb);
      color.a = NL_CLOUD0_OPACITY * fog_fade(worldPos.xyz);

      // clouds.png has two non-overlaping layers:
      // r=unused, g=layers, b=reference, a=unused
      // g=0 (layer 0), g=1 (layer 1)
      bool isL2 = a_color0.g > 0.5 * a_color0.b;
      if (isL2) {
        #ifdef NL_CLOUD0_MULTILAYER
          worldPos.y += 64.0;
        #else
          worldPos = vec3(0.0,0.0,0.0);
          color.a = 0.0;
        #endif
      }
    #else
      pos.xz = pos.xz - 32.0;
      pos.y *= 0.01;
      worldPos.x = pos.x*model[0][0];
      worldPos.z = pos.z*model[2][2];
      #if BGFX_SHADER_LANGUAGE_GLSL
        worldPos.y = pos.y+model[3][1];
      #else
        worldPos.y = pos.y+model[1][3];
      #endif

      float fade = fog_fade(worldPos.xyz);
      #if NL_CLOUD_TYPE == 1
        // make cloud plane spherical
        float len = length(worldPos.xz)*0.01;
        worldPos.y -= len*len*clamp(0.2*worldPos.y, -1.0, 1.0);

        color = renderCloudsSimple(skycol, worldPos.xyz, t, rain);

        // cloud depth
        worldPos.y -= NL_CLOUD1_DEPTH*color.a*3.3;

        color.a *= NL_CLOUD1_OPACITY;

        #ifdef NL_AURORA
          color += renderAurora(worldPos, t, rain, FogColor.rgb)*(1.0-color.a);
        #endif

        color.a *= fade;
        color.rgb = colorCorrection(color.rgb);
      #else // NL_CLOUD_TYPE 2
        v_fogColor = FogColor.rgb;
        v_color1 = vec4(skycol.zenith, rain);
        v_color2 = vec4(skycol.horizonEdge, ViewPositionAndTime.w);
        color = vec4(worldPos, fade);
      #endif 
    #endif

    v_color0 = color;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
  #else
    vec4 apos = vec4(pos.xz - 32.0, 1.0, 1.0);
    apos.x *= pos.y - 0.5;
    apos.xy = clamp(apos.xy, -1.0, 1.0);

    #if BGFX_SHADER_LANGUAGE_GLSL
      float h = model[3][1];
    #else
      float h = model[1][3];
    #endif
    h = clamp(0.002*h, 0.0, 1.0);

    worldPos = mul(u_invViewProj, apos).xyz;

    v_fogColor = FogColor.rgb;
    v_color0 = vec4(worldPos, h*h);
    v_color1 = vec4(skycol.zenith, rain);
    v_color2 = vec4(skycol.horizonEdge, ViewPositionAndTime.w);
    gl_Position = apos;
  #endif
}
