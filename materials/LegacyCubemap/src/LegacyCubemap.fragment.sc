$input v_texcoord0, v_color0, v_color1, v_color2, v_color3

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D(s_MatTexture, 0);

uniform vec4 ViewPositionAndTime;

void main() {
  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);

  vec3 viewDir = normalize(v_color3.xyz);
  bool underWater = v_color2.w > 0.5;

  vec3 skyColor = nlRenderSky(v_color2.rgb, v_color1.rgb, v_color0.rgb, -viewDir, ViewPositionAndTime.w, false, underWater);

  vec4 color = vec4(colorCorrection(skyColor), clamp(v_color3.w, 0.0, 1.0));

  diffuse = mix(color, diffuse, diffuse.a);

  gl_FragColor = diffuse;
}
