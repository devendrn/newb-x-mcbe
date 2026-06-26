$input a_color0, a_position, a_texcoord0
#ifdef INSTANCING
  $input i_data1, i_data2, i_data3
#endif
$output v_fog, v_occlusionUVHeight, v_texcoord0, v_texcoord1

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>
#include <newb/config.h>

uniform vec4 Dimensions;
uniform vec4 ViewPosition;
uniform vec4 UVOffsetAndScale;
uniform vec4 FogAndDistanceControl;
uniform vec4 OcclusionHeightOffset;
uniform vec4 FogColor;
uniform vec4 Velocity;
uniform vec4 PositionBaseOffset;
uniform vec4 PositionForwardOffset;

void main() {
  vec2 texcoord = UVOffsetAndScale.xy + (a_texcoord0 * UVOffsetAndScale.zw);

  #ifndef NO_VARIETY
    float spriteSelector = a_color0.x * 255.0;
    texcoord.x += spriteSelector * UVOffsetAndScale.z;
  #endif

  const vec3 PARTICLE_BOX = vec3(30.0,30.0,30.0);

  vec3 basePos = a_position + PositionBaseOffset.xyz;
  vec3 worldPos = basePos - PARTICLE_BOX*trunc(basePos/PARTICLE_BOX);
  worldPos += PositionForwardOffset.xyz - 0.5*PARTICLE_BOX;

  bool isRain = (UVOffsetAndScale.w > 3.8*UVOffsetAndScale.z) && Dimensions.x < 0.1;
  vec3 velocity = Velocity.xyz;
  vec2 dimensions = NL_WEATHER_PARTICLE_SIZE*Dimensions.xy;

  if (isRain) {
    velocity.x *= NL_WEATHER_RAIN_SLANT;
    worldPos.x -= worldPos.y*velocity.x;
  }

  vec3 worldPosBottom = worldPos;
  vec3 worldPosTop = worldPosBottom + velocity*dimensions.y;

  vec4 screenSpacePosBottom = mul(u_modelViewProj, vec4(worldPosBottom, 1.0));
  vec4 screenSpacePosTop = mul(u_modelViewProj, vec4(worldPosTop, 1.0));

  vec2 screenSpaceUpDirection = (screenSpacePosTop.xy/screenSpacePosTop.w) - (screenSpacePosBottom.xy/screenSpacePosBottom.w);
  vec2 screenSpaceRightDirection = normalize(vec2(-screenSpaceUpDirection.y, screenSpaceUpDirection.x));

  vec4 pos = mix(screenSpacePosTop, screenSpacePosBottom, a_texcoord0.y);
  pos.xy += (0.5 - a_texcoord0.x) * screenSpaceRightDirection * dimensions.x;

  vec2 occlusionUV = 0.5 + (worldPos.xz + ViewPosition.xz) / 64.0;
  float occlusionHeight = (worldPos.y + ViewPosition.y - 0.5) / 255.0;

  float fogIntensity = calculateFogIntensity(pos.z, FogAndDistanceControl.z, FogAndDistanceControl.x, FogAndDistanceControl.y);

  v_fog = vec4(FogColor.rgb, fogIntensity);
  v_occlusionUVHeight = vec3(occlusionUV, occlusionHeight);
  v_texcoord0 = texcoord;
  v_texcoord1 = a_texcoord0;

  gl_Position = pos;
}

