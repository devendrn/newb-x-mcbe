$input a_position, a_color0, a_texcoord0, a_indices, a_normal
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2
#endif

$output v_color0, v_fog, v_light, v_texcoord0, v_edgemap

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/DynamicUtil.dragonh>
#include <MinecraftRenderer.Materials/TAAUtil.dragonh>
#include <newb/main.sh>

uniform vec4 OverlayColor;
uniform vec4 TileLightColor;
uniform vec4 FogColor;
uniform vec4 FogControl;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];
uniform vec4 ViewPositionAndTime;
uniform vec4 DimensionID;
uniform vec4 TimeOfDay;
uniform vec4 Day;

void main() {
  mat4 World = u_model[0];

  World = mul(World, Bones[int(a_indices)]);

  vec2 texcoord0 = a_texcoord0;
  texcoord0 = applyUvAnimation(texcoord0, UVAnimation);

  vec3 worldPosition;
  #ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0.0, 0.0, 0.0, 1.0));
    worldPosition = instMul(model, vec4(a_position, 1.0)).xyz;
  #else
    worldPosition = mul(World, vec4(a_position, 1.0)).xyz;
  #endif

  vec4 position = jitterVertexPosition(worldPosition);

  #if !(defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY) || defined(INSTANCING))
    nl_environment env = nlDetectEnvironment(DimensionID.x, TimeOfDay.x, Day.x, FogColor.rgb, FogControl.xyz);
    nl_skycolor skycol = nlSkyColors(env);

    float relativeDist = position.z/FogControl.z;

    worldPosition.y = -worldPosition.y;
    vec3 viewDir = normalize(worldPosition.xyz);

    vec4 fogColor;
    fogColor.rgb = nlRenderSky(skycol, env, viewDir, ViewPositionAndTime.w, false);
    fogColor.a = nlRenderFogFade(relativeDist, FogColor.rgb, FogControl.xy);

    if (env.nether) {
      // blend fog with void color
      fogColor.rgb = colorCorrectionInv(FogColor.rgb);
    }

    vec3 light = nlEntityLighting(env, a_position, a_normal, World, TileLightColor, OverlayColor, skycol.horizonEdge, ViewPositionAndTime.w);

    v_texcoord0 = texcoord0;
    v_color0 = a_color0;
    v_fog = fogColor;
    #ifdef NL_ENTITY_EDGE_HIGHLIGHT
      v_edgemap = nlEntityEdgeHighlightPreprocess(texcoord0);
    #else
      v_edgemap = vec4_splat(0.0);
    #endif
    v_light = vec4(light, 1.0);
  #endif

  gl_Position = position;
}
