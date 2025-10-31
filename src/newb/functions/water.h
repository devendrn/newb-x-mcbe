#ifndef WATER_H
#define WATER_H

#include "utils.h"
#include "detection.h"
#include "sky.h"
#include "clouds.h"
#include "noise.h"

// fresnel - Schlick's approximation
float calculateFresnel(float cosR, float r0) {
  float a = 1.0-cosR;
  float a2 = a*a;
  return r0 + (1.0-r0)*a2*a2*a;
}

vec4 nlWater(
  nl_skycolor skycol, nl_environment env, inout vec3 wPos, inout vec4 color, vec4 COLOR, vec3 viewDir, vec3 light, vec3 cPos, vec3 tiledCpos, 
  float fractCposY, vec3 FOG_COLOR, vec2 lit, highp float t, float camDist, vec3 torchColor
) {

  vec2 bump = vec2(disp(tiledCpos, NL_WATER_WAVE_SPEED*t), disp(tiledCpos, NL_WATER_WAVE_SPEED*(t+1.8))) - 0.5;
  vec3 nrm;
  if (fractCposY > 0.0) { // top plane
    nrm.xz = bump*NL_WATER_BUMP;
    nrm.y = -1.0;
    /*if (fractCposY>0.8 || fractCposY<0.9) { // flat plane
    } else { // slanted plane and highly slanted plane
    }*/
  } else { // reflection for side plane
    bump *= 0.5 + 0.5*sin(3.0*t*NL_WATER_WAVE_SPEED + cPos.y*PI_HALF);
    nrm.xz = normalize(viewDir.xz) + bump.y*(1.0-viewDir.xz*viewDir.xz)*NL_WATER_BUMP;
    nrm.y = bump.x*NL_WATER_BUMP;
  }
  nrm = normalize(nrm);

  float cosR = dot(nrm, viewDir);
  viewDir = viewDir - 2.0*cosR*nrm ; // reflect(viewDir, nrm)

  vec3 waterRefl = nlRenderSky(skycol, env, viewDir, t, false);

  #if defined(NL_WATER_CLOUD_AURORA_REFLECTION)
    if (viewDir.y < 0.0) {
      vec2 cloudPos = (120.0-wPos.y)*viewDir.xz/viewDir.y;
      float fade = clamp(2.0 - 0.005*length(cloudPos), 0.0, 1.0);

      #ifdef NL_AURORA
        vec4 aurora = renderAurora(cloudPos.xyy, t, env.rainFactor, FOG_COLOR);
        waterRefl += aurora.rgb*aurora.a*fade;
      #endif

      #if NL_CLOUD_TYPE == 1
        vec4 clouds = renderCloudsSimple(skycol, cloudPos.xyy, t, env.rainFactor);
        waterRefl = mix(waterRefl, clouds.rgb, clouds.a*fade);
      #endif
    }
  #endif

  // torch light reflection
  float tc = 0.5+0.5*sin(16.0*viewDir.x)*sin(16.0*viewDir.z);
  waterRefl += torchColor*NL_TORCHLIGHT_INTENSITY*lit.x*tc*tc;

  // mask sky reflection under shade
  if (!env.end) {
    waterRefl *= 0.05 + lit.y*1.14;
  }

  #ifdef NL_WATER_REFL_MASK
    float mask = 0.05+0.05*sin(viewDir.x*12.0)*sin(viewDir.z*6.0);
    waterRefl *= smoothstep(mask-0.2,mask+0.13,viewDir.y*viewDir.y);
  #endif

  cosR = abs(cosR);
  float fresnel = calculateFresnel(cosR, 0.07);
  float opacity = 1.0-cosR;

  color.rgb *= 0.22*NL_WATER_TINT*(1.0-0.8*fresnel);
  color.a = mix(COLOR.a*NL_WATER_TRANSPARENCY, 1.0, opacity*opacity);

  #ifdef NL_WATER_WAVE
    if (camDist < 14.0) {
      wPos.y -= 0.5*(bump.x+0.5)*NL_WATER_BUMP;
    }
  #endif

  return vec4(waterRefl, fresnel);
}

#endif
