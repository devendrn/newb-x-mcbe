$input v_color0, v_fog, v_occlusionHeight, v_occlusionUV, v_texcoord0, v_worldPos

#include <bgfx_shader.sh>

uniform vec4 OcclusionHeightOffset;

SAMPLER2D_AUTOREG(s_LightingTexture);
SAMPLER2D_AUTOREG(s_OcclusionTexture);
SAMPLER2D_AUTOREG(s_WeatherTexture);

bool isOccluded(const vec2 occlUV, const float occlHeight, const float occlHeightThreshold) {
  #ifdef NO_OCCLUSION
    return false;
  #else
    #ifdef FLIP_OCCLUSION
      bool isUnder = occlHeight > occlHeightThreshold;
    #else
      bool isUnder = occlHeight < occlHeightThreshold;
    #endif
    return occlUV.x >= 0.0 && occlUV.x <= 1.0 && occlUV.y >= 0.0 && occlUV.y <= 1.0 && isUnder;
  #endif
}

void main() {
  vec4 diffuse = texture2D(s_WeatherTexture, v_texcoord0);
  vec4 occlLuminanceAndHeightThreshold = texture2D(s_OcclusionTexture, v_occlusionUV);

  float occlLuminance = occlLuminanceAndHeightThreshold.x;
  float occlHeightThreshold = occlLuminanceAndHeightThreshold.y;
  occlHeightThreshold += occlLuminanceAndHeightThreshold.z * 255.0;
  occlHeightThreshold -= OcclusionHeightOffset.x / 255.0;

  vec2 lightingUV = vec2(0.0, 0.0);
  if (!isOccluded(v_occlusionUV, v_occlusionHeight, occlHeightThreshold)) {
    float mixAmount = (v_occlusionHeight - occlHeightThreshold) * 25.0;
    lightingUV = vec2(occlLuminance * (1.0 - mixAmount), 1.0);
  }

  vec3 light = texture2D(s_LightingTexture, lightingUV).rgb;

  diffuse.rgb *= light;
  diffuse.a *= lightingUV.y;

  diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);

  gl_FragColor = diffuse;
}

