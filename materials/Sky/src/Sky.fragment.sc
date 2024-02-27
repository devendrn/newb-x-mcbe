#ifdef OPAQUE
$input v_zenithCol, v_horizonColTime, v_horizonEdgeColUnderwater, v_worldPos
#endif

#include <bgfx_shader.sh>
#include <newb/main.sh>

void main() {
#ifdef OPAQUE
  vec3 viewDir = normalize(v_worldPos);
  bool underWater = v_horizonEdgeColUnderwater.w > 0.5;

  vec3 skyColor = nlRenderSky(v_horizonEdgeColUnderwater.rgb, v_horizonColTime.rgb, v_zenithCol, -viewDir, v_horizonColTime.w, false, underWater);
  skyColor = colorCorrection(skyColor);

  gl_FragColor = vec4(skyColor, 1.0);
#else
  gl_FragColor = vec4(0.0,0.0,0.0,0.0);
#endif
}
