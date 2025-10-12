$input v_fog, v_occlusionUVHeight, v_texcoord0, v_texcoord1

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform vec4 UVOffsetAndScale;
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
  vec4 occlLuminanceAndHeightThreshold = texture2D(s_OcclusionTexture, v_occlusionUVHeight.xy);

  float occlLuminance = occlLuminanceAndHeightThreshold.x;
  float occlHeightThreshold = occlLuminanceAndHeightThreshold.y;
  occlHeightThreshold += occlLuminanceAndHeightThreshold.z * 255.0;
  occlHeightThreshold -= OcclusionHeightOffset.x / 255.0;

  vec2 lightingUV = vec2(0.0, 0.0);
  if (!isOccluded(v_occlusionUVHeight.xy, v_occlusionUVHeight.z, occlHeightThreshold)) {
    float mixAmount = (v_occlusionUVHeight.z - occlHeightThreshold) * 25.0;
    lightingUV = vec2(occlLuminance * (1.0 - mixAmount), 1.0);
  }

  #ifdef NL_WEATHER_SPECK
    vec2 gv = 2.0*v_texcoord1 - 1.0;
    gv = 1.0 - gv*gv;
    float g = gv.x*gv.y;
    if (UVOffsetAndScale.w > 3.5*UVOffsetAndScale.z) { // isRain
      g *= 0.9*gv.x;
    }

    vec4 color = texture2D(s_WeatherTexture, UVOffsetAndScale.xy + 0.5*UVOffsetAndScale.zw);
    color.a = g*g;

    diffuse = mix(diffuse, color, NL_WEATHER_SPECK);
  #endif 

  vec3 light = texture2D(s_LightingTexture, lightingUV).rgb;

  diffuse.rgb *= diffuse.rgb*light;
  diffuse.rgb += 3.0*v_fog.rgb;

  diffuse.rgb = colorCorrection(diffuse.rgb);

  diffuse.a *= lightingUV.y*(1.0-v_fog.a);
  diffuse.a *= NL_WEATHER_PARTICLE_OPACITY;

  gl_FragColor = diffuse;
}

