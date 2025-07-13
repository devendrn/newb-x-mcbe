#ifndef DETECTION_H
#define DETECTION_H

struct nl_environment {
  bool end;
  bool nether;
  bool underwater;
  float rainFactor;
  float dayFactor;
};

bool detectEnd(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
  // custom fog color set in biomes_client.json to help in detection
  return FOG_COLOR.r==FOG_COLOR.b && (FOG_COLOR.r-FOG_COLOR.g>0.24 || (FOG_COLOR.g==0.0 && FOG_COLOR.r>0.1));
}

bool detectNether(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
  // fogctrl.xy varies with renderdistance
  // x range (0.03,0.14)

  // reverse plotted relation (5,6,7,8,9,11,12,20,96 chunks data) with an accuracy of 0.02
  float expectedFogX = 0.029 + (0.09*FOG_CONTROL.y*FOG_CONTROL.y);

  // nether wastes, basalt delta, crimson forest, wrapped forest, soul sand valley
  bool netherFogCtrl = (FOG_CONTROL.x<0.14  && abs(FOG_CONTROL.x-expectedFogX) < 0.02);
  bool netherFogCol = (FOG_COLOR.r+FOG_COLOR.g)>0.0;

  // consider underlava as nether
  bool underLava = FOG_CONTROL.x == 0.0 && FOG_COLOR.b == 0.0 && FOG_COLOR.g < 0.18 && FOG_COLOR.r-FOG_COLOR.g > 0.1;

  return (netherFogCtrl && netherFogCol) || underLava;
}

bool detectUnderwater(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
  return FOG_CONTROL.x==0.0 && FOG_CONTROL.y<0.8 && (FOG_COLOR.b>FOG_COLOR.r || FOG_COLOR.g>FOG_COLOR.r);
}

float detectRain(vec3 FOG_CONTROL) {
  // clear fogctrl.x varies with render distance (z)
  // reverse plotted as 0.5 + 1.25/k (k is renderdistance in chunks, fogctrl.z = k*16)
  vec2 clear = vec2(0.5 + 20.0/FOG_CONTROL.z, 1.0); // clear fogctrl value
  vec2 rain = vec2(0.23, 0.70); // rain fogctrl value
  vec2 factor = clamp((FOG_CONTROL.xy-clear)/(rain-clear), vec2(0.0,0.0), vec2(1.0,1.0));
  float val = factor.x*factor.y;
  return val*val*(3.0 - 2.0*val);
}

float detectDayFactor(vec3 FOG_COLOR) {
  return min(dot(FOG_COLOR, vec3(0.5,0.7,0.5)), 1.0);
}

nl_environment nlDetectEnvironment(vec3 FOG_COLOR, vec3 FOG_CONTROL) {
  nl_environment e;
  e.end = detectEnd(FOG_COLOR, FOG_CONTROL.xy);
  e.nether = detectNether(FOG_COLOR, FOG_CONTROL.xy);
  e.underwater = detectUnderwater(FOG_COLOR, FOG_CONTROL.xy);
  e.rainFactor = detectRain(FOG_CONTROL.xyz);
  e.dayFactor = detectDayFactor(FOG_COLOR);
  return e;
}

#endif
