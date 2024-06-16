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

// rounded clouds 3D density map
float cloudDf(vec3 pos, float rain) {
    #ifdef NL_CLOUD2_NATURAL_SHAPE
    pos.xz += NL_CLOUDS_NATURAL_SHAPE_SCALE_XZ*noise(pos.xyz);
	pos.y += NL_CLOUDS_NATURAL_SHAPE_SCALE_Y*noise(pos.xyz);
    #endif
    
	#ifdef NL_CLOUD2_NOISE
	pos.xz += NL_CLOUD2_NOISE_INTENSITY*noise(NL_CLOUD2_NOISE_SCALE*pos.xyz);
    #endif
    
    #ifdef NL_CLOUD2_RAND
	pos.xyz += 0.2*rand(0.2*pos.xz);
    #endif
  vec2 p0 = floor(pos.xz);
  vec2 u = smoothstep(0.999*NL_CLOUD2_SHAPE, 1.0, pos.xz-p0);
  
  // rain transition
  vec2 t = vec2(NL_CLOUD2_TRANSITION1+0.2*rain, NL_CLOUD2_TRANSITION2+0.2*rain*rain);

  float n = mix(
    mix(randt(p0, t),randt(p0+vec2(1.0,0.0), t), u.x),
    mix(randt(p0+vec2(0.0,1.0), t),randt(p0+vec2(1.0,1.0), t), u.x),
    u.y
  );
	
  // round y
  float b = 1.0 - 1.9*smoothstep(NL_CLOUD2_SHAPE, 2.0 - NL_CLOUD2_SHAPE, 2.0*abs(pos.y-0.5));
  return smoothstep(0.2, 1.0, n * b);
}

vec4 renderClouds(vec3 vDir, vec3 vPos, float rain, float time, vec3 fogCol, vec3 skyCol) {

  float height = 7.0*mix(NL_CLOUD2_THICKNESS, NL_CLOUD2_RAIN_THICKNESS, rain);

  // scaled ray offset
  vec3 deltaP;
  deltaP.y = 1.0;
  deltaP.xz = (NL_CLOUD2_SCALE*height)*vDir.xz/(0.02+0.98*abs(vDir.y));
  //deltaP.xyz = (NL_CLOUD2_SCALE*height)*vDir.xyz;
  //deltaP.y = abs(deltaP.y);
  
  // local cloud pos
  vec3 pos;
  pos.y = 0.0;
  pos.xz = NL_CLOUD2_SCALE*(vPos.xz + vec2(1.0,0.5)*(time*NL_CLOUD2_VELOCIY));
  pos += deltaP;

  deltaP /= -float(NL_CLOUD2_STEPS);
  
  // alpha, gradient
  vec2 d = vec2(0.0,NL_CLOUDS_SHAPE_DENSITY);
  for (int i=1; i<=NL_CLOUD2_STEPS; i++) {
    float m = cloudDf(pos, rain);
    
    d.x += m;
    d.y = mix(d.y, pos.y, m);
    
    //if (d.x == 0.0 && i > NL_CLOUD2_STEPS/2) {
    //	break;
    //} 

    pos += deltaP;
  }
  //d.x *= vDir.y*vDir.y; 
  d.x *= smoothstep(0.03, 0.1, d.x);
  d.x = d.x / ((float(NL_CLOUD2_STEPS)/NL_CLOUD2_DENSITY) + d.x);
  
  if (vPos.y > 0.0) { // view from bottom
    d.y = 1.0 - d.y;
  }

  d.y = 1.0 - NL_CLOUDS_SHADOW_INTENSITY*d.y*d.y;
 
  vec4 col = vec4(0.8*skyCol, d.x);
  col.rgb += (vec3(0.03,0.05,0.05) + NL_CLOUDS_BRIGHTNESS*fogCol)*d.y;
  col.rgb *= 1.0 - 0.5*rain;

  return col;
}

// aurora is rendered on clouds layer
#ifdef NL_AURORA
vec4 renderAurora(vec3 p, float t, float rain, vec3 FOG_COLOR) {
  t *= NL_AURORA_VELOCITY;
  p.xz *= NL_AURORA_SCALE;
  p.xz += 0.05*sin(p.x*4.0 + 20.0*t);

  float d0 = sin(p.x*0.1 + t + sin(p.z*0.2));
  float d1 = sin(p.z*0.1 - t + sin(p.x*0.2));
  float d2 = sin(p.z*0.1 + 1.0*sin(d0 + d1*2.0) + d1*2.0 + d0*1.0);
  d0 *= d0; d1 *= d1; d2 *= d2;
  d2 = d0/(1.0 + d2/NL_AURORA_WIDTH);

  float mask = (1.0-0.8*rain)*max(1.0 - 3.0*max(FOG_COLOR.b, FOG_COLOR.g), 0.0);
  return vec4(NL_AURORA*mix(NL_AURORA_COL1,NL_AURORA_COL2,d1),1.0)*d2*mask;
}
#endif

#endif
