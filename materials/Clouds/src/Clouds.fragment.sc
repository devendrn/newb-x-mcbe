$input v_color0, v_worldpos

#include <bgfx_shader.sh>
#include <newb_legacy.sh>
#include <newb_x.sh>

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
	vec4 color;
	vec3 v_dir = normalize(v_worldpos);

	vec3 zenith_col = getZenithCol(v_color0.r, FogColor.rgb);
	vec3 horizon_col = getHorizonCol(v_color0.r, FogColor.rgb);
	vec3 fog_col = getHorizonEdgeCol(horizon_col, v_color0.r, FogColor.rgb);

	color = render_clouds(v_dir, v_worldpos, v_color0.r, ViewPositionAndTime.w, fog_col, zenith_col);
	color.rgb = colorCorrection(color.rgb);

	color.a *= v_color0.a;

	gl_FragColor = color;
}
