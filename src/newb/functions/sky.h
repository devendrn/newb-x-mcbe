#ifndef SKY_H
#define SKY_H

#include "detection.h"

struct nl_skycolor {
  vec3 zenith;
  vec3 horizon;
  vec3 horizonEdge;
};

// rainbow spectrum
vec3 spectrum(float x) {
    vec3 s = vec3(x-0.5, x, x+0.5);
    s = smoothstep(1.0,0.0,abs(s));
    return s*s;
}

vec3 getUnderwaterCol(vec3 FOG_COLOR) {
  return 2.0*NL_UNDERWATER_TINT*FOG_COLOR*FOG_COLOR;
}

vec3 getEndZenithCol() {
  return NL_END_ZENITH_COL;
}

vec3 getEndHorizonCol() {
  return NL_END_HORIZON_COL;
}

// values used for getting sky colors
vec3 getSkyFactors(vec3 FOG_COLOR) {
  vec3 factors = vec3(
    max(FOG_COLOR.r*0.6, max(FOG_COLOR.g, FOG_COLOR.b)), // intensity val
    1.5*max(FOG_COLOR.r-FOG_COLOR.b, 0.0), // viewing sun
    min(FOG_COLOR.g, 0.26) // rain brightness
  );

  factors.z *= factors.z;

  return factors;
}

vec3 getZenithCol(float rainFactor, vec3 FOG_COLOR, vec3 fs) {
  vec3 zenithCol = NL_NIGHT_ZENITH_COL*(1.0-FOG_COLOR.b);
  zenithCol += NL_DAWN_ZENITH_COL*((0.7*fs.x*fs.x) + (0.4*fs.x) + fs.y);
  zenithCol = mix(zenithCol, (0.7*fs.x*fs.x + 0.3*fs.x)*NL_DAY_ZENITH_COL, fs.x*fs.x);
  zenithCol = mix(zenithCol*(1.0+0.5*rainFactor), NL_RAIN_ZENITH_COL*fs.z*13.2, rainFactor);

  return zenithCol;
}

vec3 getHorizonCol(float rainFactor, vec3 FOG_COLOR, vec3 fs) {
  vec3 horizonCol = NL_NIGHT_HORIZON_COL*(1.0-FOG_COLOR.b); 
  horizonCol += NL_DAWN_HORIZON_COL*(((0.7*fs.x*fs.x) + (0.3*fs.x) + fs.y)*1.9); 
  horizonCol = mix(horizonCol, 2.0*fs.x*NL_DAY_HORIZON_COL, fs.x*fs.x);
  horizonCol = mix(horizonCol, NL_RAIN_HORIZON_COL*fs.z*19.6, rainFactor);

  return horizonCol;
}

// tinting on horizon col
vec3 getHorizonEdgeCol(vec3 horizonCol, float rainFactor, vec3 FOG_COLOR) {
  float val = 2.1*(1.1-FOG_COLOR.b)*FOG_COLOR.g*(1.0-rainFactor);
  horizonCol *= vec3_splat(1.0-val) + NL_DAWN_EDGE_COL*val;

  return horizonCol;
}

// 1D sky with three color gradient
vec3 renderOverworldSky(nl_skycolor skycol, vec3 viewDir) {
  float h = 1.0-viewDir.y*viewDir.y;
  float hsq = h*h;
  if (viewDir.y < 0.0) {
    hsq = 0.4 + 0.6*hsq*hsq;
  }

  // gradient 1  h^16
  // gradient 2  h^8 mix h^2
  float gradient1 = hsq*hsq;
  gradient1 *= gradient1;
  float gradient2 = 0.6*gradient1 + 0.4*hsq;
  gradient1 *= gradient1;

  vec3 sky = mix(skycol.horizon, skycol.horizonEdge, gradient1);
  sky = mix(skycol.zenith, skycol.horizon, gradient2);

  return sky;
}

// sunrise/sunset bloom
vec3 getSunBloom(float viewDirX, vec3 horizonEdgeCol, vec3 FOG_COLOR) {
  float factor = FOG_COLOR.r/length(FOG_COLOR);
  factor *= factor;
  factor *= factor;

  float spread = smoothstep(0.0, 1.0, abs(viewDirX));
  float sunBloom = spread*spread;
  sunBloom = 0.5*spread + sunBloom*sunBloom*sunBloom*1.5;

  return NL_MORNING_SUN_COL*horizonEdgeCol*(sunBloom*factor*factor);
}


vec3 renderEndSky(vec3 horizonCol, vec3 zenithCol, vec3 viewDir, float t) {
  t *= 0.1;
  float a = atan2(viewDir.x, viewDir.z);

  float n1 = 0.5 + 0.5*sin(3.0*a + t + 10.0*viewDir.x*viewDir.y);
  float n2 = 0.5 + 0.5*sin(5.0*a + 0.5*t + 5.0*n1 + 0.1*sin(40.0*a -4.0*t));

  float waves = 0.7*n2*n1 + 0.3*n1;

  float grad = 0.5 + 0.5*viewDir.y;
  float streaks = waves*(1.0 - grad*grad*grad);
  streaks += (1.0-streaks)*smoothstep(1.0-waves, -1.0, viewDir.y);

  float f = 0.3*streaks + 0.7*smoothstep(1.0, -0.5, viewDir.y);
  float h = streaks*streaks;
  float g = h*h;
  g *= g;

  vec3 sky = mix(zenithCol, horizonCol, f*f);
  sky += (0.1*streaks + 2.0*g*g*g + h*h*h)*vec3(2.0,0.5,0.0);
  sky += 0.25*streaks*spectrum(sin(2.0*viewDir.x*viewDir.y+t));

  return sky;
}

