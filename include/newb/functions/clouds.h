#ifndef CLOUDS_H
#define CLOUDS_H

#include "noise.h"

// simple clouds 2D noise
float cloudNoise2D(vec2 p, highp float t, float rain) {
  t *= NL_CLOUD1_SPEED;
  p += t;
  p.x += sin(p.y*0.4 + t);

  vec2 p0 = floor(p);
  vec2 u = p-p0;
  u *= u*(3.0-2.0*u);
  vec2 v = 1.0-u;

  // rain transition
  vec2 d = vec2(0.09+0.5*rain,0.089+0.5*rain*rain);

  return v.y*(randt(p0,d)*v.x + randt(p0+vec2(1.0,0.0),d)*u.x) +
         u.y*(randt(p0+vec2(0.0,1.0),d)*v.x + randt(p0+vec2(1.0,1.0),d)*u.x);
}

// simple clouds
vec4 renderCloudsSimple(vec3 pos, highp float t, float rain, vec3 zenithCol, vec3 horizonCol, vec3 fogCol) {
  pos.xz *= NL_CLOUD1_SCALE;

  float cloudAlpha = cloudNoise2D(pos.xz, t, rain);
  float cloudShadow = cloudNoise2D(pos.xz*0.91, t, rain);

  vec4 color = vec4(0.02,0.04,0.05,cloudAlpha);

  color.rgb += fogCol;
  color.rgb *= 1.0 - 0.5*cloudShadow*step(0.0, pos.y);

  color.rgb += zenithCol*0.7;
  color.rgb *= 1.0 - 0.4*rain;

  return color;
}

// rounded clouds

// apply bevel with radius r at at corner (1.0)
float bevel(float x, float r) {
  float y = max(x-r,0.0)/(1.0-r);
  return (1.0-r)*(1.0-sqrt(1.0-y*y));
}

// rounded clouds 3D density map
float cloudDf(vec3 pos, float rain) {
  vec2 p0 = floor(pos.xz);
  vec2 u = smoothstep(0.99*NL_CLOUD2_SHAPE,0.995,pos.xz-p0);
  vec2 v = 1.0 - u;

  // rain transition
  vec2 t = vec2(0.1001+0.2*rain, 0.0999+0.2*rain*rain);

  // mix noise gradients
  float n = v.y*(randt(p0,t)*v.x + randt(p0+vec2(1.0,0.0),t)*u.x) +
            u.y*(randt(p0+vec2(0.0,1.0),t)*v.x + randt(p0+vec2(1.0,1.0),t)*u.x);

  // round y
  float b = 0.5*bevel(2.0*abs(pos.y-0.5), 0.3);
  return smoothstep(b,0.5+b,n);
}

vec4 renderClouds(vec3 vDir, vec3 vPos, float rain, float time, vec3 fogCol, vec3 skyCol) {
  // local cloud pos
  vec3 pos = vPos;
  pos.y = 0.0;
  pos.xz = NL_CLOUD2_SCALE*(vPos.xz + vec2(1.0,0.5)*(time*NL_CLOUD2_VELOCIY));

  // scaled ray offset
  float height = 7.0*(NL_CLOUD2_THICKNESS + rain*(NL_CLOUD2_RAIN_THICKNESS - NL_CLOUD2_THICKNESS));
  vec3 deltaP;
  deltaP.xyz = (NL_CLOUD2_SCALE*height/float(NL_CLOUD2_STEPS))*vDir.xyz/(0.02+0.98*abs(vDir.y));
  deltaP.y = abs(deltaP.y);

  // alpha, gradient, ray depth temp
  vec3 d = vec3(0.0,1.0,1.0);
  for (int i=0; i<NL_CLOUD2_STEPS; i++) {
    pos += deltaP;
    float m = cloudDf(pos.xyz, rain);
    d.x += m*NL_CLOUD2_DENSITY*(1.0-d.x)/float(NL_CLOUD2_STEPS);
    d.y = mix(d.y, pos.y, d.z);
    d.z *= 1.0 - m;

    if (d.x > 0.99) {
      break;
    }
  }

  if (vPos.y > 0.0) {
    d.y = 1.0 - d.y;
  }

  d.y = 1.0-0.7*d.y*d.y;

  vec4 col = vec4(0.6*skyCol, d.x);
  col.rgb += (vec3(0.03,0.05,0.05) + 0.8*fogCol)*d.y;
  col.rgb *= 1.0 - 0.5*rain;

  return col;
}

// aurora is rendered on clouds layer
#ifdef NL_AURORA
vec4 renderAurora(vec3 p, float t, float rain, vec3 skyCol) {
  t *= NL_AURORA_VELOCITY;
  p.xz *= NL_AURORA_SCALE;
  p.xz += 0.05*sin(p.x*4.0 + 20.0*t);

  float d0 = sin(p.x*0.1 + t + sin(p.z*0.2));
  float d1 = sin(p.z*0.1 - t + sin(p.x*0.2));
  float d2 = sin(p.z*0.1 + 1.0*sin(d0 + d1*2.0) + d1*2.0 + d0*1.0);
  d0 *= d0; d1 *= d1; d2 *= d2;
  d2 = d0/(1.0 + d2/NL_AURORA_WIDTH);

  float mask = (1.0-0.8*rain)/(1.0 + 64.0*skyCol.b*skyCol.b);
  return vec4(NL_AURORA*mix(NL_AURORA_COL1,NL_AURORA_COL2,d1),1.0)*d2*mask;
}
#endif

#endif
