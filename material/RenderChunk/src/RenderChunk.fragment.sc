$input v_color0, v_fog, v_texcoord0, v_lightmapUV, v_light, v_extra

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 FogColor;

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_LightMapTexture, 1);
SAMPLER2D(s_SeasonsTexture, 2);

void main() {
    vec4 diffuse;

#if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)
    diffuse.rgb = vec3(1.0, 1.0, 1.0);
#else
    diffuse = texture2D(s_MatTexture, v_texcoord0);
    diffuse.rgb *= diffuse.rgb;

#if defined(ALPHA_TEST)
    if (diffuse.a < 0.5) {
        discard;
    }
#endif


#if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
    diffuse.rgb *=
        mix(vec3(1.0, 1.0, 1.0),
            texture2D(s_SeasonsTexture, v_color0.xy).rgb * 2.0, v_color0.b);
    diffuse.rgb *= v_color0.aaa;
#else
    diffuse *= v_color0;
#endif
#endif

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif


    vec3 light_tint = texture2D(s_LightMapTexture, v_lightmapUV).rgb;
    light_tint = mix(light_tint.bbb, light_tint*light_tint, 0.35 + 0.65*v_lightmapUV.y*v_lightmapUV.y*v_lightmapUV.y);
    diffuse.rgb *= light_tint;

    if ( v_extra.b > 0.5 ) {
		diffuse.rgb = vec3(mix(1.0,diffuse.b*1.8,WATER_TEX_OPACITY));
        diffuse.rgb *= v_color0.rgb;
        diffuse.a = v_color0.a;
	}

    //diffuse.rgb = mix(diffuse.rgb, FogColor.rgb, v_fog.a);

	// color correction
	diffuse.rgb = colorCorrection(diffuse.rgb);

    gl_FragColor = diffuse;
}
