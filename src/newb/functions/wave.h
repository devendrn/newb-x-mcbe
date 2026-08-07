#ifndef WAVE_H
#define WAVE_H

#include "utils.h"
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
  if (y5625 && (texPosY < 0.001 || (texPosY>0.43 && texPosY<0.93))) {
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
  // 1.26.40 (1024x512) vanilla only
  // not meant to be used

  // count texture atlas in left-to-right row wise order (64X32)
  // starts from 0
  int texN = 64*int(uv0.y*32.0) + int(uv0.x*64.0);

  if ( // full
    (texN>=18 && texN<=20) || // Azeala Leaves and Flowering Azeala Leaves
    (texN>=177 && texN<=180) || // Cave Vines
    (texN>=186 && texN<=187) || // Cherrry Leaves (Fixed) 
    (texN>=444 && texN<=459) || (texN==678) || // tall flowers/plants top
    (texN>=796 && texN<=797) || // Pale Hanging Moss
    (texN>=803 && texN<=804) || // Pale Oak Leaves
    (texN>=832 && texN<=834) || (texN>=837 && texN<=838) // Pitcher Plant
  ) {
    shouldWave = true;
  } else if ( // top only
    (texN==6) || // Acacia Sappling
    (texN==8) || // Allium (NEW
    (texN==25) || // Azure Bluet (NEW
    (texN==85) || // Birch sappling
    (texN==110) || // Blue Orchid
    (texN==145) || // Cactus Flower
    (texN==192) || // Cherry Blossom Sapling
    (texN==223) || // Closed Eyeblossom
    (texN==336) || // Cornflower
    (texN==387) || // Dandelion
    (texN==390) || // Dark Oak Sappling
    (texN==396) || // Dead Bush
    (texN>445 && texN<453) || // tall flowers/plants bottom
    (texN>=530 && texN<=531) || // Firefly Bush
    (texN==564) || // Golden Dandelion
    (texN==642) || // Jungle Sappling
    (texN==679) || // Lily of the Valley
    (texN>=715 && texN<=717) || // Mangrove Propagule
    (texN==753) || // Oak Sappling
    (texN>=761 && texN<=763) || // Open Eyeblossom
    (texN==773) || // Orange Tulip
    (texN==774) || // Oxeye Daisy
    (texN==808) || // Pale Oak Sappling
    (texN==823) || // Pink Tulip (New)
    (texN==861) || // Poppy
    (texN==914) || // Red Tulip
    (texN==915) || // White Tilip
    (texN==945) ||  // Spruce Sappling
    (texN==1011) || // Spore Blossom Petal
    (texN>=1079 && texN<=1082) || // Sweet Berries Bush
    (texN==1084) || // Tall Dry Grass
    (texN>=1090 && texN<=1092) || // Torch Flowers
    (texN==1187) // Wither Rose
  ) {
    shouldWave = isTop;
  } else if ( // bottom only
    (texN==23 || texN==547) ||  // Azeala
    (texN==610) // Hanging Roots
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
  float texPosY = fract(uv0.y*vec2(textureSize(terrainTex, 0)).y/16.0);

  // x and z distance from block center
  vec2 bPosC = abs(bPos.xz-0.5);
  vec2 bPosH = fract(bPos.xz*2.0);

  bool isTop = texPosY < 0.5;
  bool isPlants = COLOR.r/COLOR.g<1.9;
  bool isVines = (bPosC.x==0.453125 && bPos.z==0.0) || (bPosC.y==0.453125 && bPos.x==0.0);
  bool isFarmPlant = (bPos.y==0.9375) && (bPosC.x==0.25 ||  bPosC.y==0.25);
  bool isRedStone = COLOR.r > 0.25 && COLOR.r > 3.0*COLOR.g  && COLOR.b == 0.0;
  bool isLeafLitter = bPos.y==0.015625 && (bPosH.x+bPosH.y)==0.0;
  bool shouldWave = ((isTreeLeaves || isPlants || isVines) && isColored && !isLeafLitter) || (isFarmPlant && isTop);

  float windStrength = lit.y*(noise1D(t*0.36) + rainFactor*0.4)*(1.0-waveFade);

  // darken farm plants bottom
  light *= isFarmPlant && !isTop ? 0.7 : 1.1;
  if (isColored && !isTreeLeaves && !isLeafLitter && uv0.y>0.214 && uv0.y<0.502 && !isRedStone) {
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

      float phaseDiff = dot(cPos,vec3_splat(PI_QUART)) + fastRand(tiledCpos.xz + tiledCpos.y);
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
