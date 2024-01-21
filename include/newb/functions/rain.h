#ifndef RAIN_H
#define RAIN_H

#include "noise.h"
#include "sky.h"
#include "water.h"

float nlWindblow(vec2 p, float t){
  float val = sin(4.0*p.x + 2.0*p.y + 2.0*t + 3.0*p.y*p.x)*sin(p.y*2.0 + 0.2*t);
  val += sin(p.y - p.x + 0.2*t);
  return 0.25*val*val;
}

vec4 nlRefl(inout vec4 color, inout vec4 mistColor, vec2 lit, vec2 uv1, vec3 tiledCpos,
             float camDist, vec3 wPos, vec3 viewDir, vec3 torchColor, vec3 horizonCol,
             vec3 zenithCol, float rainFactor, float renderDist, highp float t, vec3 pos) {
  vec4 wetRefl = vec4(0.0,0.0,0.0,0.0);

  if (rainFactor > 0.0) {
    float wetness = lit.y*lit.y*rainFactor;

#ifdef NL_RAIN_MIST_OPACITY
    // humid air blow
    float humidAir = wetness*nlWindblow(pos.xy/(1.0+pos.z), t);
    mistColor.a = min(mistColor.a + humidAir*NL_RAIN_MIST_OPACITY, 1.0);
#endif

    // clip reflection when far (better performance)
    float endDist = renderDist*0.6;
    if (camDist < endDist) {

      // puddles
      wetness *= 1.0 - NL_RAIN_PUDDLES*fastRand(tiledCpos.xz);

      float cosR = max(viewDir.y, 0.0);
      wetRefl.rgb = getRainSkyRefl(horizonCol, zenithCol, cosR);
      wetRefl.a = calculateFresnel(cosR, 0.03)*wetness*NL_RAIN_WETNESS;

      // torch light
      wetRefl.rgb += torchColor*lit.x*NL_TORCH_INTENSITY;

      // fade out before clip
      wetRefl.a *= clamp(2.0-2.0*camDist/endDist, 0.0, 1.0);
    }

    // darken wet parts
    color.rgb *= 1.0 - 0.4*wetness;
  }

  return wetRefl;
}

#endif
