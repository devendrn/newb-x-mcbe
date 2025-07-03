#ifndef LIGHTING_H
#define LIGHTING_H

#include "detection.h"
#include "sky.h"
#include "constants.h"
#include "noise.h"
#include "clouds.h"

// sunlight tinting
vec3 sunLightTint(float dayFactor, float rain, vec3 FOG_COLOR) {

  float tintFactor = FOG_COLOR.g + 0.1*FOG_COLOR.r;
  float noon = clamp((tintFactor-0.37)/0.45,0.0,1.0);
  float morning = clamp((tintFactor-0.05)*3.125,0.0,1.0);

  vec3 clearTint = mix(
    mix(NL_NIGHT_SUN_COL, NL_MORNING_SUN_COL, morning),
    mix(NL_MORNING_SUN_COL, NL_NOON_SUN_COL, noon),
    dayFactor
  );

  float r = 1.0-rain;
  r *= r;

  return mix(vec3(0.65,0.65,0.75), clearTint, r*r);
}

vec3 nlLighting(
  nl_skycolor skycol, nl_environment env, vec3 wPos, out vec3 torchColor, vec3 COLOR, vec3 FOG_COLOR,
  vec2 uv1, vec2 lit, bool isTree, float shade, highp float t
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

  float torchAttenuation = (NL_TORCH_INTENSITY*uv1.x)/(0.5-0.45*lit.x);

  #ifdef NL_BLINKING_TORCH
    torchAttenuation *= 1.0 - 0.19*noise1D(t*8.0);
  #endif

  vec3 torchLight = torchColor*torchAttenuation;

  if (env.nether || env.end) {
    // nether & end lighting

    light = env.end ? NL_END_AMBIENT : NL_NETHER_AMBIENT;

    light += skycol.horizon + torchLight*0.5;
  } else {
    // overworld lighting

    float dayFactor = min(dot(FOG_COLOR.rgb, vec3(0.5,0.4,0.4))*(1.0 + 1.9*env.rainFactor), 1.0); // use env.dayFactor here?
    float nightFactor = 1.0-dayFactor*dayFactor;
    float rainDim = min(FOG_COLOR.g, 0.25)*env.rainFactor;
    float lightIntensity = NL_SUN_INTENSITY*(1.0 - rainDim)*(1.0 + NL_NIGHT_BRIGHTNESS*nightFactor);

    // min ambient in caves
    light = vec3_splat((1.35+NL_CAVE_BRIGHTNESS)*(1.0-uv1.x)*(1.0-uv1.y));

    // sky ambient
    light += mix(skycol.horizon, skycol.zenith, 0.5+uv1.y-0.5*lit.y)*(lit.y*(3.0-2.0*uv1.y)*(1.3 + (4.0*nightFactor) - rainDim));

    // shadow cast by top light
    float shadow = step(0.93, uv1.y);
    shadow = max(shadow, (1.0 - NL_SHADOW_INTENSITY + (0.6*NL_SHADOW_INTENSITY*nightFactor))*lit.y);
    shadow *= shade > 0.8 ? 1.0 : 0.8;

    // shadow cast by simple cloud
    #ifdef NL_CLOUD_SHADOW
      shadow *= smoothstep(0.6, 0.1, cloudNoise2D(2.0*wPos.xz*NL_CLOUD1_SCALE, t, env.rainFactor));
    #endif

    // direct light from top
    float dirLight = shadow*(1.0-uv1.x*nightFactor)*lightIntensity;
    light += dirLight*sunLightTint(dayFactor, env.rainFactor, FOG_COLOR);

    // extra indirect light
    light += vec3_splat(0.3*lit.y*uv1.y*(1.2-shadow)*lightIntensity);

    // torch light
    light += torchLight*(1.0-(max(shadow, 0.65*lit.y)*dayFactor*(1.0-0.3*env.rainFactor)));
  }

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
    pos.xy += NL_UNDERWATER_WAVE*min(0.05*pos.z,0.6)*sin(t*1.2 + dot(cPos,vec3_splat(NL_CONST_PI_HALF)));
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

  intensity *= tileLightCol.b*tileLightCol.b*NL_SUN_INTENSITY*1.2;
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
  vec3 p = NL_CONST_PI_HALF*tiledCpos;
  float d = fastVoronoi2(4.3*tiledCpos.xz + t, 2.0);
  float n = sin(2.0*(p.x+p.y+p.z) + 1.7*sin(2.0*d + 4.0*(p.x-p.z)) + 4.0*t);
  n = 0.3*d*d +  0.7*n*n;
  n *= n;
  return vec4(mix(vec3(0.7, 0.4, 0.0), vec3_splat(1.5), n),n);
}

#endif
