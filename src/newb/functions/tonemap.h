#ifndef TONEMAP_H
#define TONEMAP_H

vec3 colorCorrection(vec3 col) {
  #ifdef NL_EXPOSURE
    col *= NL_EXPOSURE;
  #endif
  
 #if NL_TONEMAP_TYPE == 10 //https://github.com/dmnsgn/glsl-tone-map/blob/main/unreal.glsl
  //unreal tonemap
  col = col / (col + 0.155) * 1.019;
 #endif
  // ref - https://64.github.io/tonemapping/
  #if NL_TONEMAP_TYPE == 3
    // extended reinhard tonemap
    const float whiteScale = 0.063;
    col = col*(1.0+col*whiteScale)/(1.0+col);
  #elif NL_TONEMAP_TYPE == 4
    // aces tonemap
     mat3 m1 = mat3(
        0.59719, 0.07600, 0.02840,
        0.35458, 0.90834, 0.13383,
        0.04823, 0.01566, 0.83777
	);
	mat3 m2 = mat3(
        1.60475, -0.10208, -0.00327,
        -0.53108,  1.10813, -0.07276,
        -0.07367, -0.00605,  1.07602
	);
	vec3 v = m1 * col;
	vec3 a = v * (v + 0.0245786) - 0.000090537;
	vec3 b = v * (0.983729 * v + 0.4329510) + 0.238081;
	col = pow(clamp(m2 * (a / b), 0.0, 1.0), vec3(1.0 / 2.2));
   #elif NL_TONEMAP_TYPE == 2
    // simple reinhard tonemap
    col = col/(1.0+col);
  #elif NL_TONEMAP_TYPE == 1
    // exponential tonemap
    col = 1.0-exp(-col*0.8);
  #endif

  // gamma correction + contrast
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
