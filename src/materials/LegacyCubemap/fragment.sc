$input v_texcoord0, v_fogColor, v_worldPos, v_underwaterRainTime

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);

// obsolete now
// could be used for clouds, aurora?

void main() {
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
  vec4 sky = vec4(nlRenderSky(skycol, env, -viewDir, v_fogColor, v_underwaterRainTime.z), smoothstep(0.1, -0.3, viewDir.y));

  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);
  diffuse.rgb *= 0.4 + 3.1*diffuse.rgb;
  diffuse = mix(sky, diffuse, diffuse.a);

diffuse.rgb = colorCorrection(diffuse.rgb,gl_FragCoord.xy,u_viewRect.zw);

  gl_FragColor = diffuse;
}
