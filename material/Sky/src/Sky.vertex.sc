$input a_color0, a_position

$output v_color0, v_color1, v_color2, v_color3

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

//uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;

void main() {
#if defined(OPAQUE)
    //Opaque

	vec3 pos = a_position;
	pos.y -= 0.4*a_color0.r*a_color0.r;	// Displaces the sky edge

	vec3 wPos = pos.xyz;
	wPos.y += 0.148;

	// detections
	bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
	float rainFactor = detectRain(FogAndDistanceControl.xyz);

	// sky colors
	vec3 zenith_color = getZenithCol(rainFactor, FogColor.rgb);
	vec3 horizon_color = getHorizonCol(rainFactor, FogColor.rgb);
	vec3 horizon_edge_color = getHorizonEdgeCol(horizon_color, rainFactor, FogColor.rgb);

	// underwater sky
	if (underWater) {
		vec3 fogcol = getUnderwaterCol(FogColor.rgb);
		zenith_color = fogcol;
		horizon_color = fogcol;
		horizon_edge_color = fogcol;
	}

    v_color0 = zenith_color;
    v_color1 = horizon_color;
    v_color2 = horizon_edge_color;
    v_color3 = wPos;

    gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));

#else
    //Fallback
    v_color0 = vec3(0.0, 0.0, 0.0);
    gl_Position = vec4(0.0, 0.0, 0.0, 0.0);
#endif
}
