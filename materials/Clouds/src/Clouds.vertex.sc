$input a_color0, a_position
#ifdef INSTANCING
	$input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_color1, v_color2

#include <bgfx_shader.sh>
#include <newb_legacy.sh>
#include <newb_x.sh>

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
#ifdef TRANSPARENT
#ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
    mat4 model = u_model[0];
#endif
	vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;

	float fade = clamp(2.0-2.0*length(worldPos.xyz)*0.004, 0.0, 1.0);

	float rain = detectRain(FogAndDistanceControl.xyz);
	vec3 zenith_col = getZenithCol(rain, FogColor.rgb);
	vec3 fog_col = getHorizonCol(rain, FogColor.rgb);
	fog_col = getHorizonEdgeCol(fog_col, rain, FogColor.rgb);

	v_color0 = vec4(worldPos, fade);
	v_color1 = vec4(zenith_col,rain);
	v_color2 = vec4(fog_col,ViewPositionAndTime.w);
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
	gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
