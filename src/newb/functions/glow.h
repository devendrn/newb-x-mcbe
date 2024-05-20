#ifndef GLOW_H
#define GLOW_H

#ifdef ALPHA_TEST
  #define GLOW_PIXEL(C) C.a>0.9875 && C.a<0.993
#else
  #define GLOW_PIXEL(C) C.a>0.9875 && C.a<0.995
#endif

vec3 glowDetect(vec4 diffuse) {
  // Texture alpha: diffuse.a
  // 252/255 = max glow
  // 253/255 = partial glow
  if (GLOW_PIXEL(diffuse)) {
    return  diffuse.rgb * (0.995-diffuse.a)/(0.995-0.9875);
  }
  return vec3(0.0,0.0,0.0);
}

vec3 glowDetectC(sampler2D tex, vec2 uv) {
  return glowDetect(texture2D(tex, uv));
}

vec3 nlGlow(sampler2D tex, vec2 uv, vec4 diffuse, float shimmer) {
  vec3 glow = glowDetect(diffuse);

  #ifdef NL_GLOW_LEAK
  // glow leak is done by interpolating 8 surrounding pixels
  // c3 c4 c5
  // c2    c6
  // c1 c8 c7
  const vec2 texSize = vec2(1024.0, 2048.0);
  const vec2 offset = 1.0 / texSize;

  vec3 c1 = glowDetectC(tex, uv - offset);
  vec3 c2 = glowDetectC(tex, uv + offset*vec2(-1, 0));
  vec3 c3 = glowDetectC(tex, uv + offset*vec2(-1, 1));
  vec3 c4 = glowDetectC(tex, uv + offset*vec2( 0, 1));
  vec3 c5 = glowDetectC(tex, uv + offset);
  vec3 c6 = glowDetectC(tex, uv + offset*vec2( 1, 0));
  vec3 c7 = glowDetectC(tex, uv + offset*vec2( 1,-1));
  vec3 c8 = glowDetectC(tex, uv + offset*vec2( 0,-1));

  vec2 p = uv * texSize;
  vec2 u = fract(p);
  //u *= u*(3.0 - 2.0*u);
  vec2 v = 1.0 - u;

  // corners
  vec3 g = mix(mix(c1, c3, u.y), mix(c7, c5, u.y), u.x);

  /*
  g = max(
    max(c1*min(v.x,v.y), c3*min(v.x,u.y)),
    max(c5*min(u.x,u.y), c7*min(u.x,v.y))
  );*/

  // sides
  g = max(g, max(max(c2*v.x, c4*u.y), max(c6*u.x, c8*v.y)));

  // apply attuenation and add to glow
  g = ((g*0.7 + 0.2)*g + 0.1)*g;
  glow = max(glow, g*NL_GLOW_LEAK);
  #endif

  #ifdef NL_GLOW_SHIMMER
  glow *= (0.3 + 0.9*shimmer);
  #endif

  return glow * NL_GLOW_TEX;
}

float nlGlowShimmer(vec3 cPos, float t) {
  float d = dot(cPos,vec3(1.0,1.0,1.0));
  float shimmer = sin(1.57*d + 0.7854*sin(d + 0.1*t) + 0.8*t);
  return shimmer * shimmer;
}

#endif
