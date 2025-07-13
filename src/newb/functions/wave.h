#ifndef WAVE_H
#define WAVE_H

#include "constants.h"
#include "noise.h"

#ifdef NL_LANTERN_WAVE
void lanternWave(
  inout vec3 worldPos, vec3 cPos, vec3 bPos, vec2 bPosC, float texPosY, float rainFactor, vec2 uv1, float windStrength, highp float t
) {
  bool y6875 = bPos.y==0.6875;
  bool y5625 = bPos.y==0.5625;

  bool isLantern = ( (y6875 || y5625) && bPosC.x==0.125 ) || ( (y5625 || bPos.y==0.125) && (bPosC.x==0.1875) );
  bool isChain = bPosC.x==0.0625 && y6875;

  // fix for non-hanging lanterns waving top part (works only if texPosY is correct)
  if (y5625 && (texPosY < 0.3 || (texPosY>0.55 && texPosY<0.69))) {
    isLantern = false;
  }

  if (uv1.x > 0.6 && (isChain || isLantern)) {
    // simple wave for angle
    float phase = dot(floor(cPos), vec3_splat(0.3927));
    vec2 theta = vec2(t + phase, t*1.4 + phase);
    theta = sin(vec2(theta.x,theta.x+0.7)) + rainFactor*sin(vec2(theta.y,theta.y+0.7));
    theta *= NL_LANTERN_WAVE*windStrength;

    vec2 sinA = sin(theta);
    vec2 cosA = cos(theta);

    // apply XZ rotation
    vec3 pivotPos = vec3(0.5,1.0,0.5) - bPos;
    worldPos.x += dot(pivotPos.xy, vec2(1.0-cosA.x, -sinA.x));
    worldPos.y += dot(pivotPos, vec3(sinA.x*cosA.y, 1.0-cosA.x*cosA.y, sinA.y));
    worldPos.z += dot(pivotPos, vec3(sinA.x*sinA.y, -cosA.x*sinA.y, 1.0-cosA.y));
  }
}
#endif

#ifdef NL_EXTRA_PLANTS_WAVE
void extraPlantsFlag(inout bool shouldWave, vec2 uv0, bool isTop) {
  // 1.21.0 (2048x1024) vanilla only
  // not meant to be used

  // count texture atlas in left-to-right row wise order (64X32)
  // starts from 0
  int texN = 64*int(uv0.y*32.0) + int(uv0.x*64.0);

  if ( // full
    (texN==182) || // cherrry leaves
    (texN>415 && texN<425) // tall flowers/plants bottom
  ) {
    shouldWave = true;
  } else if ( // top only
    (texN==187) || // cherry blossom sapling
    (texN==1361) || // spore blossom petal
    (texN>409 && texN<416) || // tall flowers/plants bottom
    (texN>959 && texN<964) || // sweet berries bush
    (texN>971 && texN<975) || // torch flowers
    (texN==905) || // wither rose
    (texN==1057)  // yellow dandelion
  ) {
    shouldWave = isTop;
  } else if ( // bottom only
    (texN==524) || // hanging roots
    (texN==23 || texN==465)  // azeala
  ) { 
    shouldWave = !isTop;
  }
}
#endif

void nlWave(
  inout vec3 worldPos, inout vec3 light, float rainFactor, vec2 uv1, vec2 lit,
  vec2 uv0, vec3 bPos, vec4 COLOR, vec3 cPos, vec3 tiledCpos, highp float t, sampler2D terrainTex,
  bool isColored, float camDist, bool isTreeLeaves
) {
  if (camDist > NL_WAVE_RANGE) {  // only wave nearby (better performance)
    return;
  }

  float waveFade = 2.0*max((camDist/NL_WAVE_RANGE) - 0.5, 0.0);
  waveFade *= waveFade;

  // texture atlas has 64x32 textures (uv0.xy division)
  float texPosY = fract(uv0.y*vec2(textureSize(terrainTex, 0)).y/32.0);

  // x and z distance from block center
  vec2 bPosC = abs(bPos.xz-0.5);

  bool isTop = texPosY < 0.5;
  bool isPlants = COLOR.r/COLOR.g<1.9;
  bool isVines = (bPosC.x==0.453125 && bPos.z==0.0) || (bPosC.y==0.453125 && bPos.x==0.0);
  bool isFarmPlant = (bPos.y==0.9375) && (bPosC.x==0.25 ||  bPosC.y==0.25);
  bool shouldWave = ((isTreeLeaves || isPlants || isVines) && isColored) || (isFarmPlant && isTop);
  bool isRedStone = COLOR.r > 0.25 && COLOR.r > 3.0*COLOR.g  && COLOR.b == 0.0;

  float windStrength = lit.y*(noise1D(t*0.36) + rainFactor*0.4)*(1.0-waveFade);

  // darken farm plants bottom
  light *= isFarmPlant && !isTop ? 0.7 : 1.1;
  if (isColored && !isTreeLeaves && uv0.y>0.375 && uv0.y<0.466 && !isRedStone) {
    // make grass bottom more dark depending how deep it is
    light *= mix(isTop ? 1.2 : 1.2 - 1.2*(bPos.y>0.0 ? 1.5-bPos.y : 0.5), 1.0, waveFade);
  }

  #ifdef NL_PLANTS_WAVE
    #ifdef NL_EXTRA_PLANTS_WAVE
      extraPlantsFlag(shouldWave, uv0, isTop);
    #endif

    if (shouldWave) {

      float wave = NL_PLANTS_WAVE*windStrength;

      if (isTreeLeaves) {
        wave *= 0.5;
      } else if (isVines) {
        wave *= fract(0.01+tiledCpos.y*0.5);
      } else if (isPlants && isColored && !isTop) {
        // wave the bottom of grass in opposite direction
        // depending on how deep it is to make it look almost fixed
        wave *= bPos.y > 0.0 ? bPos.y-1.0 : 0.0;
      }

      float phaseDiff = dot(cPos,vec3_splat(NL_CONST_PI_QUART)) + fastRand(tiledCpos.xz + tiledCpos.y);
      wave *= 1.0 + mix(
        sin(t*NL_WAVE_SPEED + phaseDiff),
        sin(t*NL_WAVE_SPEED*1.5 + phaseDiff),
        rainFactor);

      //worldPos.y -= 1.0-sqrt(1.0-wave*wave);
      worldPos.xyz -= vec3(wave, wave*wave*0.5, wave);
    }
  #endif

  #ifdef NL_LANTERN_WAVE
    lanternWave(worldPos, cPos, bPos, bPosC, texPosY, rainFactor, uv1, windStrength, t);
  #endif
}

#endif
