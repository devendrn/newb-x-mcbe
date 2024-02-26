#ifndef FOG_H
#define FOG_H

#include "tonemap.h"

float nlRenderFogFade(float relativeDist, vec3 FOG_COLOR, vec2 FOG_CONTROL) {
#if NL_FOG_TYPE == 0
  // no fog
  return 0.0;
#else
  #if NL_FOG_TYPE == 1
    // linear transition
    float fade = clamp((relativeDist-FOG_CONTROL.x)/(FOG_CONTROL.y-FOG_CONTROL.x), 0.0, 1.0);
  #else
    // smoother transition
    float fade = smoothstep(FOG_CONTROL.x, FOG_CONTROL.y, relativeDist);
  #endif

  // misty effect
  float density = NL_MIST_DENSITY*(19.0 - 18.0*FOG_COLOR.g);
  fade += (1.0-fade)*(0.3-0.3*exp(-relativeDist*relativeDist*density));

  return fade;
#endif
}

#endif
