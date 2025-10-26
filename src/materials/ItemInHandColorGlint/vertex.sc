$input a_position, a_color0, a_texcoord0, a_indices, a_normal
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2
#endif

$output v_color0, v_fog, v_light, v_glintuv

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/TAAUtil.dragonh>
#include <MinecraftRenderer.Materials/GlintUtil.dragonh>
#include <newb/main.sh>

uniform vec4 FogControl;
uniform vec4 FogColor;
uniform vec4 OverlayColor;
uniform vec4 TileLightIntensity;
uniform vec4 TileLightColor;
uniform vec4 ViewPositionAndTime;
uniform vec4 UVAnimation;
uniform vec4 UVScale;
uniform vec4 DimensionID;
uniform vec4 TimeOfDay;
uniform vec4 Day;

void main() {
  mat4 World = u_model[0];
  vec2 texcoord0 = a_texcoord0;
  vec3 wpos;

  #ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0.0, 0.0, 0.0, 1.0));
    wpos = instMul(model, vec4(a_position, 1.0)).xyz;
  #else
    wpos = mul(World, vec4(a_position, 1.0)).xyz;
  #endif

  vec4 position = jitterVertexPosition(wpos);

  #if !(defined(DEPTH_ONLY) || defined(INSTANCING))
    nl_environment env = nlDetectEnvironment(DimensionID.x, TimeOfDay.x, Day.x, FogColor.rgb, FogControl.xyz);
    nl_skycolor skycol = nlSkyColors(env);

    float relativeDist = position.z/FogControl.z;

    wpos.y = -wpos.y;
    vec3 viewDir = normalize(wpos.xyz);

    vec4 fogColor;
    fogColor.rgb = nlRenderSky(skycol, env, viewDir, ViewPositionAndTime.w, false);
    fogColor.a = nlRenderFogFade(relativeDist, FogColor.rgb, FogControl.xy);

    if (env.nether) {
      // blend fog with void color
      fogColor.rgb = colorCorrectionInv(FogColor.rgb);
    }

    vec3 light = nlEntityLighting(env, a_position, a_normal, World, TileLightColor, OverlayColor, skycol.horizonEdge, ViewPositionAndTime.w);

    vec4 glintuv;
    glintuv.xy = calculateLayerUV(texcoord0, UVAnimation.x, UVAnimation.z, UVScale.xy);
    glintuv.zw = calculateLayerUV(texcoord0, UVAnimation.y, UVAnimation.w, UVScale.xy);

    v_glintuv = glintuv;
    v_color0 = a_color0;
    v_fog = fogColor;
    v_light = vec4(light, 1.0);
  #endif

  gl_Position = position;
}
