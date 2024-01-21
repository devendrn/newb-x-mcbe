#ifndef SKY_H
#define SKY_H

vec3 getUnderwaterCol(vec3 FOG_COLOR) {
  return 2.0*NL_UNDERWATER_TINT*FOG_COLOR*FOG_COLOR;
}

vec3 getEndZenithCol() {
  return NL_END_ZENITH_COL;
}

vec3 getEndHorizonCol() {
  return NL_END_HORIZON_COL;
}

vec3 getZenithCol(float rainFactor, vec3 FOG_COLOR) {
  // value needs tweaking
  float val = max(FOG_COLOR.r*0.6, max(FOG_COLOR.g, FOG_COLOR.b));

  // zenith color
  vec3 zenithCol = (0.77*val*val + 0.33*val)*NL_DAY_ZENITH_COL;
  zenithCol += NL_NIGHT_ZENITH_COL*(1.0-FOG_COLOR.b);

  // rain sky
  float brightness = min(FOG_COLOR.g, 0.26);
  brightness *= brightness*13.2;
  return mix(zenithCol*(1.0+0.5*rainFactor), NL_RAIN_ZENITH_COL*brightness, rainFactor);
}

vec3 getHorizonCol(float rainFactor, vec3 FOG_COLOR) {
  // value needs tweaking
  float val = max(FOG_COLOR.r*0.65, max(FOG_COLOR.g*1.1, FOG_COLOR.b));
  float sun = max(FOG_COLOR.r-FOG_COLOR.b, 0.0);

  // horizon color
  vec3 horizonCol = NL_DAWN_HORIZON_COL*(((0.7*val*val) + (0.4*val) + sun)*2.4);
  horizonCol += NL_NIGHT_HORIZON_COL;
  horizonCol = mix(horizonCol, 2.0*val*NL_DAY_HORIZON_COL, val*val);

  // rain horizon
  float brightness = min(FOG_COLOR.g, 0.26);
  brightness *= brightness*19.6;
  return mix(horizonCol, NL_RAIN_HORIZON_COL*brightness, rainFactor);
}

// tinting on horizon col
vec3 getHorizonEdgeCol(vec3 horizonCol, float rainFactor, vec3 FOG_COLOR) {
  float val = 2.1*(1.1-FOG_COLOR.b)*FOG_COLOR.g*(1.0-rainFactor);
  horizonCol *= vec3_splat(1.0-val) + NL_DAWN_EDGE_COL*val;
  return horizonCol;
}

// 1D sky with three color gradient
vec3 renderSky(vec3 reddishTint, vec3 horizonColor, vec3 zenithColor, float h) {
  h = 1.0-h*h;
  float hsq = h*h;

  // gradient 1  h^16
  // gradient 2  h^8 mix h^2
  float gradient1 = hsq*hsq*hsq*hsq;
  float gradient2 = 0.6*gradient1 + 0.4*hsq;
  gradient1 *= gradient1;

  horizonColor = mix(horizonColor, reddishTint, gradient1);
  return mix(zenithColor,horizonColor, gradient2);
}

// sky reflection on plane
vec3 getSkyRefl(vec3 horizonEdgeCol, vec3 horizonCol, vec3 zenithCol, float y, float h) {

  // offset the reflection based on height from camera
  float offset = h/(50.0+h);   // (h*0.02)/(1.0+h*0.02)
  y = max((y-offset)/(1.0-offset), 0.0);

  return renderSky(horizonEdgeCol, horizonCol, zenithCol, y);
}

// simpler sky reflection for rain
vec3 getRainSkyRefl(vec3 horizonCol, vec3 zenithCol, float h) {
  h = 1.0-h*h;
  h *= h;
  return mix(zenithCol, horizonCol, h*h);
}

// sunrise/sunset reflection
vec3 getSunRefl(float viewDirX, float fogBrightness, vec3 FOG_COLOR) {
  float factor = FOG_COLOR.r/length(FOG_COLOR);
  factor *= factor;

  float sunRefl = clamp((abs(viewDirX)-0.9)/0.099, 0.0, 1.0);
  sunRefl *= sunRefl*sunRefl*factor*factor;
  sunRefl *= sunRefl;

  return fogBrightness*sunRefl*vec3(2.5,1.6,0.8);
}

#endif
