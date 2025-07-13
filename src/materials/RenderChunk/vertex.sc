$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_color1, v_fog, v_refl, v_texcoord0, v_lightmapUV, v_extra

#include <bgfx_shader.sh>
#include <newb/main.sh>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

SAMPLER2D_AUTOREG(s_MatTexture);

void main() {
  #ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
  #else
    mat4 model = u_model[0];
  #endif

  vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;

  #if !(defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY) || defined(INSTANCING))

  #ifdef NL_CHUNK_LOAD_ANIM
    worldPos.y -= NL_CHUNK_LOAD_ANIM*RenderChunkFogAlpha.x*RenderChunkFogAlpha.x*RenderChunkFogAlpha.x;
  #endif

  #ifdef RENDER_AS_BILLBOARDS
    worldPos += vec3(0.5,0.5,0.5);

    vec3 modelCamPos = ViewPositionAndTime.xyz - worldPos;
    float camDis = length(modelCamPos);
    vec3 viewDir = modelCamPos / camDis;

    vec3 boardPlane = normalize(vec3(-viewDir.z, 0.0, viewDir.x));
    worldPos -= (((viewDir.zxy * boardPlane.yzx) - (viewDir.yzx * boardPlane.zxy)) *
                 (a_color0.z - 0.5)) +
                 (boardPlane * (a_color0.x - 0.5));
    vec4 color = vec4(1.0,1.0,1.0,1.0);
  #else
    vec3 modelCamPos = ViewPositionAndTime.xyz - worldPos;
    float camDis = length(modelCamPos);
    vec3 viewDir = modelCamPos / camDis;

    vec4 color = a_color0;
  #endif

  float relativeDist = camDis / FogAndDistanceControl.z;

  vec3 cPos = a_position.xyz;
  vec3 bPos = fract(cPos);
  vec3 tiledCpos = fract(cPos*0.0625);

  vec2 uv1 = a_texcoord1;
  vec2 lit = uv1*uv1;

  bool isColored = color.r != color.g || color.r != color.b;
  float shade = isColored ? color.g*1.5 : color.g;

  // tree leaves detection
  #if defined(ALPHA_TEST) && !defined(RENDER_AS_BILLBOARDS)
    bool isTree = (isColored && (bPos.x+bPos.y+bPos.z < 0.001)) || color.a == 0.0;
  #else
    bool isTree = false;
  #endif

  nl_environment env = nlDetectEnvironment(FogColor.rgb, FogAndDistanceControl.xyz);
  nl_skycolor skycol = nlSkyColors(env, FogColor.rgb);

  // time
  highp float t = ViewPositionAndTime.w;

  // convert color space to linear-space
  #ifdef SEASONS
    isTree = true;

    // season tree leaves are colored in fragment
    color.w *= color.w;
    color = vec4(color.www, 1.0);
  #else
    if (isColored) {
      color.rgb *= color.rgb*1.2;
    }
  #endif

  vec3 torchColor; // modified by nl_lighting
  vec3 light = nlLighting(skycol, env, worldPos, torchColor, a_color0.rgb, FogColor.rgb, uv1, lit, isTree, shade, t);

  #if defined(ALPHA_TEST) && (defined(NL_PLANTS_WAVE) || defined(NL_LANTERN_WAVE)) && !defined(RENDER_AS_BILLBOARDS)
    nlWave(worldPos, light, env.rainFactor, uv1, lit, a_texcoord0, bPos, a_color0, cPos, tiledCpos, t, s_MatTexture, isColored, camDis, isTree);
  #endif

  // loading chunks
  relativeDist += RenderChunkFogAlpha.x;

  vec4 fogColor;
  fogColor.rgb = nlRenderSky(skycol, env, viewDir, FogColor.rgb, t);
  fogColor.a = nlRenderFogFade(relativeDist, FogColor.rgb, FogAndDistanceControl.xy);
  #ifdef NL_GODRAY
    fogColor.a = mix(fogColor.a, 1.0, min(NL_GODRAY*nlRenderGodRayIntensity(cPos, worldPos, t, uv1, relativeDist, FogColor.rgb), 1.0));
  #endif

  if (env.nether) {
    // blend fog with void color
    fogColor.rgb = colorCorrectionInv(FogColor.rgb);
  }

  #ifdef NL_CLOUDY_FOG
    float fg = smoothstep(0.0, 1.0-NL_CLOUDY_FOG, relativeDist);
    fg *= sin(5.0*viewDir.y + 2.0*viewDir.x - 0.1*t);
    fg *= sin(5.0*viewDir.y - 2.0*viewDir.x + viewDir.z + 0.1*t);
    fogColor.a += (1.0-fogColor.a)*fg*fg;
  #endif

  float water = 0.0;
  vec4 refl = vec4(0.0,0.0,0.0,0.0);
  #if defined(TRANSPARENT) && !(defined(RENDER_AS_BILLBOARDS) || defined(SEASONS))
    color.a = mix(color.a, 1.0, 0.5*clamp(relativeDist, 0.0, 1.0));
    if (a_color0.b > 0.3 && a_color0.a < 0.95) {
      water = 1.0;
      refl = nlWater(skycol, env, worldPos, color, a_color0, viewDir, light, cPos, tiledCpos, bPos.y, FogColor.rgb, lit, t, camDis, torchColor);
    } else {
      refl = nlRefl(skycol, env, color, lit, tiledCpos, camDis, worldPos, viewDir, torchColor, FogColor.rgb, FogAndDistanceControl.z, t);
    }
  #else
    refl = nlRefl(skycol, env, color, lit, tiledCpos, camDis, worldPos, viewDir, torchColor, FogColor.rgb, FogAndDistanceControl.z, t);
  #endif

  vec4 pos = mul(u_viewProj, vec4(worldPos, 1.0));
  #ifdef NL_RAIN_MIST_OPACITY
    if (env.rainFactor > 0.0) {
      float humidAir = env.rainFactor*lit.y*lit.y*nlWindblow(pos.xyz, t);
      fogColor.a = mix(fogColor.a, 1.0, humidAir*NL_RAIN_MIST_OPACITY);
    }
  #endif

  if (env.underwater) {
    nlUnderwaterLighting(light, pos.xyz, lit, uv1, tiledCpos, cPos, t, skycol.horizon);
  }

  color.rgb *= light;

  #if defined(NL_GLOW_SHIMMER) && !(defined(RENDER_AS_BILLBOARDS) || defined(SEASONS))
    float shimmer = nlGlowShimmer(cPos, t);
  #else
    float shimmer = 1.0;
  #endif

  #ifdef NL_LAVA_NOISE
    bool isc = (a_color0.r+a_color0.g+a_color0.b) > 2.999;
    bool isb = bPos.y < 0.891 && bPos.y > 0.889;
    if (isc && isb && (uv1.x > 0.81 && uv1.x < 0.876) && a_texcoord0.y > 0.45) {
      vec4 lava = nlLavaNoise(tiledCpos, t);
      #ifdef NL_LAVA_NOISE_BUMP
        worldPos.y += NL_LAVA_NOISE_BUMP*lava.a;
      #endif
      color.rgb *= lava.rgb;
    }
  #endif

  v_extra = vec4(shade, worldPos.y, water, shimmer);
  v_refl = refl;
  v_texcoord0 = a_texcoord0;
  v_lightmapUV = a_texcoord1;
  v_color0 = color;
  v_color1 = a_color0;
  v_fog = fogColor;

  #else

  vec4 pos = mul(u_viewProj, vec4(worldPos, 1.0));

  #endif

  gl_Position = pos;
}
