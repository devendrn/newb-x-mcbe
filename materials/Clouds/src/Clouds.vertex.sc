$input a_color0, a_position
#ifdef INSTANCING
	$input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_worldpos

#include <bgfx_shader.sh>
#include <newb_legacy.sh>
#include <newb_x.sh>

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;

void main() {
#ifdef TRANSPARENT
#ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
    mat4 model = u_model[0];
#endif
	vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;

	vec4 color;
	color.r = detectRain(FogAndDistanceControl.xyz);
	color.a = clamp(2.0-2.0*length(worldPos.xyz)*0.004, 0.0, 1.0);

	v_color0 = color;
	v_worldpos = worldPos;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
	v_color0 = vec4(0.0,0.0,0.0,0.0);
	gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
