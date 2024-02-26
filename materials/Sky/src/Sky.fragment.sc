#ifdef OPAQUE
$input v_color0, v_color1, v_color2, v_color3
#endif

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform vec4 ViewPositionAndTime;

void main() {
#ifdef OPAQUE
  vec3 viewDir = normalize(v_color3.xyz);
  bool underWater = v_color2.w > 0.5;

  vec3 skyColor = nlRenderSky(v_color2.rgb, v_color1.rgb, v_color0.rgb, -viewDir, ViewPositionAndTime.w, false, underWater);
  skyColor = colorCorrection(skyColor);

  gl_FragColor = vec4(skyColor, 1.0);
#else
  gl_FragColor = vec4(0.0,0.0,0.0,0.0);
#endif
}
