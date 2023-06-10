$input v_color0, v_fog, v_texcoord0, v_lightmapUV, v_light, v_extra

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 FogColor;

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_LightMapTexture, 1);
SAMPLER2D(s_SeasonsTexture, 2);

void main() {
    vec4 diffuse;
    vec4 color = vec4_splat(1.0);

#if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)
    diffuse.rgb = vec3(1.0, 1.0, 1.0);
#else
    diffuse = texture2D(s_MatTexture, v_texcoord0);

#if defined(ALPHA_TEST)
    if (diffuse.a < 0.5) {
        discard;
    }
#endif

#if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
    diffuse.rgb *=
        mix(vec3(1.0, 1.0, 1.0),
            texture2D(s_SeasonsTexture, v_color0.xy).rgb * 2.0, v_color0.b);
    color.rgb = v_color0.aaa;
#else
    color = v_color0;
#endif

#endif

    diffuse.rgb *= diffuse.rgb;

    vec3 light_tint = texture2D(s_LightMapTexture, v_lightmapUV).rgb;
    light_tint = mix(light_tint.bbb, light_tint*light_tint, 0.35 + 0.65*v_lightmapUV.y*v_lightmapUV.y*v_lightmapUV.y);

    nl_glow(diffuse, color, light_tint, v_lightmapUV);

    if ( v_extra.b > 0.5 ) {
		diffuse.rgb = vec3_splat(mix(1.0,diffuse.b*1.8,WATER_TEX_OPACITY));
        diffuse.a = color.a;
	}

	diffuse.rgb *= color.rgb * light_tint;

    diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);

	diffuse.rgb = colorCorrection(diffuse.rgb);

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif

    gl_FragColor = diffuse;
}
