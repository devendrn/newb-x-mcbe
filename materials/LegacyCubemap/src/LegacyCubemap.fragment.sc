$input v_texcoord0, v_fogColor, v_worldPos, v_underwaterRainTime

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D(s_MatTexture, 0);

void main() {
  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);

  vec3 viewDir = normalize(v_worldPos);
  bool underWater = v_underwaterRainTime.x > 0.5;
  float rainFactor = v_underwaterRainTime.y;

  vec3 zenithCol;
  vec3 horizonCol;
  vec3 horizonEdgeCol;
  if (underWater) {
    vec3 fogcol = getUnderwaterCol(v_fogColor);
    zenithCol = fogcol;
    horizonCol = fogcol;
    horizonEdgeCol = fogcol;
  } else {
    vec3 fs = getSkyFactors(v_fogColor);
    zenithCol = getZenithCol(rainFactor, v_fogColor, fs);
    horizonCol = getHorizonCol(rainFactor, v_fogColor, fs);
    horizonEdgeCol = getHorizonEdgeCol(horizonCol, rainFactor, v_fogColor);
  }

  vec3 skyColor = nlRenderSky(horizonEdgeCol, horizonCol, zenithCol, -viewDir, v_fogColor, v_underwaterRainTime.z, rainFactor, false, underWater, false);

  float fade = clamp(-10.0*viewDir.y, 0.0, 1.0);
  vec4 color = vec4(colorCorrection(skyColor), fade);

  diffuse = mix(color, diffuse, diffuse.a);

  gl_FragColor = diffuse;
}