vec3 nlRenderSky(nl_skycolor skycol, nl_environment env, vec3 viewDir, vec3 FOG_COLOR, float t) {
  vec3 sky;
  viewDir.y = -viewDir.y;

  if (env.end) {
    sky = renderEndSky(skycol.horizon, skycol.zenith, viewDir, t);
  } else {
    sky = renderOverworldSky(skycol, viewDir);
    #ifdef NL_RAINBOW
      sky += mix(NL_RAINBOW_CLEAR, NL_RAINBOW_RAIN, env.rainFactor)*spectrum((viewDir.z+0.6)*8.0)*max(viewDir.y, 0.0)*FOG_COLOR.g;
    #endif
    #ifdef NL_UNDERWATER_STREAKS
      if (env.underwater) {
        float a = atan2(viewDir.x, viewDir.z);
        float grad = 0.5 + 0.5*viewDir.y;
        grad *= grad;
        float spread = (0.5 + 0.5*sin(3.0*a + 0.2*t + 2.0*sin(5.0*a - 0.4*t)));
        spread *= (0.5 + 0.5*sin(3.0*a - sin(0.5*t)))*grad;
        spread += (1.0-spread)*grad;
        float streaks = spread*spread;
        streaks *= streaks;
        streaks = (spread + 3.0*grad*grad + 4.0*streaks*streaks);
        sky += 2.0*streaks*skycol.horizon;
      } else 
    #endif
    if (!env.nether) {
      sky += getSunBloom(viewDir.x, skycol.horizonEdge, FOG_COLOR);
    }
  }

  return sky;
}

// sky reflection on plane
vec3 getSkyRefl(nl_skycolor skycol, nl_environment env, vec3 viewDir, vec3 FOG_COLOR, float t, float h) {
  viewDir.y = -viewDir.y;
  vec3 refl = nlRenderSky(skycol, env, viewDir, FOG_COLOR, t);

  if (!(env.underwater || env.nether)) {
    float specular = smoothstep(0.7, 0.0, abs(viewDir.z));
    specular *= 2.0*max(FOG_COLOR.r-FOG_COLOR.b, 0.0);
    specular *= specular*viewDir.x;
    refl += skycol.horizonEdge * specular * specular;
  }

  return refl;
}

// shooting star
vec3 nlRenderShootingStar(vec3 viewDir, vec3 FOG_COLOR, float t) {
  // transition vars
  float h = t / (NL_SHOOTING_STAR_DELAY + NL_SHOOTING_STAR_PERIOD);
  float h0 = floor(h);
  t = (NL_SHOOTING_STAR_DELAY + NL_SHOOTING_STAR_PERIOD) * (h-h0);
  t = min(t/NL_SHOOTING_STAR_PERIOD, 1.0);
  float t0 = t*t;
  float t1 = 1.0-t0;
  t1 *= t1; t1 *= t1; t1 *= t1;

  // randomize size, rotation, add motion, add skew
  float r = fract(sin(h0) * 43758.545313);
  float a = 6.2831*r;
  float cosa = cos(a);
  float sina = sin(a);
  vec2 uv = viewDir.xz * (6.0 + 4.0*r);
  uv = vec2(cosa*uv.x + sina*uv.y, -sina*uv.x + cosa*uv.y);
  uv.x += t1 - t;
  uv.x -= 2.0*r + 3.5;
  uv.y += viewDir.y * 3.0;

  // draw star
  float g = 1.0-min(abs((uv.x-0.95))*20.0, 1.0); // source glow
  float s = 1.0-min(abs(8.0*uv.y), 1.0); // line
  s *= s*s*smoothstep(-1.0+1.96*t1, 0.98-t, uv.x); // decay tail
  s *= s*s*smoothstep(1.0, 0.98-t0, uv.x); // decay source
  s *= 1.0-t1; // fade in
  s *= 1.0-t0; // fade out
  s *= 0.7 + 16.0*g*g;
  s *= max(1.0-FOG_COLOR.r-FOG_COLOR.g-FOG_COLOR.b, 0.0); // fade out during day
  return s*vec3(0.8, 0.9, 1.0);
}

nl_skycolor nlUnderwaterSkyColors(float rainFactor, vec3 FOG_COLOR) {
  nl_skycolor s;
  s.zenith = getUnderwaterCol(FOG_COLOR);
  s.horizon = s.zenith;
  s.horizonEdge = s.zenith;
  return s;
}

nl_skycolor nlEndSkyColors(float rainFactor, vec3 FOG_COLOR) {
  nl_skycolor s;
  s.zenith = getEndZenithCol();
  s.horizon = getEndHorizonCol();
  s.horizonEdge = s.horizon;
  return s;
}

nl_skycolor nlOverworldSkyColors(float rainFactor, vec3 FOG_COLOR) {
  nl_skycolor s;
  vec3 fs = getSkyFactors(FOG_COLOR);
  s.zenith= getZenithCol(rainFactor, FOG_COLOR, fs);
  s.horizon= getHorizonCol(rainFactor, FOG_COLOR, fs);
  s.horizonEdge= getHorizonEdgeCol(s.horizon, rainFactor, FOG_COLOR);
  return s;
}

nl_skycolor nlSkyColors(nl_environment env, vec3 FOG_COLOR) {
  nl_skycolor s;
  if (env.underwater) {
    s = nlUnderwaterSkyColors(env.rainFactor, FOG_COLOR);
  } else if (env.end) {
    s = nlEndSkyColors(env.rainFactor, FOG_COLOR);
  } else {
    s = nlOverworldSkyColors(env.rainFactor, FOG_COLOR);
  }
  return s;
}

#endif
