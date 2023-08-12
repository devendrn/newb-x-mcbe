$input v_color0
#include <newb_config_legacy.h>
#if defined(TRANSPARENT) && NL_CLOUD_TYPE == 2
  $input v_color1, v_color2
#endif

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

void main() {
  vec4 color = v_color0;
  
#if defined(TRANSPARENT) && NL_CLOUD_TYPE == 2
  vec3 v_dir = normalize(v_color0.xyz);

  color = render_clouds(v_dir, v_color0.xyz, v_color1.a, v_color2.a, v_color2.rgb, v_color1.rgb);
  color.a *= v_color0.a;

  color.rgb = colorCorrection(color.rgb);
#endif

  gl_FragColor = color;
}
