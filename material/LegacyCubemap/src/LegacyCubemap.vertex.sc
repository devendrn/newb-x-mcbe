$input a_position, a_texcoord0
$output v_texcoord0, v_color

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform mat4 CubemapRotation;

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;

void main() {
    vec4 color;

	// will be clamped in fragment shader
	color.a = (a_position.y - 0.15)*10.0;

	bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);

	// horizon color
	if (underWater) {
		color.rgb = getUnderwaterCol(FogColor.rgb);
	} else {
		float rainFactor = detectRain(FogAndDistanceControl.xyz);
		color.rgb = getHorizonCol(rainFactor, FogColor.rgb);
		color.rgb = getHorizonEdgeCol(color.rgb, rainFactor, FogColor.rgb);
	}

    v_color = color;
    v_texcoord0 = a_texcoord0;
    gl_Position = mul(u_modelViewProj, mul(CubemapRotation, vec4(a_position, 1.0)));
}
