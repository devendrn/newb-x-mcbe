$input a_color0, a_position, a_texcoord0
#ifdef INSTANCING
  $input i_data1, i_data2, i_data3
#endif
$output v_color0, v_fog, v_occlusionHeight, v_occlusionUV, v_texcoord0, v_worldPos

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>

uniform vec4 Dimensions;
uniform vec4 ViewPosition;
uniform vec4 UVOffsetAndScale;
uniform vec4 FogAndDistanceControl;
uniform vec4 OcclusionHeightOffset;
uniform vec4 FogColor;
uniform vec4 Velocity;
uniform vec4 PositionBaseOffset;
uniform vec4 PositionForwardOffset;

// vec3 fmod(vec3 a, vec3 b) { return a - b*trunc(a/b); }

void main() {
  #ifdef INSTANCING
    mat4 model = mtxFromRows(i_data1, i_data2, i_data3, vec4(0.0, 0.0, 0.0, 1.0));
    vec3 worldPos = instMul(model, vec4(a_position, 1.0)).xyz;
  #else
    vec3 worldPos = mul(u_model[0], vec4(a_position, 1.0)).xyz;
  #endif

  vec2 texcoord = UVOffsetAndScale.xy + (a_texcoord0 * UVOffsetAndScale.zw);

  #ifndef NO_VARIETY
    float spriteSelector = a_color0.x * 255.0;
    texcoord.x += spriteSelector * UVOffsetAndScale.z;
  #endif

  bool isRain = UVOffsetAndScale.x < 0.0001;
  vec3 velocity = Velocity.xyz;
  if (isRain) {
    velocity.x *= 4.0;
  }

  const vec3 PARTICLE_BOX = vec3(30.0, 30.0, 30.0);
  vec3 worldSpacePos = mod(a_position + PositionBaseOffset.xyz, PARTICLE_BOX); // should this be fmod?
  worldSpacePos += PositionForwardOffset.xyz - 0.5*PARTICLE_BOX;
  if (isRain) {
    worldSpacePos.xz -= worldSpacePos.y*velocity.xz;
  }

  vec3 worldSpacePosBottom = worldSpacePos;
  vec3 worldSpacePosTop = worldSpacePosBottom + velocity*Dimensions.y;

  vec4 screenSpacePosBottom = mul(u_modelViewProj, vec4(worldSpacePosBottom, 1.0));
  vec4 screenSpacePosTop = mul(u_modelViewProj, vec4(worldSpacePosTop, 1.0));

  vec2 screenSpaceUpDirection = (screenSpacePosTop.xy / screenSpacePosTop.w) - (screenSpacePosBottom.xy / screenSpacePosBottom.w);
  vec2 screenSpaceRightDirection = normalize(vec2(-screenSpaceUpDirection.y, screenSpaceUpDirection.x));

  vec4 pos = mix(screenSpacePosTop, screenSpacePosBottom, a_texcoord0.y);
  pos.xy += (0.5 - a_texcoord0.x) * screenSpaceRightDirection * Dimensions.x;

  vec2 occlusionUV = 0.5 + (worldSpacePos.xz + ViewPosition.xz) / 64.0;
  float occlusionHeight = (worldSpacePos.y + ViewPosition.y - 0.5) / 255.0;

  float fogIntensity = calculateFogIntensity(pos.z, FogAndDistanceControl.z, FogAndDistanceControl.x, FogAndDistanceControl.y);

  v_fog = vec4(FogColor.rgb, fogIntensity);
  v_color0 = a_color0;
  v_occlusionHeight = occlusionHeight;
  v_occlusionUV = occlusionUV;
  v_texcoord0 = texcoord;
  v_worldPos = worldPos;

  gl_Position = pos;
}

