$input a_color0, a_position
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0
#include <newb/config.h>
#if defined(TRANSPARENT) && NL_CLOUD_TYPE == 2
  $output v_color1, v_color2
#endif

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform vec4 CloudColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
#ifdef TRANSPARENT

#ifdef INSTANCING
  mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
  mat4 model = u_model[0];
#endif
  float t = ViewPositionAndTime.w;
  float rain = detectRain(FogAndDistanceControl.xyz);

  vec3 zenithCol = getZenithCol(rain, FogColor.rgb);
  vec3 horizonCol = getHorizonCol(rain, FogColor.rgb);
  vec3 fogCol = getHorizonEdgeCol(horizonCol, rain, FogColor.rgb);

  vec3 pos = a_position;
  vec4 color;
  
  #if NL_CLOUD_TYPE == 0
    pos.y *= NL_CLOUD0_THICKNESS + rain*(NL_CLOUD0_RAIN_THICKNESS - NL_CLOUD0_THICKNESS);
    vec3 worldPos = mul(model, vec4(pos, 1.0)).xyz;
    
    color.rgb = zenithCol + fogCol*(0.3+0.5*a_position.y);
    color.rgb *= 1.0 - 0.5*rain;

    // fade out cloud layer
    color.a = NL_CLOUD1_OPACITY;
    color.a *= clamp(2.0-2.0*length(worldPos.xyz)*0.004, 0.0, 1.0);

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

    float fade = clamp(2.0-2.0*length(worldPos.xyz)*0.0022, 0.0, 1.0);
    #if NL_CLOUD_TYPE == 1
      // make cloud plane sperical
      float len = length(worldPos.xz)*0.01;
      worldPos.y -= len*len*clamp(0.2*worldPos.y, -1.0, 1.0);

      color = renderCloudsSimple(worldPos.xyz, t, rain, zenithCol, horizonCol, fogCol);

      // cloud depth
      worldPos.y -= NL_CLOUD1_DEPTH*color.a*3.3;

      color.a *= NL_CLOUD1_OPACITY;

      #ifdef NL_AURORA
        color += renderAurora(worldPos, t, rain, fogCol)*(1.0-color.a);
      #endif

      color.a *= fade;
      color.rgb = colorCorrection(color.rgb);
    #else
    
      v_color2 = vec4(fogCol,ViewPositionAndTime.w);
      v_color1 = vec4(zenithCol,rain);
      color = vec4(worldPos, fade);
    #endif 
  #endif

  v_color0 = color;
  gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
  v_color0 = vec4(0.0,0.0,0.0,0.0);
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
