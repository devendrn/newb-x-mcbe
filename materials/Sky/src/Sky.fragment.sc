$input v_color0, v_color1, v_color2, v_color3

#include <bgfx_shader.sh>
#include <newb/main.sh>

void main() {
#if defined(OPAQUE)
  vec3 wPos = v_color3;
  float sphereY = max(0.0,wPos.y/sqrt(dot(wPos,wPos)));

  vec3 skyColor = renderSky(v_color2,v_color1,v_color0.rgb,sphereY);

  skyColor = colorCorrection(skyColor);

  gl_FragColor = vec4(skyColor, 1.0);
#else
  gl_FragColor = vec4(0.0,0.0,0.0,0.0);
#endif
}
