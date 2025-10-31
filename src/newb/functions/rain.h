#ifndef RAIN_H
#define RAIN_H

#include "detection.h"
#include "noise.h"
#include "sky.h"
#include "water.h"

float nlWindblow(vec3 pos, float t){
  vec2 p = pos.xy/(1.0+pos.z);
  float val = sin(4.0*p.x + 2.0*p.y + 2.0*t + 3.0*p.y*p.x)*sin(p.y*2.0 + 0.2*t);
  val += sin(p.y - p.x + 0.2*t);
  return 0.25*val*val;
}

vec4 nlRefl(
  nl_skycolor skycol, nl_environment env, inout vec4 color, vec2 lit, vec3 tiledCpos, float camDist,
  vec3 wPos, vec3 viewDir, vec3 torchColor, vec3 FOG_COLOR, float renderDist, highp float t
) {
  vec4 wetRefl = vec4(0.0,0.0,0.0,0.0);

  #ifndef NL_GROUND_REFL
  if (env.rainFactor > 0.0) {
  #endif

    float wetness = lit.y*lit.y;

    // clip reflection when far (better performance)
    float endDist = renderDist*0.6;
    if (camDist < endDist) {
      float cosR = max(viewDir.y, 0.0);
      float puddles = max(1.0 - NL_GROUND_RAIN_PUDDLES*fastRand(tiledCpos.xz), 0.0);

      #ifndef NL_GROUND_REFL
        wetness *= puddles;
        float reflective = wetness*env.rainFactor*NL_GROUND_RAIN_WETNESS;
      #else
        float reflective = NL_GROUND_REFL;
        if (!env.end && !env.nether) {
          // only multiply with wetness in overworld
          reflective *= wetness;
        } 

        wetness *= puddles;
        reflective = mix(reflective, wetness, env.rainFactor);
      #endif

      if (wPos.y < 0.0) {
        wetRefl.rgb = nlRenderSky(skycol, env, viewDir, t, false);
        wetRefl.a = calculateFresnel(cosR, 0.03)*reflective;

        #if defined(NL_GROUND_AURORA_REFL) && defined(NL_AURORA) && defined (NL_GROUND_REFL)
          vec2 cloudPos = -(120.0-wPos.y)*viewDir.xz/viewDir.y;
          float fade = clamp(2.0 - 0.005*length(cloudPos), 0.0, 1.0);
          vec4 aurora = renderAurora(cloudPos.xyy, t, env.rainFactor, skycol.horizonEdge);
          wetRefl.rgb += aurora.rgb*aurora.a*fade;
        #endif

        // torch light
        wetRefl.rgb += torchColor*lit.x*NL_TORCHLIGHT_INTENSITY;

        // fade out before clip
        wetRefl.a *= clamp(2.0-2.0*camDist/endDist, 0.0, 1.0);
      }
    }

    // darken wet parts
    color.rgb *= 1.0 - 0.4*wetness*env.rainFactor;

  #ifndef NL_GROUND_REFL
  }
  #endif

  return wetRefl;
}

#endif
