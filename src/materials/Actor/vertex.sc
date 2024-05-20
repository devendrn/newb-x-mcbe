$input a_position, a_color0, a_texcoord0, a_indices, a_normal
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2
#endif

$output v_color0, v_fog, v_light, v_texcoord0, v_edgemap

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>
#include <MinecraftRenderer.Materials/DynamicUtil.dragonh>
#include <MinecraftRenderer.Materials/TAAUtil.dragonh>
#include <newb/main.sh>

uniform vec4 ColorBased;
uniform vec4 ChangeColor;
uniform vec4 UseAlphaRewrite;
uniform vec4 TintedAlphaTestEnabled;
uniform vec4 MatColor;
uniform vec4 OverlayColor;
uniform vec4 TileLightColor;
uniform vec4 MultiplicativeTintColor;
uniform vec4 FogColor;
uniform vec4 FogControl;
uniform vec4 ActorFPEpsilon;
uniform vec4 LightDiffuseColorAndIntensity;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];
uniform vec4 ViewPositionAndTime;

void main() {
  mat4 World = u_model[0];

  //StandardTemplate_InvokeVertexPreprocessFunction
  World = mul(World, Bones[int(a_indices)]);

  vec2 texcoord0 = a_texcoord0;
  texcoord0 = applyUvAnimation(texcoord0, UVAnimation);

  //StandardTemplate_VertSharedTransform
  vec3 worldPosition;
#ifdef INSTANCING
  mat4 model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0.0, 0.0, 0.0, 1.0));
  worldPosition = instMul(model, vec4(a_position, 1.0)).xyz;
#else
  worldPosition = mul(World, vec4(a_position, 1.0)).xyz;
#endif

  vec4 position = jitterVertexPosition(worldPosition);

#if defined(DEPTH_ONLY)
  v_texcoord0 = vec2(0.0, 0.0);
  v_color0 = vec4(0.0, 0.0, 0.0, 0.0);
#else
  v_texcoord0 = texcoord0;
  v_color0 = a_color0;
#endif

  vec4 edgeMap = fract(vec4(v_texcoord0.xy*128.0, v_texcoord0.xy*256.0));
  edgeMap = 2.0*step(edgeMap, vec4_splat(0.5)) - 1.0;

  // environment detections
  bool end = detectEnd(FogColor.rgb, FogControl.xy);
  bool nether = detectNether(FogColor.rgb, FogControl.xy);
  bool underWater = detectUnderwater(FogColor.rgb, FogControl.xy);
  float rainFactor = detectRain(FogControl.xyz);

  vec3 newFog;
  if (underWater) {
    newFog = getUnderwaterCol(FogColor.rgb);
  } else if (end) {
    newFog = getEndHorizonCol();
  } else {
    vec3 fs = getSkyFactors(FogColor.rgb);
    newFog = getHorizonCol(rainFactor, FogColor.rgb, fs);
    newFog = getHorizonEdgeCol(newFog.rgb, rainFactor, FogColor.rgb);
  }

  // relative cam dist
  float camDist = position.z/FogControl.z;

  vec4 fogColor;
  fogColor.rgb = newFog;
  fogColor.a = nlRenderFogFade(camDist, FogColor.rgb, FogControl.xy);

  if (nether) {
    // blend fog with void color
    fogColor.rgb = colorCorrectionInv(FogColor.rgb);
  }

  vec3 light = nlActorLighting(a_position, a_normal, World, TileLightColor, OverlayColor, newFog, nether, underWater, end, ViewPositionAndTime.w);

  v_fog = fogColor;
  v_edgemap = edgeMap;
  v_light = vec4(light, 1.0);
  gl_Position = position;
}
