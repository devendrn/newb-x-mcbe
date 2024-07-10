$input a_color0, a_position
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0
#include <newb/config.h>
#if NL_CLOUD_TYPE == 2
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

  vec3 fs = getSkyFactors(FogColor.rgb);
  vec3 zenithCol = getZenithCol(rain, FogColor.rgb, fs);
  vec3 horizonCol = getHorizonCol(rain, FogColor.rgb, fs);
  vec3 horizonEdgeCol = getHorizonEdgeCol(horizonCol, rain, FogColor.rgb);

  vec3 pos = a_position;
  vec4 color;
  
  #if NL_CLOUD_TYPE == 0
    // clouds.png has two non-overlaping layers:
    // r=unused, g=layers, b=reference, a=unused
    // g=0 (normal clouds), g=1 (used for aurora)
    bool isL2 = a_color0.g > 0.5 * a_color0.b;

    if (!isL2) {
      pos.y *= (NL_CLOUD0_THICKNESS + rain*(NL_CLOUD0_RAIN_THICKNESS - NL_CLOUD0_THICKNESS));
    }

    vec3 worldPos = mul(model, vec4(pos, 1.0)).xyz;
    
    color.rgb = zenithCol + horizonEdgeCol*(0.5 + 0.5*a_position.y);
    color.rgb *= 1.0 - 0.5*rain;
    color.a = NL_CLOUD0_OPACITY;

    if (isL2) {
      #ifdef NL_AURORA
        worldPos.xyz += 4.0*sin(0.1*worldPos.zxx + vec3(0.2,0.5,0.3)*t);
        worldPos.y += 24.0*a_position.y*(1.0 + sin(0.1*(worldPos.z+worldPos.x) + 0.5*t));
        worldPos.y += NL_CLOUD0_THICKNESS + 20.0;
        vec4 aurora = renderAurora(worldPos, t, rain, FogColor.rgb);
        color.rgb = zenithCol + 0.8*horizonCol;
        color.rgb +=  aurora.rgb;
        color.a = (a_position.y < 0.5 && a_color0.b > 0.9) ? aurora.a : 0.0;
      #else
        worldPos.xyz = vec3(0.0,0.0,0.0);
        color.a = 0.0;
      #endif
    } 

    color.a *= fog_fade(worldPos.xyz);
    color.rgb = colorCorrection(color.rgb);
  #else
    pos.xz = pos.xz - 32.0;
    pos.y *= 0.01;
    vec3 worldPos;
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

      color = renderCloudsSimple(worldPos.xyz, t, rain, zenithCol, horizonCol, horizonEdgeCol);

      // cloud depth
      worldPos.y -= NL_CLOUD1_DEPTH*color.a*3.3;

      color.a *= NL_CLOUD1_OPACITY;

      #ifdef NL_AURORA
        color += renderAurora(worldPos, t, rain, FogColor.rgb)*(1.0-color.a);
      #endif

      color.a *= fade;
      color.rgb = colorCorrection(color.rgb);
    #else
      v_fogColor = FogColor.rgb;
      v_color2 = vec4(horizonEdgeCol, ViewPositionAndTime.w);
      v_color1 = vec4(zenithCol, rain);
      color = vec4(worldPos, fade);
    #endif 
  #endif

  v_color0 = color;
  gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
}
