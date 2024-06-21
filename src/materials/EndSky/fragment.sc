#ifndef INSTANCING
$input v_texcoord0, v_posTime
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
#include <newb/main.sh>

SAMPLER2D(s_MatTexture, 0);
#endif

void main() {
// Instancing is off normally?
#ifndef INSTANCING
  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);

  // end sky gradient
  vec3 color = renderEndSky(getEndHorizonCol(), getEndZenithCol(), normalize(v_posTime.xyz), v_posTime.w);

  // stars
  color += 2.8*diffuse.rgb;

  color = colorCorrection(color);
  gl_FragColor = vec4(color, 1.0);
#else
  gl_FragColor = vec4(0.0,0.0,0.0,0.0);
#endif
}
