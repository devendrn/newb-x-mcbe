$input v_texcoord0, v_zenithCol, v_horizonColTime, v_horizonEdgeColUnderwater, v_worldPos

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D(s_MatTexture, 0);

void main() {
  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);

  vec3 viewDir = normalize(v_worldPos);
  bool underWater = v_horizonEdgeColUnderwater.w > 0.5;

  vec3 skyColor = nlRenderSky(v_horizonEdgeColUnderwater.rgb, v_horizonColTime.rgb, v_zenithCol, -viewDir, v_horizonColTime.w, false, underWater);

  float fade = clamp(-10.0*viewDir.y, 0.0, 1.0);
  vec4 color = vec4(colorCorrection(skyColor), fade);

  diffuse = mix(color, diffuse, diffuse.a);

  gl_FragColor = diffuse;
}
