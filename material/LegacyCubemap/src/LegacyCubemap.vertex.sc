$input a_position, a_texcoord0
$output v_texcoord0, v_color

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform mat4 CubemapRotation;

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;

void main() {

    vec4 pos =  mul(u_model[0], vec4(a_position, 1.0));
    vec4 color;

	// will be clamped in fragment shader
	color.a = (a_position.y-0.15)*10.0;

	// detections
	bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
	float rainFactor = detectRain(FogAndDistanceControl.z,FogAndDistanceControl.xy);

	// horizon color
	color.rgb = getHorizonCol(rainFactor,FogColor.rgb);
	color.rgb = getHorizonEdgeCol(color.rgb,rainFactor,FogColor.rgb);
	if(underWater){ color.rgb = getUnderwaterCol(FogColor.rgb); }

    v_color = color;
    gl_Position = mul(u_modelViewProj, mul(CubemapRotation, vec4(a_position, 1.0)));
    v_texcoord0 = a_texcoord0;
}
