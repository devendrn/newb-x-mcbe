$input a_position, a_texcoord0
$output v_texcoord0

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/config.h>
  #include <newb/functions/utils.h>

  uniform vec4 TimeOfDay;
#endif

void main() {
  v_texcoord0 = a_texcoord0;
  #ifndef INSTANCING
    vec3 pos = a_position;
    pos.x = -pos.x;

    mat4 model = u_model[0];
    vec2 dir = mul(u_model[0], vec4(0.0, 0.0, 0.0, 1.0)).xy;
    float st = 2.0*PI*TimeOfDay.x + 0.5*PI;
    bool isSun = dot(vec2(cos(st), sin(st)), dir) > 0.0;

    float dist = 300.0;
    float angle = 0.0;
    float tilt = 0.0;
    float yaw = 0.0;
    if (isSun) {
      dist = -dist;
      pos.x = -pos.x;
      pos.xz *= NL_SUN_SIZE;
      angle = degToRad(NL_SUN_TILT);
      tilt = degToRad( NL_SUN_PATH_TILT);
      yaw = degToRad(NL_SUN_PATH_YAW);
    } else {
      pos.xz *= NL_MOON_SIZE;
      angle = degToRad(NL_MOON_TILT);
      tilt = degToRad( NL_MOON_PATH_TILT);
      yaw = degToRad(NL_MOON_PATH_YAW);
    }
    pos.xz = mul(rmat2(angle + 0.5*st), pos.xz);

    vec4 wpos = vec4(dist, 70.0*pos.xz, 1.0);

    // TODO: Combine these into single mat?
    wpos.xy = mul(rmat2(st), -wpos.xy);
    wpos.yz = mul(rmat2(tilt), wpos.yz);
    wpos.xz = mul(rmat2(yaw), wpos.xz);

    gl_Position = mul(u_viewProj, wpos);
  #else
    gl_Position = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
