$input v_color0, v_texcoord0, v_pos

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

SAMPLER2D(s_MatTexture, 0);

void main() {
  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);

  float sphereY = v_pos.y/sqrt(dot(v_pos,v_pos));
  float grad = 1.0 - max(sphereY,0.0);
  grad *= grad;

  // end sky gradient
  vec3 color = v_color0*(0.3 + 0.7*smoothstep(0.0,1.0,grad));

  // stars
  color += diffuse.rgb*(1.0-grad*grad);

  // end void gradient
  float glow = max((-sphereY-0.5)*2.0,0.0);
  color += v_color0*glow*glow*glow;

  gl_FragColor = vec4(color, 1.0);
}
