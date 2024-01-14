$input a_position, a_texcoord0
$output v_texcoord0

#include <bgfx_shader.sh>
#include <newb/config.h>

void main() {
  v_texcoord0 = a_texcoord0;
  vec3 pos = a_position;

  pos.xz *= NL_SUNMOON_SIZE;
  #ifdef NL_SUNMOON_ANGLE
    float angle = NL_SUNMOON_ANGLE*0.0174533;
    float sinA = sin(angle);
    float cosA = cos(angle);
    pos.xz = vec2(pos.x*cosA - pos.z*sinA, pos.x*sinA + pos.z*cosA);
  #endif

  gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));
}
