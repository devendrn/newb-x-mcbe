#ifndef FOG_H
#define FOG_H

#include "tonemap.h"

vec4 nlRenderFog(vec3 fogColor, float relativeDist, bool nether, vec3 FOG_COLOR, vec2 FOG_CONTROL) {

#if NL_FOG_TYPE == 0
  return vec4(0.0,0.0,0.0,0.0);
#endif

  vec4 fog;
  if (nether) {
    // to blend fog with void color
    fog.rgb = colorCorrectionInv(FOG_COLOR);
  } else {
    fog.rgb = fogColor;
  }

#if NL_FOG_TYPE == 2
  fog.a = smoothstep(FOG_CONTROL.x, FOG_CONTROL.y, relativeDist);
#else
  fog.a = clamp((relativeDist-FOG_CONTROL.x)/(FOG_CONTROL.y-FOG_CONTROL.x), 0.0, 1.0);
#endif

  // misty effect
  float density = NL_MIST_DENSITY*(19.0 - 18.0*FOG_COLOR.g);
  fog.a += (1.0-fog.a)*(0.3-0.3*exp(-relativeDist*relativeDist*density));

  return fog;
}

#endif
