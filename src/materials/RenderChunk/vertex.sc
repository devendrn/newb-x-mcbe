$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_color1, v_fog, v_refl, v_texcoord0, v_lightmapUV, v_extra

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

void main() {
  #ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
  #else
    mat4 model = u_model[0];
  #endif

  vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;

  #ifdef RENDER_AS_BILLBOARDS
    worldPos += vec3(0.5,0.5,0.5);

    vec3 modelCamPos = ViewPositionAndTime.xyz - worldPos;
    float camDis = length(modelCamPos);
    vec3 viewDir = modelCamPos / camDis;

    vec3 boardPlane = normalize(vec3(-viewDir.z, 0.0, viewDir.x));
    worldPos -= (((viewDir.zxy * boardPlane.yzx) - (viewDir.yzx * boardPlane.zxy)) *
                 (a_color0.z - 0.5)) +
                 (boardPlane * (a_color0.x - 0.5));
    vec4 color = vec4(1.0,1.0,1.0,1.0);
  #else
    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
    vec3 viewDir = modelCamPos / camDis;

    vec4 color = a_color0;
  #endif

  float relativeDist = camDis / FogAndDistanceControl.z;

  vec3 cPos = a_position.xyz;
  vec3 bPos = fract(cPos);
  vec3 tiledCpos = fract(cPos*0.0625);

  vec2 uv1 = a_texcoord1;
  vec2 lit = uv1*uv1;

  bool isColored = color.r != color.g || color.r != color.b;
  float shade = isColored ? color.g*1.5 : color.g;

  // tree leaves detection
  #ifdef ALPHA_TEST
    bool isTree = (isColored && (bPos.x+bPos.y+bPos.z < 0.001)) || color.a == 0.0;
  #else
    bool isTree = false;
  #endif

  // environment detections
  bool end = detectEnd(FogColor.rgb, FogAndDistanceControl.xy);
  bool nether = detectNether(FogColor.rgb, FogAndDistanceControl.xy);
  bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
  float rainFactor = detectRain(FogAndDistanceControl.xyz);

  // sky colors
  vec3 zenithCol;
  vec3 horizonCol;
  vec3 horizonEdgeCol;
  if (underWater) {
    vec3 fogcol = getUnderwaterCol(FogColor.rgb);
    zenithCol = fogcol;
    horizonCol = fogcol;
    horizonEdgeCol = fogcol;
  } else if (end) {
    zenithCol = getEndZenithCol();
    horizonCol = getEndHorizonCol();
    horizonEdgeCol = horizonCol;
  } else {
    vec3 fs = getSkyFactors(FogColor.rgb);
    zenithCol = getZenithCol(rainFactor, FogColor.rgb, fs);
    horizonCol = getHorizonCol(rainFactor, FogColor.rgb, fs);
    horizonEdgeCol = getHorizonEdgeCol(horizonCol, rainFactor, FogColor.rgb);
  }

  // time
  highp float t = ViewPositionAndTime.w;

  // convert color space to linear-space
  #ifdef SEASONS
    isTree = true;

    // season tree leaves are colored in fragment
    color.w *= color.w;
    color = vec4(color.www, 1.0);
  #else
    if (isColored) {
      color.rgb *= color.rgb*1.2;
    }
  #endif

  vec3 torchColor; // modified by nl_lighting
  vec3 light = nlLighting(
    worldPos, torchColor, a_color0.rgb, FogColor.rgb, rainFactor,uv1, lit, isTree, horizonCol, zenithCol, shade, end, nether, underWater, t
  );

  #if defined(ALPHA_TEST) && (defined(NL_PLANTS_WAVE) || defined(NL_LANTERN_WAVE))
    nlWave(worldPos, light, rainFactor, uv1, lit, a_texcoord0, bPos, a_color0, cPos, tiledCpos, t, isColored, camDis, isTree);
  #endif

  #ifdef NL_CHUNK_LOAD_ANIM
    // slide in anim
    worldPos.y -= NL_CHUNK_LOAD_ANIM*pow(RenderChunkFogAlpha.x,3.0);
  #endif

  // loading chunks
  relativeDist += RenderChunkFogAlpha.x;

  vec4 fogColor;
  fogColor.rgb = nlRenderSky(horizonEdgeCol, horizonCol, zenithCol, viewDir, FogColor.rgb, t, rainFactor, end, underWater, nether);
  fogColor.a = nlRenderFogFade(relativeDist, FogColor.rgb, FogAndDistanceControl.xy);
  #ifdef NL_GODRAY 
    fogColor.a = mix(fogColor.a, 1.0, NL_GODRAY*nlRenderGodRayIntensity(cPos, worldPos, t, uv1, relativeDist, FogColor.rgb));
  #endif

  if (nether) {
    // blend fog with void color
    fogColor.rgb = colorCorrectionInv(FogColor.rgb);
    fogColor.rgb = mix(fogColor.rgb, vec3(0.8,0.2,0.12)*1.5, lit.x*(1.67-fogColor.a*1.67));
  }

  vec4 refl = vec4(0.0,0.0,0.0,0.0);
  vec4 pos;

  #if !defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)
    #ifdef TRANSPARENT
      if (a_color0.a < 0.95) {
        color.a += (0.5-0.5*color.a)*clamp((camDis/FogAndDistanceControl.w),0.0,1.0);
      };

      float water;
      if (a_color0.b > 0.3 && a_color0.a < 0.95) {
        water = 1.0;
        refl = nlWater(
          worldPos, color, a_color0, viewDir, light, cPos, tiledCpos, bPos.y, FogColor.rgb, horizonCol, horizonEdgeCol, zenithCol, lit, t, camDis, rainFactor, torchColor, end, nether, underWater
        );
        pos = mul(u_viewProj, vec4(worldPos, 1.0));
      } else {
        water = 0.0;
        pos = mul(u_viewProj, vec4(worldPos, 1.0));
        refl = nlRefl(
          color, fogColor, lit, uv1, tiledCpos, camDis, worldPos, viewDir, torchColor, horizonEdgeCol, horizonCol, zenithCol, FogColor.rgb, rainFactor, FogAndDistanceControl.z, t, pos.xyz, underWater, end, nether
        );
      }
    #else
      float water = 0.0;
      pos = mul(u_viewProj, vec4(worldPos, 1.0));
      refl = nlRefl(
        color, fogColor, lit, uv1, tiledCpos, camDis, worldPos, viewDir, torchColor, horizonEdgeCol, horizonCol, zenithCol, FogColor.rgb, rainFactor, FogAndDistanceControl.z, t, pos.xyz, underWater, end, nether
      );
    #endif

    if (underWater) {
      nlUnderwaterLighting(light, pos.xyz, lit, uv1, tiledCpos, cPos, t, horizonEdgeCol);
    }
  #else
    float water = 0.0;
    pos = mul(u_viewProj, vec4(worldPos, 1.0));
  #endif

  color.rgb *= light;

  #ifdef NL_GLOW_SHIMMER
    float shimmer = nlGlowShimmer(cPos, t);
  #else
    float shimmer = 0.0;
  #endif

  v_extra = vec4(shade, worldPos.y, water, shimmer);
  v_refl = refl;
  v_texcoord0 = a_texcoord0;
  v_lightmapUV = a_texcoord1;
  v_color0 = color;
  v_color1 = a_color0;
  v_fog = fogColor;
  gl_Position = pos;
}
