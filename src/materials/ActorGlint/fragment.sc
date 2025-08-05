$input v_color0, v_fog, v_light, v_texcoord0, v_edgemap, v_glintuv

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/ActorUtil.dragonh>
#include <newb/main.sh>

uniform vec4 ColorBased;
uniform vec4 ChangeColor;
uniform vec4 UseAlphaRewrite;
uniform vec4 TintedAlphaTestEnabled;
uniform vec4 MatColor;
uniform vec4 OverlayColor;
uniform vec4 TileLightColor;
uniform vec4 GlintColor;
uniform vec4 MultiplicativeTintColor;
uniform vec4 ActorFPEpsilon;
uniform vec4 HudOpacity;

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_MatTexture1);

void main() {
  #if defined(DEPTH_ONLY) || defined(INSTANCING)
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
  #elif defined(DEPTH_ONLY_OPAQUE)
    gl_FragColor = vec4(mix(vec3_splat(1.0), v_fog.rgb, v_fog.a), 1.0);
    return;
  #endif

  vec4 albedo = getActorAlbedoNoColorChange(v_texcoord0, s_MatTexture, s_MatTexture1, MatColor);

  #if ALPHA_TEST
    float alpha = mix(albedo.a, (albedo.a * OverlayColor.a), TintedAlphaTestEnabled.x);
    if (shouldDiscard(albedo.rgb, alpha, ActorFPEpsilon.x)) {
      discard;
    }
  #endif

  #ifdef CHANGE_COLOR_MULTI
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
  #elif defined(CHANGE_COLOR)
    albedo = applyColorChange(albedo, ChangeColor, albedo.a);
    albedo.a *= ChangeColor.a;
  #endif

  albedo.rgb *= mix(vec3_splat(1.0), v_color0.rgb, ColorBased.x);

  albedo = applyOverlayColor(albedo, OverlayColor);

  albedo *= albedo;

  vec4 light = v_light;
  #if defined(EMISSIVE) || defined(EMISSIVE_ONLY)
    light.rgb = max(light.rgb, 2.0*NL_GLOW_TEX*(1.0-albedo.a)); // glow effect
  #endif
  light = nlGlint(light, v_glintuv, s_MatTexture1, GlintColor, TileLightColor, albedo);

  albedo = applyLighting(albedo, light);

  #ifdef TRANSPARENT
    albedo = applyHudOpacity(albedo, HudOpacity.x);
  #endif

  albedo.rgb *= nlEntityEdgeHighlight(v_edgemap);

  albedo.rgb = mix(albedo.rgb, v_fog.rgb, v_fog.a);

colorCorrection(albedo.rgb,gl_FragCoord.xy,u_viewRect.zw);

  gl_FragColor = albedo;
}
