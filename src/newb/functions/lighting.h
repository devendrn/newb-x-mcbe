#ifndef LIGHTING_H
#define LIGHTING_H

#include "detection.h"
#include "sky.h"
#include "utils.h"
#include "noise.h"
#include "clouds.h"

vec3 sunLightTint(float dayFactor, float rain) {
  float nightFactor = step(dayFactor, 0.0);
  float dawnFactor = 1.0-dayFactor*dayFactor;
  dawnFactor *= dawnFactor*dawnFactor;
  dawnFactor *= mix(1.0, dawnFactor*dawnFactor, nightFactor);
  vec3 tint = mix(NL_NOON_SUNLIGHT_COL, NL_NIGHT_MOONLIGHT_COL, nightFactor);
  tint = mix(tint, NL_DAWN_SUNLIGHT_COL, dawnFactor);
  tint = mix(tint, vec3_splat(dot(tint, vec3_splat(0.33))), rain);
  return tint;
}

vec3 nlLighting(
  sampler2D tex, nl_skycolor skycol, nl_environment env, vec3 wPos, out vec3 torchColor, vec3 COLOR,
  vec2 uv1, vec2 lit, bool isTree, float shade, highp float t, float renderdistance, float TIME_OF_DAY, vec3 CAMERA_POS
) {
  // all of these will be multiplied by tex uv1 in frag so functions should be divided by uv1 here

  vec3 light;

  if (env.underwater) {
    torchColor = NL_UNDERWATER_TORCH_COL;
  } else if (env.end) {
    torchColor = NL_END_TORCH_COL;
  } else if (env.nether) {
    torchColor = NL_NETHER_TORCH_COL;
  } else {
    torchColor = NL_OVERWORLD_TORCH_COL;
  }

  float torchAttenuation = (NL_TORCHLIGHT_INTENSITY*uv1.x)/(0.5-0.45*lit.x);

  #ifdef NL_BLINKING_TORCH
    torchAttenuation *= 1.0 - 0.19*noise1D(t*8.0);
  #endif

  vec3 torchLight = torchColor*torchAttenuation;
  float gameBrightness = texture2D(tex, vec2_splat(0.0)).g;
  float lum = 0.0;

  if (env.nether || env.end) {
    // nether & end lighting

    light = env.end ? NL_END_AMBIENT : NL_NETHER_AMBIENT;
    light *= 0.2*gameBrightness;

    lum = luminance(light);
    light += skycol.horizon/(1.0+lum);

  } else {
    // overworld lighting
    float nightFactor = step(env.dayFactor, 0.0);
    float dawnFactor = 1.0-env.dayFactor*env.dayFactor;
    dawnFactor *= dawnFactor*dawnFactor;
    dawnFactor *= mix(1.0, dawnFactor*dawnFactor, nightFactor);
    float nightIntensity = 1.0-(0.5+0.5*env.dayFactor);
    nightIntensity *= nightIntensity;

    float sunLightAttenuation = clamp(0.5*(((2.0*step(TIME_OF_DAY, 0.5)-1.0)*(wPos.x*cos(NL_SUN_PATH_YAW)+wPos.y*sin(NL_SUN_PATH_YAW))/renderdistance) + 1.0), 0.0, 1.0);
    sunLightAttenuation = mix(1.0, sunLightAttenuation*sunLightAttenuation, dawnFactor);
    sunLightAttenuation *= 1.0-0.4*env.rainFactor;

    // shadow cast by sun light
    float shadow = step(0.93, uv1.y);
    shadow = max(shadow, (1.0 - NL_SHADOW_INTENSITY + (0.6*NL_SHADOW_INTENSITY*nightIntensity))*lit.y);
    shadow *= shade > 0.8 ? 1.0 : 0.8;
    #ifdef NL_CLOUD_SHADOW
      // shadow cast by simple clouds
      vec3 mainLightDir = env.sunDir.y > 0.0 ? env.sunDir : env.moonDir;
      vec3 gPos = wPos + CAMERA_POS;
      float cloudRelativeHeight = gPos.y-187.0;
      vec2 projectionOffset = cloudRelativeHeight*mainLightDir.xz/mainLightDir.y;
      vec2 projectedPos = gPos.xz + projectionOffset;
      float cloudFade = smoothstep(1.0, 0.5, length(0.002*(wPos.xz + projectionOffset)));
      cloudFade *= (1.0-dawnFactor*dawnFactor)*clamp(-0.12*(cloudRelativeHeight-7.0), 0.0, 1.0);
      shadow *= smoothstep(0.6, 0.0, cloudNoise2D(projectedPos*NL_CLOUD1_SCALE, t, env.rainFactor)*cloudFade);
    #endif

    // direct light from top
    light = (NL_SUNLIGHT_INTENSITY*shadow*sunLightAttenuation)*sunLightTint(env.dayFactor, env.rainFactor);

    // sky ambient
    lum = luminance(light);
    light += (skycol.horizon + skycol.zenith)*(uv1.y/(1.0+lum));

  }

  // torch light
  lum = luminance(light);
  light += torchLight/(1.0+lum);

  // game min brightness
  lum = luminance(light);
  light += vec3_splat(gameBrightness*(1.5/(1.0+lum)));

  // darken at crevices
  light *= COLOR.g > 0.35 ? 1.0 : 0.8;

  // brighten tree leaves
  if (isTree) {
    light *= 1.25;
  }

  return light;
}

