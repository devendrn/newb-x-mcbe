$input a_texcoord0, a_position

$output v_color0, v_pos, v_texcoord0

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main()
{
	vec3 pos = a_position;

	// pi/1800 (one complete rotation per hour)
	highp float t = 0.00174532925*ViewPositionAndTime.w;

	// rotate skybox
	float sinA = sin(t);
	float cosA = cos(t);
	pos.xz = mul(mat2(cosA,-sinA,sinA,cosA), pos.xz);

	v_color0 = colorCorrection(getEndSkyCol());
	v_texcoord0 = (2.5 - abs(pos.y))*a_texcoord0;
	v_pos = pos;
    gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));
}
