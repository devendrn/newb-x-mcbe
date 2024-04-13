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

vec4 nlRefl(
  inout vec4 color, inout vec4 mistColor, vec2 lit, vec2 uv1, vec3 tiledCpos,
  float camDist, vec3 wPos, vec3 viewDir, vec3 torchColor, vec3 horizonEdgeCol, vec3 horizonCol,
  vec3 zenithCol, vec3 FOG_COLOR, float rainFactor, float renderDist, highp float t, vec3 pos, bool underWater, bool end, bool nether
) {
  vec4 wetRefl = vec4(0.0,0.0,0.0,0.0);

  #ifndef NL_GROUND_REFL
  if (rainFactor > 0.0) {
  #endif
    float wetness = lit.y*lit.y;

  #ifdef NL_RAIN_MIST_OPACITY
    // humid air blow
    float humidAir = rainFactor*wetness*nlWindblow(pos.xy/(1.0+pos.z), t);
    mistColor.a = min(mistColor.a + humidAir*NL_RAIN_MIST_OPACITY, 1.0);
  #endif

    // clip reflection when far (better performance)
    float endDist = renderDist*0.6;
    if (camDist < endDist) {
      float cosR = max(viewDir.y, 0.0);
      float puddles = max(1.0 - NL_GROUND_RAIN_PUDDLES*fastRand(tiledCpos.xz), 0.0);

    #ifndef NL_GROUND_REFL
      wetness *= puddles;
      wetRefl.a = calculateFresnel(cosR, 0.03)*wetness*rainFactor*NL_GROUND_RAIN_WETNESS;
    #else
      float reflective = NL_GROUND_REFL;
      if (!end && !nether) {
        reflective *= wetness;
      }

      wetness *= puddles;

      reflective = mix(reflective, wetness, rainFactor);

      wetRefl.a = calculateFresnel(cosR, 0.03)*reflective;
    #endif

      // wetRefl.rgb = getRainSkyRefl(horizonCol, zenithCol, cosR);
      wetRefl.rgb = getSkyRefl(horizonEdgeCol, horizonCol, zenithCol, viewDir, FOG_COLOR, t, -wPos.y, end, underWater, nether);

      // torch light
      wetRefl.rgb += torchColor*lit.x*NL_TORCH_INTENSITY;

      // fade out before clip
      wetRefl.a *= clamp(2.0-2.0*camDist/endDist, 0.0, 1.0);
    }

    // darken wet parts
    color.rgb *= 1.0 - 0.4*wetness*rainFactor;
    color.rgb *= 1.0 - 0.5*wetRefl.a;

  #ifndef NL_GROUND_REFL
  }
  #endif

  return wetRefl;
}

#endif
