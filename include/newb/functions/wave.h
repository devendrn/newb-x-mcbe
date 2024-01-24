#ifndef WAVE_H
#define WAVE_H

#include "constants.h"
#include "noise.h"

#ifdef NL_LANTERN_WAVE
void lanternWave(inout vec3 worldPos, vec3 cPos, vec3 bPos, vec2 bPosC, float texPosY, float rainFactor, vec2 uv1, float windStrength, highp float t) {
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
  // 1.20.40 vanilla only 
  // not meant to be used
  
  // count texture atlas in LR row wise order (32x64)
  int texN = 32*int(uv0.y*64.0) + int(uv0.x*32.0) + 1;

  if ( // full
    (texN == 168) || // cherrry leaves
    (texN>378 && texN<389) || // tall flowers top
    (texN==914) // sunflower sepal
  ) {
    shouldWave = true;
  } else if ( // top only
    (texN==173) || // cherry blossom sapling
    (texN>749 && texN<761)  || // short flowers
    (texN>372 && texN<379)  || // tall flowers bottom
    (texN>795 && texN<803) || // saplings
    (texN==866) || // spore blossom petal
    (texN>922 && texN<927) || // cherry bush
    (texN>939 && texN<943) || // torch flower
    (texN==988) || // wither rose
    (texN==1009)  // yellow dandelion
  ) {
    shouldWave = isTop;
  } else if ( // bottom only
    (texN==477) || // hanging roots
    (texN==19 || texN==418)  // azeala
  ) {
    shouldWave = !isTop;
  }
}
#endif

void nlWave(inout vec3 worldPos, inout vec3 light, float rainFactor, vec2 uv1, vec2 lit,
             vec2 uv0, vec3 bPos, vec4 COLOR, vec3 cPos, vec3 tiledCpos, highp float t,
             bool isColored, float camDist, bool underWater, bool isTreeLeaves) {

  if (camDist > 13.0) {  // only wave nearby (better performance)
    return;
  }

  // texture atlas has 32x64 textures (uv0.xy division)
  float texPosY = fract(uv0.y*64.0);
  
  // x and z distance from block center
  vec2 bPosC = abs(bPos.xz-0.5);

  bool isTop = texPosY < 0.5;
  bool isPlants = COLOR.r/COLOR.g<1.9;
  bool isVines = (bPosC.x==0.453125 && bPos.z==0.0) || (bPosC.y==0.453125 && bPos.x==0.0);
  bool isFarmPlant = (bPos.y==0.9375) && (bPosC.x==0.25 ||  bPosC.y==0.25);
  bool shouldWave = ((isTreeLeaves || isPlants || isVines) && isColored) || (isFarmPlant && isTop);

  float windStrength = lit.y*(noise1D(t*0.36) + rainFactor*0.4);

  // darken plants bottom - better to not move it elsewhere
  light *= isFarmPlant && !isTop ? 0.7 : 1.1;
  if (isColored && !isTreeLeaves && uv0.y>0.43 && uv0.y<0.48) {
    light *= isTop ? 1.2 : 1.2 - 1.2*(bPos.y>0.0 ? 1.5-bPos.y : 0.5);
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
      // wave the bottom of plants in opposite direction to make it look fixed
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
