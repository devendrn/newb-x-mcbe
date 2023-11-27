$input v_texcoord0

#include <bgfx_shader.sh>
#include <newb_config_legacy.h>

uniform vec4 SunMoonColor;

SAMPLER2D(s_SunMoonTexture, 0);

void main() {
  vec4 color = texture2D(s_SunMoonTexture, v_texcoord0);
  color.rgb *= SunMoonColor.rgb;
  float tr = 1.0 - SunMoonColor.a;
  color.a *= 1.0 - tr*tr*tr;
  gl_FragColor = color;
}
