#ifndef NOISE_H
#define NOISE_H

#include "constants.h"

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83

// hash function for noise (for highp only)
float rand(highp vec2 n) {
  return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

// 1D noise - used in plants,lantern wave
float noise1D(highp float x) {
  float x0 = floor(x);
  float t0 = x-x0;
  t0 *= t0*(3.0-2.0*t0);
  return mix(fract(sin(x0)*84.85), fract(sin(x0+1.0)*84.85), t0);
}

// simpler rand for disp, puddles
float fastRand(vec2 n){
  return fract(37.45*sin(dot(n, vec2(4.36, 8.28))));
}

// water displacement map (also used by caustic)
float disp(vec3 pos, float t, float s) {
  float n = sin(8.0*NL_CONST_PI_HALF*(pos.x+pos.y*pos.z) + 0.7*s*t);
  pos.y += s*t + 0.8*n;
  float p = floor(pos.y);
  return (0.8+0.2*n) * mix(fastRand(pos.xz+p), fastRand(pos.xz+p+1.0), pos.y - p);
}

float noise2D(vec2 u) {
  vec2 u0 = floor(u);
  vec2 v = u-u0;
  v *= v*(3.0 - 2.0*v);
  float c0 = rand(u0);
  float c1 = rand(u0+vec2(1.0, 0.0));
  float c2 = rand(u0+vec2(1.0, 1.0));
  float c3 = rand(u0+vec2(0.0, 1.0));
  return mix(mix(c0, c3, v.y), mix(c1, c2, v.y), v.x);
}

vec4 mod289(vec4 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 perm(vec4 x) {
  return mod289(((x * 34.0) + 1.0) * x);
}

// used by galaxy
float noise3D(vec3 p){
  vec3 a = floor(p);
  vec3 d = p - a;
  //d = d * d * (3.0 - 2.0 * d);

  vec4 b = a.xxyy + vec4(0.0, 1.0, 0.0, 1.0);
  vec4 k1 = perm(b.xyxy);
  vec4 k2 = perm(k1.xyxy + b.zzww);

  vec4 c = k2 + a.zzzz;
  vec4 k3 = perm(c);
  vec4 k4 = perm(c + 1.0);
  vec4 o1 = fract(k3 / 41.0);
  vec4 o2 = fract(k4 / 41.0);
  vec4 o3 = o2 * d.z + o1 * (1.0 - d.z);
  vec2 o4 = o3.yw * d.x + o3.xz * (1.0 - d.x);

  return o4.y * d.y + o4.x * (1.0 - d.y);
}


#endif
