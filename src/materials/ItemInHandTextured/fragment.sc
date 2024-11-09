$input v_color0, v_fog, v_light, v_texcoord0, v_edgemap

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/ActorUtil.dragonh>
#include <newb/main.sh>

uniform vec4 FogControl;
uniform vec4 ChangeColor;
uniform vec4 OverlayColor;
uniform vec4 ColorBased;
uniform vec4 FogColor;
uniform vec4 LightDiffuseColorAndIlluminance;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 TileLightIntensity;
uniform vec4 MatColor;
uniform vec4 MaterialID;
uniform vec4 MultiplicativeTintColor;
uniform vec4 SubPixelOffset;
uniform vec4 TileLightColor;

SAMPLER2D_AUTOREG(s_MatTexture);

void main() {
  #if DEPTH_ONLY || INSTANCING
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
  #endif

  vec4 albedo = MatColor * texture2D(s_MatTexture, v_texcoord0);

  #if ALPHA_TEST
    if (albedo.a < 0.5) {
      discard;
    }
  #endif

  #if MULTI_COLOR_TINT
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
  #else
    albedo = applyColorChange(albedo, ChangeColor, albedo.a);
    albedo.a *= ChangeColor.a;
  #endif

  albedo.rgb *= mix(vec3(1.0, 1.0, 1.0), v_color0.rgb, ColorBased.x);

  albedo = applyOverlayColor(albedo, OverlayColor);
  albedo *= albedo;

  albedo = applyLighting(albedo, v_light);

  albedo.rgb *= nlEntityEdgeHighlight(v_edgemap);

  albedo.rgb = mix(albedo.rgb, v_fog.rgb, v_fog.a);

  albedo.rgb = colorCorrection(albedo.rgb);

  gl_FragColor = albedo;
}
