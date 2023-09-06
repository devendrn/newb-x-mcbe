$input a_position, a_color0, a_texcoord0, a_indices, a_normal
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2
#endif

$output v_color0, v_fog, v_light, v_texcoord0, v_edgemap

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>
#include <MinecraftRenderer.Materials/DynamicUtil.dragonh>
#include <MinecraftRenderer.Materials/TAAUtil.dragonh>
#include <newb_legacy.sh>

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
uniform vec4 LightDiffuseColorAndIntensity;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];
uniform vec4 ViewPositionAndTime;

vec3 lighting(vec3 position, vec4 normal, mat4 world) {
  float intensity;
  vec3 light;

#ifdef FANCY
  vec3 N = normalize(mul(world, normal)).xyz;

  N.y *= TileLightColor.w; //TileLightColor.w contains the direction of the light
  N.xz *= N.xz;

  intensity =  (0.5 + N.y*0.5)*0.5 - N.x*0.1 + N.z*0.1 + 0.5;
  intensity *= intensity;
#else
  // a_normal is passed in
  intensity = (0.7+abs(normal.y)*0.3)*(0.9+abs(normal.x)*0.1);
#endif

  intensity *= TileLightColor.b*TileLightColor.b*NL_SUN_INTENSITY*1.2;

  intensity += OverlayColor.a * 0.35;

  float factor = TileLightColor.b-TileLightColor.r;
  light = intensity*vec3(1.0-2.8*factor,1.0-2.7*factor,1.0);
  light *= vec3(1.0,1.0,1.0) + FogColor.rgb*0.3;

  light *= 1.0-0.3*float(position.y>0.0);

  return light;
}

void main() {
  mat4 World = u_model[0];

  //StandardTemplate_InvokeVertexPreprocessFunction
  World = mul(World, Bones[int(a_indices)]);

  vec2 texcoord0 = a_texcoord0;
  texcoord0 = applyUvAnimation(texcoord0, UVAnimation);

  //StandardTemplate_VertSharedTransform
  vec3 worldPosition;
#ifdef INSTANCING
  mat4 model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0.0, 0.0, 0.0, 1.0));
  worldPosition = instMul(model, vec4(a_position, 1.0)).xyz;
#else
  worldPosition = mul(World, vec4(a_position, 1.0)).xyz;
#endif

  vec4 position; // = mul(u_viewProj, vec4(worldPosition, 1.0));
  //StandardTemplate_InvokeVertexOverrideFunction
  position = jitterVertexPosition(worldPosition);

#if defined(DEPTH_ONLY)
  v_texcoord0 = vec2(0.0, 0.0);
  v_color0 = vec4(0.0, 0.0, 0.0, 0.0);
#else
  v_texcoord0 = texcoord0;
  v_color0 = a_color0;
#endif

  vec4 edgeMap = fract(vec4(v_texcoord0.xy*128.0,v_texcoord0.xy*256.0));
  edgeMap.x = float(edgeMap.x<0.5);
  edgeMap.y = float(edgeMap.y<0.5);
  edgeMap.z = float(edgeMap.z<0.5);
  edgeMap.w = float(edgeMap.w<0.5);
  edgeMap = 2.0*edgeMap - 1.0;

  // environment detections
  bool end = detectEnd(FogColor.rgb, FogControl.xy);
  bool nether = detectNether(FogColor.rgb, FogControl.xy);
  bool underWater = detectUnderwater(FogColor.rgb, FogControl.xy);

  float rainFactor = detectRain(FogControl.xyz);
  vec3 newFog;
  if (underWater) {
    newFog = getUnderwaterCol(FogColor.rgb);
  } else if (end) {
    newFog = getEndSkyCol();
  } else {
    newFog = getHorizonCol(rainFactor, FogColor.rgb);
    newFog = getHorizonEdgeCol(newFog.rgb, rainFactor, FogColor.rgb);
  }

  // relative cam dist
  float camDist = position.z/FogControl.z;

  vec4 fogColor = renderFog(newFog, camDist, nether, FogColor.rgb, FogControl.xy);

  vec3 light = lighting(a_position, a_normal, World);
  light += 0.5*newFog*TileLightColor.x;

  // nether,end,underwater tint
  if (nether) {
    light *= TileLightColor.x*vec3(1.4,0.96,0.9);
  } else if (end) {
    light *= vec3(2.1,1.5,2.3);
  } else if (underWater) {
    light += NL_UNDERWATER_BRIGHTNESS;
    light *= mix(normalize(fogColor.rgb),vec3(1.0,1.0,1.0),TileLightColor.x*0.5);
    highp float t = ViewPositionAndTime.w;
    light += NL_CAUSTIC_INTENSITY*max(TileLightColor.x-0.46,0.0)*(0.5+0.5*sin(t + dot(a_position.xyz,vec3_splat(1.5)) ));
  }

  v_fog = fogColor;
  v_edgemap = edgeMap;
  v_light = vec4(light, 1.0);
  gl_Position = position;
}
