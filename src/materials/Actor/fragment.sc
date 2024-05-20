$input v_color0, v_fog, v_light, v_texcoord0, v_edgemap

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/ActorUtil.dragonh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>
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
uniform vec4 LightDiffuseColorAndIlluminance;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_MatTexture1, 1);

void main() {
#if DEPTH_ONLY
  gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  return;
#elif DEPTH_ONLY_OPAQUE
  gl_FragColor = vec4(applyFog(vec3(1.0, 1.0, 1.0), v_fog.rgb, v_fog.a), 1.0);
  return;
#endif

  vec4 albedo = getActorAlbedoNoColorChange(v_texcoord0, s_MatTexture, s_MatTexture1, MatColor);

#if ALPHA_TEST
  float alpha = mix(albedo.a, (albedo.a * OverlayColor.a), TintedAlphaTestEnabled.x);
  if(shouldDiscard(albedo.rgb, alpha, ActorFPEpsilon.x))
    discard;
#endif // ALPHA_TEST

#if CHANGE_COLOR_MULTI
  albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
#elif CHANGE_COLOR
  albedo = applyColorChange(albedo, ChangeColor, albedo.a);
  albedo.a *= ChangeColor.a;
#endif // CHANGE_COLOR_MULTI

#if ALPHA_TEST
  albedo.a = max(UseAlphaRewrite.r, albedo.a);
#endif

  albedo.rgb *= mix(vec3(1, 1, 1), v_color0.rgb, ColorBased.x);

  albedo = applyOverlayColor(albedo, OverlayColor);

  albedo *= albedo;

  vec4 light = v_light;
#if EMISSIVE || EMISSIVE_ONLY
  light.rgb = max(light.rgb, 2.0*NL_GLOW_TEX*(1.0-albedo.a)); //make glowy stuff
#endif

  albedo = applyLighting(albedo, light);

#if TRANSPARENT
  albedo = applyHudOpacity(albedo, HudOpacity.x);
#endif

  // soft edge highlight
  vec2 len = min(abs(v_edgemap.xy),abs(v_edgemap.zw));
  float ambient = max(len.x,len.y);
  ambient = min(ambient*ambient,1.0);
  albedo.rgb *= 0.65 + ambient*0.41;

  //apply fog
  albedo.rgb = mix(albedo.rgb, v_fog.rgb, v_fog.a);

  albedo.rgb = colorCorrection(albedo.rgb);

  gl_FragColor = albedo;
}
