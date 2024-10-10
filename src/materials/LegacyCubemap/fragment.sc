$input v_texcoord0, v_fogColor, v_worldPos, v_underwaterRainTime

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);

void main() {
  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);

  vec3 viewDir = normalize(v_worldPos);

  nl_environment env;
  env.end = false;
  env.nether = false;
  env.underwater = v_underwaterRainTime.x > 0.5;
  env.rainFactor = v_underwaterRainTime.y;

  nl_skycolor skycol;
  if (env.underwater) {
    skycol = nlUnderwaterSkyColors(env.rainFactor, v_fogColor.rgb);
  } else {
    skycol = nlOverworldSkyColors(env.rainFactor, v_fogColor.rgb);
  }

  vec3 skyColor = nlRenderSky(skycol, env, -viewDir, v_fogColor, v_underwaterRainTime.z);

  float fade = clamp(-10.0*viewDir.y, 0.0, 1.0);
  vec4 color = vec4(colorCorrection(skyColor), fade);

  diffuse = mix(color, diffuse, diffuse.a);

  gl_FragColor = diffuse;
}
