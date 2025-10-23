#ifndef DETECTION_H
#define DETECTION_H

#include "utils.h"

struct nl_environment {
  bool end;
  bool nether;
  bool underwater;
  float rainFactor;
  float dayFactor;
  vec3 sunDir;
  vec3 moonDir;
};

bool detectEnd(float DIMENSION_ID) {
  return DIMENSION_ID == 2.0;
}

bool detectNether(float DIMENSION_ID, vec3 FOG_COLOR, vec2 FOG_CONTROL) {
  // also consider underlava as nether
  bool underLava = FOG_CONTROL.x == 0.0 && FOG_COLOR.b == 0.0 && FOG_COLOR.g < 0.18 && FOG_COLOR.r-FOG_COLOR.g > 0.1;
  return (DIMENSION_ID == 1.0) || underLava;
}

bool detectUnderwater(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
  return FOG_CONTROL.x==0.0 && FOG_CONTROL.y<0.8 && (FOG_COLOR.b>FOG_COLOR.r || FOG_COLOR.g>FOG_COLOR.r);
}

float detectRain(vec3 FOG_CONTROL) {
  // clear fogctrl.x varies with render distance (z)
  // reverse plotted as 0.5 + 1.25/k (k is renderdistance in chunks, fogctrl.z = k*16)
  vec2 clear = vec2(0.5 + 20.0/FOG_CONTROL.z, 1.0); // clear fogctrl value
  vec2 rain = vec2(0.23, 0.70); // rain fogctrl value
  vec2 factor = clamp((FOG_CONTROL.xy-clear)/(rain-clear), vec2(0.0,0.0), vec2(1.0,1.0));
  float val = factor.x*factor.y;
  return val*val*(3.0 - 2.0*val);
}

float detectDayFactor(vec3 FOG_COLOR) {
  return min(dot(FOG_COLOR, vec3(0.5,0.7,0.5)), 1.0);
}

nl_environment calculateSunParams(nl_environment env, float TIME_OF_DAY, float DAY) {
  float t = 2.0 * PI * TIME_OF_DAY;
  vec3 sunDir = vec3(sin(t), cos(t), 0.0); 
  vec3 moonDir = vec3(-sunDir.x, -sunDir.y, 0.0);
  env.dayFactor = sunDir.y;

  sunDir.yz = mul(rmat2(-degToRad(NL_SUN_PATH_TILT)), sunDir.yz);
  sunDir.xz = mul(rmat2(degToRad(NL_SUN_PATH_YAW)), sunDir.xz);

  moonDir.yz = mul(rmat2(-degToRad(NL_MOON_PATH_TILT)), moonDir.yz);
  moonDir.xz = mul(rmat2(degToRad(NL_MOON_PATH_YAW)), moonDir.xz);

  env.sunDir = sunDir;
  env.moonDir = moonDir;

  return env;
}

nl_environment nlDetectEnvironment(float DIMENSION_ID, float TIME_OF_DAY, float DAY, vec3 FOG_COLOR, vec3 FOG_CONTROL) {
  nl_environment env;
  env.end = detectEnd(DIMENSION_ID);
  env.nether = detectNether(DIMENSION_ID, FOG_COLOR, FOG_CONTROL.xy);
  env.underwater = detectUnderwater(FOG_COLOR, FOG_CONTROL.xy);
  env.rainFactor = detectRain(FOG_CONTROL.xyz);
  env = calculateSunParams(env, TIME_OF_DAY, DAY);
  return env;
}

#endif
