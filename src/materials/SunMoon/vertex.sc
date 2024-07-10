$input a_position, a_texcoord0
$output v_texcoord0

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/config.h>
#endif

void main() {
  v_texcoord0 = a_texcoord0;
  #ifndef INSTANCING
    vec3 pos = a_position;

    pos.xz *= NL_SUNMOON_SIZE;
    #ifdef NL_SUNMOON_ANGLE
      float angle = NL_SUNMOON_ANGLE*0.0174533;
      float sinA = sin(angle);
      float cosA = cos(angle);
      pos.xz = vec2(pos.x*cosA - pos.z*sinA, pos.x*sinA + pos.z*cosA);
    #endif
    gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));
  #else
    gl_Position = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
