#ifndef RAIN_H
#define RAIN_H

#include "clouds.h"
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
  inout vec4 color, nl_skycolor skycol, nl_environment env, vec3 viewDir, vec3 wPos, vec3 tiledCpos,
  vec3 CAMERA_POS, vec3 torchColor, vec2 lit, float camDist, float renderDist, highp float t
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
        viewDir.y = -viewDir.y;
        wetRefl.rgb = nlRenderSky(skycol, env, viewDir, t, false);

        #ifdef NL_CLOUD_AURORA_REFLECTION
          vec4 cloudRefl = nlCloudAuroraReflection(skycol, env, viewDir, wPos, CAMERA_POS, t);
          wetRefl.rgb = mix(wetRefl.rgb, cloudRefl.rgb, cloudRefl.a);
        #endif

        // torch light
        wetRefl.rgb += torchColor*lit.x*NL_TORCHLIGHT_INTENSITY;

        wetRefl.a = calculateFresnel(cosR, 0.03)*reflective;
        wetRefl.a *= clamp(2.0-2.0*camDist/endDist, 0.0, 1.0); // fade out before clip
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
