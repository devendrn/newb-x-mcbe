$input v_color0, v_color1, v_color2, v_color3
#if defined(GEOMETRY_PREPASS)
    $input v_texcoord0, v_normal, v_worldPos, v_prevWorldPos
#endif

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

void main() {
#if defined(OPAQUE)

    vec3 wPos = v_color3;
	float sphereY = max(0.0,wPos.y/sqrt(dot(wPos.xyz,wPos.xyz)));

	vec3 skyColor = renderSky(v_color2,v_color1,v_color0.rgb,sphereY);

	skyColor = colorCorrection(skyColor);

	gl_FragColor = vec4(skyColor,1.0);

    //Opaque
    //gl_FragColor = v_color0;

#elif defined(GEOMETRY_PREPASS)
    //GeometryPrepass
    vec3 normal = vec3(0.0, 1.0, 0.0);
    vec3 GNormal = normalize(normal);
    float rGNormalManhattanLength = 1.0f / (abs(GNormal.x) + abs(GNormal.y) + abs(GNormal.z));
    float NX = rGNormalManhattanLength * GNormal.x;
    float NY = rGNormalManhattanLength * GNormal.y;
    bool isDownFace = GNormal.z < 0.0;

    vec4 _841 = mul(u_viewProj, vec4(v_worldPos, 1.0));
    vec4 _850 = (_841 / _841.w) * 0.5 + 0.5;
    vec4 _861 = mul(u_prevViewProj, vec4(v_worldPos - u_prevWorldPosOffset.xyz, 1.0));
    vec4 _870 = (_861 / _861.w) * 0.5 + 0.5;
    vec2 motionVector = _850.xy - _870.xy;

    //ColorMetalness
    gl_FragData[0].xyz = v_color0.xyz;
    gl_FragData[0].w = 0.0;
    
    //Normal
    gl_FragData[1].x = isDownFace ? ((1.0f - abs(NY)) * ((NX >= 0.0) ? 1.0 : (-1.0))) : NX;
    gl_FragData[1].y = isDownFace ? ((1.0f - abs(NX)) * ((NY >= 0.0) ? 1.0 : (-1.0))) : NY;
    gl_FragData[1].zw = vec2(0.0);

    //EmissiveAmbientLinearRoughness
    gl_FragData[2] = vec4(1.0, 0.0, 0.0, 0.5);

    //MotionVectors
    gl_FragData[3].xy = motionVector;
    gl_FragData[3].zw = vec2(0.0);

#else
    //Fallback
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    
#endif
}
