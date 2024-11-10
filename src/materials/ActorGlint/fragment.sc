$input v_color0, v_fog, v_light, v_texcoord0, v_edgemap, v_layeruv

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
uniform vec4 FogColor;
uniform vec4 FogControl;
uniform vec4 ActorFPEpsilon;
uniform vec4 LightDiffuseColorAndIlluminance;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_MatTexture1);

vec4 applyGlint(vec4 light, vec4 layerUV, sampler2D glintTexture, vec4 glintColor, vec4 tileLightColor, vec4 albedo) {
  float d = fract(dot(albedo.rgb, vec3_splat(4.0)));

  vec4 tex1 = texture2D(glintTexture, fract(layerUV.xy+0.1*d)).rgbr;
  vec4 tex2 = texture2D(glintTexture, fract(layerUV.zw+0.1*d)).rgbr;

  vec4 glint = (tex1*tex1 + tex2*tex2) * tileLightColor * glintColor;

  light.rgb = light.rgb*(1.0-0.4*glint.a) + 80.0*glint.rgb;
  light.rgb += vec3(0.1,0.0,0.1) + 0.2*spectrum(sin(layerUV.x*9.42477 + 2.0*glint.a + d));

  return light;
}

void main() {
  #if DEPTH_ONLY || INSTANCING
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
  #elif DEPTH_ONLY_OPAQUE
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

  #if CHANGE_COLOR_MULTI
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
  #elif CHANGE_COLOR
    albedo = applyColorChange(albedo, ChangeColor, albedo.a);
    albedo.a *= ChangeColor.a;
  #endif

  albedo.rgb *= mix(vec3_splat(1.0), v_color0.rgb, ColorBased.x);

  albedo = applyOverlayColor(albedo, OverlayColor);

  albedo *= albedo;

  vec4 light = v_light;
  #if EMISSIVE || EMISSIVE_ONLY
    light.rgb = max(light.rgb, 2.0*NL_GLOW_TEX*(1.0-albedo.a)); // glow effect
  #endif
  light = applyGlint(light, v_layeruv, s_MatTexture1, GlintColor, TileLightColor, albedo);

  albedo = applyLighting(albedo, light);

  #if TRANSPARENT
    albedo = applyHudOpacity(albedo, HudOpacity.x);
  #endif

  albedo.rgb *= nlEntityEdgeHighlight(v_edgemap);

  albedo.rgb = mix(albedo.rgb, v_fog.rgb, v_fog.a);

  albedo.rgb = colorCorrection(albedo.rgb);

  gl_FragColor = albedo;
}
