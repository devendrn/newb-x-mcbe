$input v_texcoord0

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/config.h>
  #include <newb/functions/tonemap.h>

  uniform vec4 SunMoonColor;

  SAMPLER2D_AUTOREG(s_SunMoonTexture);
#endif

void main() {
  #ifndef INSTANCING
    vec4 color = texture2D(s_SunMoonTexture, v_texcoord0);
    color.rgb *= SunMoonColor.rgb;
    color.rgb *= 4.4*color.rgb;
    float tr = 1.0 - SunMoonColor.a;
    color.a *= 1.0 - (1.0-NL_SUNMOON_RAIN_VISIBILITY)*tr*tr;
  color.rgb = colorCorrection(color.rgb,gl_FragCoord.xy,u_viewRect.zw);
    gl_FragColor = color;
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
