$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_fog, v_texcoord0, v_lightmapUV, v_light, v_extra

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
    worldPos += vec3(0.5, 0.5, 0.5);
    viewDir = normalize(worldPos - ViewPositionAndTime.xyz);
    vec3 boardPlane = normalize(vec3(viewDir.z, 0.0, -viewDir.x));
    worldPos = (worldPos -
        ((((viewDir.yzx * boardPlane.zxy) - (viewDir.zxy * boardPlane.yzx)) *
        (a_color0.z - 0.5)) +
        (boardPlane * (a_color0.x - 0.5))));
    color = vec4(1.0, 1.0, 1.0, 1.0);
#else
    color = a_color0;
#endif

    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
	float relativeDist = camDis / FogAndDistanceControl.z;
	viewDir = modelCamPos / camDis;


#ifdef TRANSPARENT
    if(a_color0.a < 0.95) {
		float alphaFadeOut = clamp((camDis / FogAndDistanceControl.w),0.0,1.0);
		color.a = getWaterAlpha(a_color0.rgb);
		color.a = color.a + (0.5-0.5*color.a)*alphaFadeOut;
    };
#endif

	vec3 wPos = worldPos.xyz;
	vec3 cPos = a_position.xyz;
	vec3 bPos = fract(cPos);
	vec3 tiledCpos = cPos*vec3(cPos.x<15.99,cPos.y<15.99,cPos.z<15.99);


    vec4 COLOR = a_color0;
    vec2 uv0 = a_texcoord0;
    vec2 uv1 = a_texcoord1;
	vec2 lit = uv1*uv1;
	bool isColored = (color.g > min(color.r,color.b)) || !(color.r==color.g && color.r==color.b);
	float shade = isColored ? color.g*1.5 : color.g;

	// tree leaves detection
	#ifdef ALPHA_TEST
		bool isTree = isColored;
	#else
		bool isTree = isColored && (uv0.x<0.1 || uv0.x>0.9) && uv0.y<0.3;
	#endif
	isTree = (isTree && (bPos.x+bPos.y+bPos.z<0.001)) || (color.a < 0.005 && max(COLOR.g,COLOR.r)>0.37);

	// environment detections
	bool end = detectEnd(FogColor.rgb);
	bool nether = detectNether(FogColor.rgb, FogAndDistanceControl.xy);

	bool underWater = detectUnderwater(FogColor.rgb,FogAndDistanceControl.xy);
	float rainFactor = detectRain(FogAndDistanceControl.z,FogAndDistanceControl.xy);

	bool isWater = COLOR.b<0.02;
	float water = float(isWater);

	// sky colors
	vec3 zenithCol = getZenithCol(rainFactor,FogColor.rgb);
	vec3 horizonCol = getHorizonCol(rainFactor,FogColor.rgb);
	vec3 horizonEdgeCol = getHorizonEdgeCol(horizonCol,rainFactor,FogColor.rgb);
	if(underWater){
		vec3 fogcol = getUnderwaterCol(FogColor.rgb);
		zenithCol = fogcol;
		horizonCol = fogcol;
		horizonEdgeCol = fogcol;
	}


// time
highp float t = ViewPositionAndTime.w;

// convert color space to linear-space for color correction (not entirely accurate)
// and tree leaves, slab lighting fix
#ifdef SEASONS
	// season tree leaves are colored in fragment using sCol values
	vec3 sCol = COLOR.rgb;
	color.rgb = vec3_splat(1.0);

	uv1.y *= 1.00151;
#else
	if(isColored){color.rgb *= color.rgb*1.2;}
	if(isTree || (fract(cPos.y)==0.5 && fract(cPos.x)==0.0) ){uv1.y *= 1.00151;}
#endif

	vec3 torchColor = vec3(1.0,1.0,1.0);
    vec3 light = nl_lighting(a_color0.rgb, FogColor.rgb, rainFactor,uv1, isTree,
                 horizonCol, zenithCol, shade, end, nether);

	// mist (also used in underwater to decrease visibility)
	vec4 mistColor = renderMist(horizonEdgeCol, relativeDist, lit.x, rainFactor, nether,underWater,end,FogColor.rgb);
	mistColor.rgb *= max(0.75,uv1.y);
	mistColor.rgb += torchColor*torch_intensity*lit.x*0.3;

	if(underWater){
		nl_underwater_lighting(light, mistColor, lit, uv1, tiledCpos, cPos, torchColor, t);
	}


#ifdef ALPHA_TEST
	nl_foliage_wave(worldPos, light, rainFactor, lit,
					 uv0, bPos, COLOR, cPos, tiledCpos, t,
					 isColored, camDis, underWater );
#endif

	if (isWater) {
		color = nl_water(worldPos, color, light,cPos, COLOR, FogColor.rgb, horizonCol,
			  horizonEdgeCol, zenithCol, uv1, t, camDis,
			  rainFactor, tiledCpos, end, torchColor);
	}
	else {
		color.rgb *= light;
	}

	// loading chunks
	relativeDist += RenderChunkFogAlpha.x;

	vec4 fogColor = renderFog(horizonEdgeCol, relativeDist, nether, FogColor.rgb, FogAndDistanceControl.xy);

	if(nether){
		fogColor.rgb = mix(fogColor.rgb,vec3(0.8,0.2,0.12)*1.5,lit.x*(1.67-fogColor.a*1.67));
	}
	else if(!underWater){
		if(end){fogColor.rgb = vec3(0.16,0.06,0.2);}

		// to remove fog in heights
		float fogGradient = 1.0-max(-viewDir.y+0.1,0.0);
		fogGradient *= fogGradient*fogGradient;
		fogColor.a *= fogGradient;
	}

	// mix fog with mist
	mistColor = mix(mistColor,vec4(fogColor.rgb,1.0),fogColor.a);

	v_extra.b = water;
    v_texcoord0 = a_texcoord0;
    v_lightmapUV = a_texcoord1;
    v_color0 = color;
    v_fog = fogColor;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
}
