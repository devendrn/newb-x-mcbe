$input a_color0, a_position

$output v_color0

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 CloudColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main()
{
	// make cloud thin
	vec3 pos = a_position;
	pos.y *= 0.1;

    vec3 worldPos = mul(u_model[0], vec4(pos, 1.0)).xyz;

	// make cloud plane sperical
	float len = length(worldPos.xz)*0.004;
	worldPos.y -= len*len*clamp(0.2*worldPos.y, -1.0, 1.0);

	// time
	highp float t = ViewPositionAndTime.w;

	float rain = detectRain(FogAndDistanceControl.xyz);

	vec4 color = vec4(CloudColor.rgb,1.0);
	color = renderClouds(color, worldPos.xz, t, rain);
	color.rgb = mix(color.rgb,vec3(color.g*FogColor.g*3.5),rain*0.8);

	// cloud depth
	worldPos.y += NL_CLOUD_DEPTH*color.a*(10.0-7.0*rain);

	color.a *= NL_CLOUD_OPACITY;

#ifdef NL_AURORA
	vec4 auroras = renderAurora(worldPos.xz,t,rain);
	auroras *= max(1.0-(1.7*color.a),0.0);
	auroras *= 1.0-min(4.5*max(FogColor.r,FogColor.b),1.0);
	auroras.rgb *= NL_AURORA;
	color += auroras;
#endif

	// fade out cloud layer
	float depth = length(worldPos.xyz)*0.002;
	color.a *= clamp(2.0-2.0*depth,0.0,1.0);

	v_color0 = color;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
}
