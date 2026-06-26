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
    bool inBounds = occlUV.x >= 0.0 && occlUV.x <= 1.0 && occlUV.y >= 0.0 && occlUV.y <= 1.0;
    #ifdef FLIP_OCCLUSION
      bool isUnder = occlHeight > occlHeightThreshold;
    #else
      bool isUnder = occlHeight < occlHeightThreshold;
    #endif
    return inBounds && isUnder;
  #endif
}

void main() {
  vec4 diffuse = texture2D(s_WeatherTexture, v_texcoord0);
  vec4 occlData = texture2D(s_OcclusionTexture, v_occlusionUVHeight.xy);
  
  // occlData.x bits 1-8 for occlusion lsb part
  // occlData.y bits 1-2 for occlusion msb part
  // occlData.y bits 3-8 for ??
  // occlData.z bits 1-4 for light level
  // occlData.z bits 5-8 for ??
  // uvec4 uocclData = uvec4(round(occlData*255.0));
  // float occLum = float(uocclData.z & 15u)/15.0;
  // float occHeight = float((uocclData.x | (uocclData.y << 8u)) & 1023u)/1023.0;
  float occlLuminance = fract(occlData.z*16.0);
  float occlHeightThreshold = ((0.25*occlData.x)+fract(64.0*occlData.y))*1024.0;
  occlHeightThreshold = (occlHeightThreshold + OcclusionHeightOffset.x) / 255.0;

  vec2 lightingUV = vec2(0.0, 0.0);
  if (!isOccluded(v_occlusionUVHeight.xy, v_occlusionUVHeight.z, occlHeightThreshold)) {
    float mixAmount = clamp((v_occlusionUVHeight.z - occlHeightThreshold) * 25.0, 0.0, 1.0);
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

  gl_FragColor = diffuse;
}

