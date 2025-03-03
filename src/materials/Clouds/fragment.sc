$input v_color0
#include <newb/config.h>
#if NL_CLOUD_TYPE >= 2
  $input v_color1, v_color2, v_fogColor
#endif

#include <bgfx_shader.sh>
#include <newb/main.sh>

#define NL_CLOUD_PARAMS(x) NL_CLOUD2##x##STEPS, NL_CLOUD2##x##THICKNESS, NL_CLOUD2##x##RAIN_THICKNESS, NL_CLOUD2##x##VELOCITY, NL_CLOUD2##x##SCALE, NL_CLOUD2##x##DENSITY, NL_CLOUD2##x##SHAPE

void main() {
  vec4 color = v_color0;

  #if NL_CLOUD_TYPE >= 2
    vec3 vDir = normalize(v_color0.xyz);

    #if NL_CLOUD_TYPE == 2
      color = renderCloudsRounded(vDir, v_color0.xyz, v_color1.w, v_color2.w, v_color2.rgb, v_color1.rgb, NL_CLOUD_PARAMS(_));

      #ifdef NL_CLOUD2_LAYER2
        vec2 parallax = vDir.xz / abs(vDir.y) * NL_CLOUD2_LAYER2_OFFSET;
        vec3 offsetPos = v_color0.xyz;
        offsetPos.xz += parallax;
        vec4 color2 = renderCloudsRounded(vDir, offsetPos, v_color1.a, v_color2.a*2.0, v_color2.rgb, v_color1.rgb, NL_CLOUD_PARAMS(_LAYER2_));
        color = mix(color2, color, 0.2 + 0.8*color.a);
      #endif

      #ifdef NL_AURORA
        color += renderAurora(v_color0.xyz, v_color2.a, v_color1.a, v_fogColor)*(1.0-0.95*color.a);
      #endif

      color.a *= v_color0.a;
    #else
      vDir.xz *= 0.3 + v_color0.w; // height parallax

      vec2 p = vDir.xz/(0.015 + 0.035*abs(vDir.y));
      vec4 clouds = renderClouds(p, v_color2.w, v_color1.w, v_color2.rgb, v_color1.rgb, NL_CLOUD3_SCALE, NL_CLOUD3_SPEED, NL_CLOUD3_SHADOW);
      color = clouds;

      #ifdef NL_AURORA
        p.xy *= 34.7;
        color += renderAurora(p.xyy, v_color2.w, v_color1.w, v_fogColor)*(1.0-0.95*color.a);
      #endif

      color.a *= smoothstep(0.0, 0.7, vDir.y);
    #endif

    color.rgb = colorCorrection(color.rgb);
  #endif

  gl_FragColor = color;
}
