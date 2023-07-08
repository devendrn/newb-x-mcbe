$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
	$input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_color1, v_fog, v_refl, v_texcoord0, v_lightmapUV, v_extra

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

void main() {

    mat4 model;
#ifdef INSTANCING
    model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
    model = u_model[0];
#endif

    vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;
    vec4 color;
	vec3 viewDir;

#ifdef RENDER_AS_BILLBOARDS
    worldPos += vec3(0.5,0.5,0.5);
    viewDir = normalize(worldPos - ViewPositionAndTime.xyz);
    vec3 boardPlane = normalize(vec3(viewDir.z, 0.0, -viewDir.x));
    worldPos = (worldPos -
        ((((viewDir.yzx * boardPlane.zxy) - (viewDir.zxy * boardPlane.yzx)) *
        (a_color0.z - 0.5)) +
        (boardPlane * (a_color0.x - 0.5))));
    color = vec4(1.0,1.0,1.0,1.0);
#else
    color = a_color0;
#endif

    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
	float relativeDist = camDis / FogAndDistanceControl.z;
	viewDir = modelCamPos / camDis;


	vec3 wPos = worldPos.xyz;
	vec3 cPos = a_position.xyz;
	vec3 bPos = fract(cPos);
	vec3 tiledCpos = fract(cPos*0.0625);

    vec2 uv0 = a_texcoord0;
    vec2 uv1 = a_texcoord1;
	vec2 lit = uv1*uv1;

	bool isColored = (color.g > min(color.r, color.b)) || !(color.r == color.g && color.r == color.b);
	float shade = isColored ? color.g*1.5 : color.g;

	// tree leaves detection
#ifdef ALPHA_TEST
	bool isTree = (isColored && (bPos.x+bPos.y+bPos.z < 0.001)) || (color.a == 0.0 && max(color.g,color.r) > 0.37);
#else
	bool isTree = false;
#endif

	// environment detections
	bool end = detectEnd(FogColor.rgb);
	bool nether = detectNether(FogColor.rgb, FogAndDistanceControl.xy);
	bool underWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
	float rainFactor = detectRain(FogAndDistanceControl.xyz);

	// sky colors
	vec3 zenithCol;
	vec3 horizonCol;
	vec3 horizonEdgeCol;
	if (underWater) {
		vec3 fogcol = getUnderwaterCol(FogColor.rgb);
		zenithCol = fogcol;
		horizonCol = fogcol;
		horizonEdgeCol = fogcol;
	} else if (end) {
		vec3 fogcol = getEndSkyCol();
		zenithCol = fogcol;
		horizonCol = fogcol;
		horizonEdgeCol = fogcol;
	} else {
		zenithCol = getZenithCol(rainFactor, FogColor.rgb);
		horizonCol = getHorizonCol(rainFactor, FogColor.rgb);
		horizonEdgeCol = getHorizonEdgeCol(horizonCol, rainFactor, FogColor.rgb);
	}

	// time
	highp float t = ViewPositionAndTime.w;

// convert color space to linear-space
#ifdef SEASONS
	isTree = true;

	// season tree leaves are colored in fragment
	color.w *= color.w;
	color = vec4(color.www, 1.0);

	// tree leaves shadow fix
	uv1.y *= 1.00151;
#else
	if (isColored) {
		color.rgb *= color.rgb*1.2;
	}

	// tree and slab shadow fix
	if (isTree || (bPos.y == 0.5 && bPos.x == 0.0)) {
		uv1.y *= 1.00151;
	}
#endif

	vec3 torchColor; // modified by nl_lighting

	vec4 mistColor = renderMist(horizonEdgeCol, relativeDist, lit.x, rainFactor, nether,underWater,end,FogColor.rgb);

    vec3 light = nl_lighting(torchColor, a_color0.rgb, FogColor.rgb, rainFactor,uv1, lit, isTree,
                 horizonCol, zenithCol, shade, end, nether, underWater, t);

	mistColor.rgb *= max(0.75, uv1.y);
	mistColor.rgb += 0.3*torchColor*NL_TORCH_INTENSITY*lit.x;

#ifdef ALPHA_TEST
#if defined(NL_PLANTS_WAVE) || defined(NL_LANTERN_WAVE)
	nl_wave(worldPos, light, rainFactor, uv1, lit,
					 uv0, bPos, a_color0, cPos, tiledCpos, t,
					 isColored, camDis, underWater, isTree);
#endif
#endif

#ifdef NL_CHUNK_LOAD_ANIM
	// slide in anim
	worldPos.y -= NL_CHUNK_LOAD_ANIM*pow(RenderChunkFogAlpha.x,3.0);
#endif

	// loading chunks
	relativeDist += RenderChunkFogAlpha.x;

	vec4 fogColor = renderFog(horizonEdgeCol, relativeDist, nether, FogColor.rgb, FogAndDistanceControl.xy);

	if (nether) {
		fogColor.rgb = mix(fogColor.rgb, vec3(0.8,0.2,0.12)*1.5,
			lit.x*(1.67-fogColor.a*1.67));
	} else if (!underWater) {
		// to remove fog in heights
		float fogGradient = 1.0-max(-viewDir.y+0.1,0.0);
		fogColor.a *= fogGradient*fogGradient*fogGradient;
	}

	vec4 refl;
	vec4 pos;
#ifdef TRANSPARENT

    if (a_color0.a < 0.95) {
		color.a += (0.5-0.5*color.a)*clamp((camDis/FogAndDistanceControl.w),0.0,1.0);
    };

	bool isWater = a_color0.b > 0.3 && a_color0.a < 0.95;
	float water = float(isWater);
	if (isWater) {
		refl = nl_water(worldPos, color, viewDir, light, cPos, bPos.y, a_color0, FogColor.rgb, horizonCol,
			  horizonEdgeCol, zenithCol, uv1, lit, t, camDis,
			  rainFactor, tiledCpos, end, torchColor);
		pos = mul(u_viewProj, vec4(worldPos, 1.0));
	} else {
		pos = mul(u_viewProj, vec4(worldPos, 1.0));
		refl = nl_refl(color, mistColor, lit, uv1, tiledCpos,
			camDis, wPos, viewDir, torchColor, horizonCol,
			zenithCol, rainFactor, FogAndDistanceControl.z, t, pos.xyz);
	}
#else
	float water = 0.0;
	pos = mul(u_viewProj, vec4(worldPos, 1.0));
	refl = nl_refl(color, mistColor, lit, uv1, tiledCpos,
		camDis, wPos, viewDir, torchColor, horizonCol,
		zenithCol, rainFactor, FogAndDistanceControl.z, t, pos.xyz);
#endif

	if (underWater) {
		nl_underwater_lighting(light, pos.xyz, lit, uv1, tiledCpos, cPos, t);
	}

	color.rgb *= light;

	// mix fog with mist
	mistColor = mix(mistColor, vec4(fogColor.rgb,1.0), fogColor.a);

	v_extra.rgb = vec3(shade, worldPos.y, water);
	v_refl = refl;
    v_texcoord0 = a_texcoord0;
    v_lightmapUV = a_texcoord1;
    v_color0 = color;
	v_color1 = a_color0;
    v_fog = mistColor;
    gl_Position = pos;
}
