$input a_position, a_color0, a_texcoord0, a_indices, a_normal
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2
#endif

$output v_color0, v_fog, v_light, v_texcoord0, v_edgemap, v_layeruv

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/DynamicUtil.dragonh>
#include <MinecraftRenderer.Materials/TAAUtil.dragonh>
#include <MinecraftRenderer.Materials/GlintUtil.dragonh>
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
uniform vec4 UVScale;
uniform mat4 Bones[8];
uniform vec4 ViewPositionAndTime;

void main() {
  mat4 World = u_model[0];

  World = mul(World, Bones[int(a_indices)]);

  vec2 texcoord0 = a_texcoord0;

  vec3 worldPosition;
  #ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0.0, 0.0, 0.0, 1.0));
    worldPosition = instMul(model, vec4(a_position, 1.0)).xyz;
  #else
    worldPosition = mul(World, vec4(a_position, 1.0)).xyz;
  #endif

  vec4 position = jitterVertexPosition(worldPosition);

  #if !(defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY) || defined(INSTANCING))
    nl_environment env = nlDetectEnvironment(FogColor.rgb, FogControl.xyz);
    nl_skycolor skycol = nlSkyColors(env, FogColor.rgb);

    float relativeDist = position.z/FogControl.z;

    worldPosition.y = -worldPosition.y;
    vec3 viewDir = normalize(worldPosition.xyz);

    vec4 fogColor;
    fogColor.rgb = nlRenderSky(skycol, env, viewDir, FogColor.rgb, ViewPositionAndTime.w);
    fogColor.a = nlRenderFogFade(relativeDist, FogColor.rgb, FogControl.xy);

    if (env.nether) {
      // blend fog with void color
      fogColor.rgb = colorCorrectionInv(FogColor.rgb);
    }

    vec3 light = nlEntityLighting(env, a_position, a_normal, World, TileLightColor, OverlayColor, skycol.horizonEdge, ViewPositionAndTime.w);

    vec4 layeruv;
    layeruv.xy = calculateLayerUV(texcoord0, UVAnimation.x, UVAnimation.z, UVScale.xy);
    layeruv.zw = calculateLayerUV(texcoord0, UVAnimation.y, UVAnimation.w, UVScale.xy);

    v_texcoord0 = texcoord0;
    v_layeruv = layeruv;
    v_color0 = a_color0;
    v_fog = fogColor;
    v_edgemap = nlEntityEdgeHighlightPreprocess(texcoord0);
    v_light = vec4(light, 1.0);
  #endif

  gl_Position = position;
}