void nlUnderwaterLighting(inout vec3 light, inout vec3 pos, vec2 lit, vec2 uv1, vec3 tiledCpos, vec3 cPos, highp float t, vec3 horizonCol) {
  if (uv1.y < 0.9) {
    float caustics = disp(tiledCpos, NL_WATER_WAVE_SPEED*t);
    caustics *= 3.0*caustics;
    light += NL_UNDERWATER_BRIGHTNESS + NL_CAUSTIC_INTENSITY*caustics*(0.15 + lit.y + lit.x*0.7);
  }
  light *= mix(normalize(horizonCol), vec3_splat(0.6), lit.y*0.6);
  #ifdef NL_UNDERWATER_WAVE
    pos.xy += NL_UNDERWATER_WAVE*min(0.05*pos.z,0.6)*sin(t*1.2 + dot(cPos,vec3_splat(PI_HALF)));
  #endif
}

vec3 nlEntityLighting(nl_environment env, vec3 pos, vec4 normal, mat4 world, vec4 tileLightCol, vec4 overlayCol, vec3 horizonEdgeCol, float t) {
  float intensity;
  #ifdef FANCY
    vec3 N = normalize(mul(world, normal)).xyz;
    N.y *= tileLightCol.w;
    N.xz *= N.xz;

    intensity = 0.75 + N.y*0.25 - N.x*0.1 + N.z*0.1;
    intensity *= intensity;
  #else
    intensity = (0.7+0.3*abs(normal.y))*(0.9+0.1*abs(normal.x));
  #endif

  intensity *= tileLightCol.b*tileLightCol.b*NL_SUNLIGHT_INTENSITY*1.2;
  intensity += overlayCol.a * 0.35;

  float factor = tileLightCol.b-tileLightCol.r;
  vec3 light = intensity*vec3(1.0-2.8*factor,1.0-2.7*factor,1.0);
  light *= 1.0-0.3*step(0.0,pos.y);
  light += 0.55*horizonEdgeCol*tileLightCol.x;

  // nether, end, underwater tint
  if (env.nether) {
    light *= tileLightCol.x*NL_NETHER_AMBIENT*0.5;
  } else if (env.end) {
    light *= NL_END_AMBIENT;
  } else if (env.underwater) {
    light += NL_UNDERWATER_BRIGHTNESS;
    light *= mix(normalize(horizonEdgeCol),vec3(1.0,1.0,1.0),tileLightCol.x*0.5);
    light += NL_CAUSTIC_INTENSITY*max(tileLightCol.x-0.46,0.0)*(0.5+0.5*sin(t + dot(pos,vec3_splat(1.5)) ));
  }

  return light;
}

float nlEntityEdgeHighlight(vec4 edgemap) {
  #ifdef NL_ENTITY_EDGE_HIGHLIGHT
    vec2 len = min(abs(edgemap.xy),abs(edgemap.zw));
    len *= len;
    len *= len;
    float ambient = len.x + len.y*(1.0-len.x);
    return NL_ENTITY_BRIGHTNESS + ambient*NL_ENTITY_EDGE_HIGHLIGHT;
  #else
    return 1.0;
  #endif
}

vec4 nlEntityEdgeHighlightPreprocess(vec2 texcoord) {
  vec4 edgeMap = fract(vec4(texcoord*128.0, texcoord*256.0));
  return 2.0*step(edgeMap, vec4_splat(0.5)) - 1.0;
}

vec4 nlLavaNoise(vec3 tiledCpos, float t) {
  t *=  NL_LAVA_NOISE_SPEED;
  vec3 p = PI_HALF*tiledCpos;
  float d = fastVoronoi2(4.3*tiledCpos.xz + t, 2.0);
  float n = sin(2.0*(p.x+p.y+p.z) + 1.7*sin(2.0*d + 4.0*(p.x-p.z)) + 4.0*t);
  n = 0.3*d*d +  0.7*n*n;
  n *= n;
  return vec4(mix(vec3(0.7, 0.4, 0.0), vec3_splat(1.5), n),n);
}

#endif
