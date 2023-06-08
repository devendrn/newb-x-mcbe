$input a_color0, a_position
#ifdef GEOMETRY_PREPASS
    $input a_texcoord0
    #ifdef INSTANCING
        $input i_data0, i_data1, i_data2, i_data3
    #endif
#endif

$output v_color0, v_color1, v_color2, v_color3

#ifdef GEOMETRY_PREPASS
    $output v_texcoord0, v_normal, v_worldPos, v_prevWorldPos
#endif

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;

void main() {
#if defined(OPAQUE)

	vec3 pos = a_position;
	pos.y -= a_color0.r*a_color0.r*0.4;	// Displaces the sky edge

	vec3 wPos = pos.xyz;
	wPos.y += 0.148;

	// detections
	bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
	float rainFactor = detectRain(FogAndDistanceControl.z, FogAndDistanceControl.xy);

	// sky colors
	vec3 zenith_color = getZenithCol(rainFactor, FogColor.rgb);
	vec3 horizon_color = getHorizonCol(rainFactor, FogColor.rgb);
	vec3 horizon_edge_color = getHorizonEdgeCol(horizon_color,rainFactor,FogColor.rgb);

	// underwater sky
	if(underWater){
		vec3 fogcol = getUnderwaterCol(FogColor.rgb);
		zenith_color = fogcol;
		horizon_color = fogcol;
		horizon_edge_color = fogcol;
	}

    //Opaque
    v_color0.rgb = zenith_color;
    v_color1 = horizon_color;
    v_color2 = horizon_edge_color;
    v_color3 = wPos;
    gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));


#elif defined(GEOMETRY_PREPASS)
    //GeometryPrepass
    mat4 model;
    #ifdef INSTANCING
        model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
    #else
        model = u_model[0];
    #endif

    v_normal = vec3(0.0, 0.0, 0.0);
    v_texcoord0 = a_texcoord0;
    v_worldPos = mul(model, vec4(a_position, 1.0)).xyz;
    v_prevWorldPos = mul(u_model[0], vec4(a_position, 1.0)).xyz;
    v_color0 = mix(SkyColor, FogColor, a_color0.x);
    gl_Position = mul(u_modelViewProj, vec4(a_position, 1.0));
#else
    //Fallback
    v_color0 = vec4(0.0, 0.0, 0.0, 0.0);
    gl_Position = vec4(0.0, 0.0, 0.0, 0.0);
#endif
}
