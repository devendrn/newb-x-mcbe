#ifndef WATER_H
#define WATER_H

#include "constants.h"
#include "noise.h"

// fresnel - Schlick's approximation
float calculateFresnel(float cosR, float r0) {
  float a = 1.0-cosR;
  float a2 = a*a;
  return r0 + (1.0-r0)*a2*a2*a;
}

vec4 nl_water(inout vec3 wPos, inout vec4 color, vec3 viewDir, vec3 light, vec3 cPos, float fractCposY,
              vec4 COLOR, vec3 FOG_COLOR, vec3 horizonCol,vec3 horizonEdgeCol, vec3 zenithCol,
              vec2 uv1, vec2 lit, highp float t, float camDist, float rainFactor,
              vec3 tiledCpos, bool end, vec3 torchColor) {

  float cosR;
  float bump = NL_WATER_BUMP;
  vec3 waterRefl;

  // reflection for top plane
  if (fractCposY > 0.0) {

    // calculate cosine of incidence angle and apply water bump
    bump *= disp(tiledCpos, t) + 0.12*sin(t*2.0 + dot(cPos, vec3_splat(NL_CONST_PI_HALF)));

    cosR = abs(viewDir.y);
    cosR = mix(cosR, 1.0-cosR*cosR, bump);

    // sky reflection
    waterRefl = getSkyRefl(horizonEdgeCol, horizonCol, zenithCol, cosR, -wPos.y);
    waterRefl += getSunRefl(viewDir.x,horizonEdgeCol.r, FOG_COLOR);

    // cloud reflection
    #if defined(NL_WATER_CLOUD_REFLECTION)
      if (wPos.y < 0.0) {
        vec2 parallax = viewDir.xz/viewDir.y;
        vec2 projectedPos = wPos.xz - parallax*100.0*(1.0-bump);

        float fade = 1.0 - 0.002*length(projectedPos);
        //projectedPos += fade*parallax;

        fade = clamp(2.0*fade,0.0,1.0);

        #ifdef NL_AURORA
        vec4 aurora = render_aurora(projectedPos.xyy, t, rainFactor, horizonEdgeCol);
        waterRefl += 2.0*aurora.rgb*aurora.a*fade;
        #endif

        #if NL_CLOUD_TYPE == 1
        vec4 clouds = render_clouds_simple(projectedPos.xyy, t, rainFactor, zenithCol, horizonCol, horizonEdgeCol);
        waterRefl = mix(waterRefl,1.5*clouds.rgb,clouds.a*fade);
        #endif
      }
    #endif

    // mask sky reflection
    if (!end) {
      waterRefl *= 0.05 + lit.y*1.14;
    }

    // torch light reflection
    waterRefl += torchColor*NL_TORCH_INTENSITY*(lit.x*lit.x + lit.x)*bump*10.0;

    if (fractCposY>0.8 || fractCposY<0.9) {
      // flat plane
      waterRefl *= 1.0 - clamp(wPos.y, 0.0, 0.66);
    } else {
      // slanted plane and highly slanted plane
      waterRefl *= (0.1*sin(t*2.0+cPos.y*12.566)) + (fractCposY > 0.9 ? 0.2 : 0.4);
    }
  }
  // reflection for side plane
  else {
    bump *= 0.5 + 0.5*sin(1.5*t + dot(cPos, vec3_splat(NL_CONST_PI_HALF)));
    cosR = max(sqrt(dot(viewDir.xz, viewDir.xz)), step(wPos.y,0.5));
    cosR += (1.0-cosR*cosR)*bump;

    waterRefl = zenithCol*uv1.y*uv1.y*1.3;
  }

  float fresnel = calculateFresnel(cosR, 0.03);
  float opacity = 1.0-cosR;

#ifdef NL_WATER_FOG_FADE
  color.a *= NL_WATER_TRANSPARENCY;
#else
  color.a = COLOR.a*NL_WATER_TRANSPARENCY;
#endif

  color.a += (1.0-color.a)*opacity*opacity;

  color.rgb *= 0.22*NL_WATER_TINT*(1.0-0.8*fresnel);

#ifdef NL_WATER_WAVE
  if(camDist < 14.0) {
    wPos.y -= bump;
  }
#endif

  return vec4(waterRefl, fresnel);
}

#endif
