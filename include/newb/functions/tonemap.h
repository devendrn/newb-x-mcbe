#ifndef TONEMAP_H
#define TONEMAP_H

vec3 colorCorrection(vec3 col) {
  #ifdef NL_EXPOSURE
    col *= NL_EXPOSURE;
  #endif

  // tone map
  // ref - https://64.github.io/tonemapping/
  #if NL_TONEMAP_TYPE == 3
    // extended reinhard tonemapping
    const float white_scale = 0.063;
    col = col*(1.0+col*white_scale)/(1.0+col);
  #elif NL_TONEMAP_TYPE == 4
    // aces tone mapping
    const float a = 1.04;
    const float b = 0.03;
    const float c = 0.93;
    const float d = 0.56;
    const float e = 0.14;
    col *= 0.85;
    col = clamp((col*(a*col + b)) / (col*(c*col + d) + e), 0.0, 1.0);
  #elif NL_TONEMAP_TYPE == 2
    // simple reinhard tonemapping
    col = col/(1.0+col);
  #elif NL_TONEMAP_TYPE == 1
    // exponential tonemapping
    col = 1.0-exp(-col*0.8);
  #endif

  // actually supposed to be gamma correction
  col = pow(col, vec3_splat(NL_CONSTRAST));

  #ifdef NL_SATURATION
    col = mix(vec3_splat(dot(col,vec3(0.21, 0.71, 0.08))), col, NL_SATURATION);
  #endif

  #ifdef NL_TINT
    col *= NL_TINT;
  #endif

  return col;
}

// inv used in fogcolor for nether
vec3 colorCorrectionInv(vec3 col) {

  #ifdef NL_TINT
    col /= NL_TINT;
  #endif

  //#ifdef NL_SATURATION
  //  col = mix(vec3_splat(dot(col,vec3(0.21, 0.71, 0.08))), col, NL_SATURATION);
  //#endif

  // incomplete
  // extended reinhard only
  float ws = 0.7966;
  col = pow(col, vec3_splat(1.0/NL_CONSTRAST));
  col = col*(ws + col)/(ws + col*(1.0 - ws));

  #ifdef NL_EXPOSURE
    col /= NL_EXPOSURE;
  #endif

  return col;
}

#endif
