$input a_color0, a_position
#ifdef INSTANCING
	$input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 CloudColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main() {
#ifdef TRANSPARENT
	vec3 pos = a_position;
	pos.y *= 0.1;

#ifdef INSTANCING
    mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
    mat4 model = u_model[0];
#endif

	// make cloud thin

	vec3 worldPos = mul(model, vec4(pos, 1.0)).xyz;

	// make cloud plane sperical
	float len = length(worldPos.xz)*0.004;
	worldPos.y -= len*len*clamp(0.2*worldPos.y, -1.0, 1.0);

	// time
	highp float t = ViewPositionAndTime.w;

	float rain = detectRain(FogAndDistanceControl.xyz);

	vec4 color = vec4(CloudColor.rgb, 1.0);
	color = renderClouds(color, worldPos.xz, t, rain);
	color.rgb = mix(color.rgb,vec3_splat(color.g*FogColor.g*3.5), rain*0.8);

	// cloud depth
	worldPos.y += NL_CLOUD_DEPTH*color.a*(10.0-7.0*rain);

	color.a *= NL_CLOUD_OPACITY;

#ifdef NL_AURORA
	vec4 auroras = renderAurora(worldPos.xz, t, rain);
	auroras *= max(1.0-1.7*color.a, 0.0);
	auroras *= 1.0-min(4.5*max(FogColor.r, FogColor.b), 1.0);
	auroras.rgb *= NL_AURORA;
	color += auroras;
#endif

	// fade out cloud layer
	color.a *= clamp(2.0-2.0*length(worldPos.xyz)*0.002, 0.0, 1.0);

	v_color0 = color;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
	v_color0 = vec4(0.0,0.0,0.0,0.0);
	gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
