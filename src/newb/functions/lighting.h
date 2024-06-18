#ifndef LIGHTING_H
#define LIGHTING_H

#include "constants.h"
#include "noise.h"

#define SHADOW_EDGE 0.3

// Uniforms for shadow calculations
uniform vec3 lightPosition; // Position of the light source
uniform float SHADOW_DISTANCE; // Maximum distance for shadow effect

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
  vec3 wPos, out vec3 torchColor, vec3 COLOR, vec3 FOG_COLOR, float rainFactor, vec2 uv1, vec2 lit, bool isTree,
  vec3 horizonCol, vec3 zenithCol, float shade, bool end, bool nether, bool underwater, highp float t
) {
  vec3 light;

  if (underwater) {
    torchColor = NL_UNDERWATER_TORCH_COL;
  } else if (end) {
    torchColor = NL_END_TORCH_COL;
  } else if (nether) {
    torchColor = NL_NETHER_TORCH_COL;
  } else {
    torchColor = NL_OVERWORLD_TORCH_COL;
  }

  float torchAttenuation = (NL_TORCH_INTENSITY*uv1.x)/(0.5-0.45*lit.x);

#ifdef NL_BLINKING_TORCH
  torchAttenuation *= 1.0 - 0.12*noise1D(t*9.0);
#endif

  vec3 torchLight = torchColor*torchAttenuation;

  if (nether || end) {
    // nether & end lighting

    light = end ? NL_END_AMBIENT : NL_NETHER_AMBIENT;

    light += horizonCol + torchLight*0.5;
  } else {
    // overworld lighting

    float dayFactor = min(dot(FOG_COLOR.rgb, vec3(0.5,0.4,0.4))*(1.0 + 1.9*rainFactor), 1.0);
    float nightFactor = 1.0-dayFactor*dayFactor;
    float rainDim = min(FOG_COLOR.g, 0.25)*rainFactor;
    float lightIntensity = NL_SUN_INTENSITY*(3.0 - rainDim)*(1.0 + NL_NIGHT_BRIGHTNESS*nightFactor);

    // min ambient in caves
    light = vec3_splat((1.35+NL_CAVE_BRIGHTNESS)*(1.0-uv1.x)*(1.0-uv1.y));

    // sky ambient
    light += mix(horizonCol,zenithCol,0.5+uv1.y-0.5*lit.y)*(lit.y*(3.0-2.0*uv1.y)*(1.3 + (4.0*nightFactor) - rainDim));

    // shadow cast by top light
    float shadow = step(SHADOW_EDGE, uv1.y);
    shadow = max(shadow, (1.0 - NL_SHADOW_INTENSITY + (0.6*NL_SHADOW_INTENSITY*nightFactor))*lit.y);
    shadow *= shade > 0.8 ? 1.0 : 0.8;

    // shadow cast by simple cloud
    #ifdef NL_CLOUD_SHADOW
      shadow *= 1.0 - cloudNoise2D(wPos.xz*NL_CLOUD1_SCALE, t, rainFactor);
    #endif

    // direct light from top
    float dirLight = shadow*(1.5-uv1.x*nightFactor)*lightIntensity;
    light += dirLight*sunLightTint(dayFactor, rainFactor, FOG_COLOR);

    // extra indirect light
    light += vec3_splat(0.3*lit.y*uv1.y*(1.0-shadow)*lightIntensity);

    // torch light
    light += torchLight*(1.0-(max(shadow, 0.65*lit.y)*dayFactor*(1.0-0.3*rainFactor)));
  }

  // Calculate the distance from the light source to the fragment
  float distance = length(lightPosition - wPos);

  // Calculate the shadow factor using smoothstep
  float shadowFactor = smoothstep(0.0, SHADOW_DISTANCE, distance);

  // Apply the shadow factor to the lighting calculation
  light *= shadowFactor;

  // Darken at crevices
  float col_max = max(COLOR.r, max(COLOR.g, COLOR.b));
  if (col_max < 0.7) {
      light *= 0.31;    //adjust this number (max 0.7)
  }

  // Brighten tree leaves
  if (isTree) {
    light *= 1.25;
  }

  return light;
}

void nlUnderwaterLighting(inout vec3 light, inout vec3 pos, vec2 lit, vec2 uv1, vec3 tiledCpos, vec3 cPos, highp float t, vec3 horizonCol) {
  // soft caustic effect
  if (uv1.y < 0.9) {
    float caustics = disp(tiledCpos*vec3(1.0,0.1,1.0), t);
    caustics += (1.0 + sin(t + (cPos.x+cPos.z)*NL_CONST_PI_HALF));
    light += NL_UNDERWATER_BRIGHTNESS + NL_CAUSTIC_INTENSITY*caustics*(0.1 + lit.y + lit.x*0.7);
  }
  light *= mix(normalize(horizonCol), vec3(1.0,1.0,1.0), lit.y*0.6);
#ifdef NL_UNDERWATER_WAVE
  pos.xy += NL_UNDERWATER_WAVE*min(0.05*pos.z,0.6)*sin(t*1.2 + dot(cPos,vec3_splat(NL_CONST_PI_HALF)));
#endif
}

vec3 nlActorLighting(vec3 pos, vec4 normal, mat4 world, vec4 tileLightCol, vec4 overlayCol, vec3 horizonCol, bool nether, bool underWater, bool end, float t) {
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
  light += 0.55*horizonCol*tileLightCol.x;

  // nether, end, underwater tint
  if (nether) {
    light *= tileLightCol.x*NL_NETHER_AMBIENT*0.5;
  } else if (end) {
    light *= NL_END_AMBIENT;
  } else if (underWater) {
    light += NL_UNDERWATER_BRIGHTNESS;
    light *= mix(normalize(horizonCol),vec3(1.0,1.0,1.0),tileLightCol.x*0.5);
    light += NL_CAUSTIC_INTENSITY*max(tileLightCol.x-0.46,0.0)*(0.5+0.5*sin(t + dot(pos,vec3_splat(1.5)) ));
  }

  return light;
}

#endif
