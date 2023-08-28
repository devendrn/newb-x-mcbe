#ifndef TAA_UTIL_H_HEADER_GUARD
#define TAA_UTIL_H_HEADER_GUARD

uniform vec4 SubPixelOffset;

vec4 jitterVertexPosition(vec3 worldPosition) { // #line 1
    mat4 offsetProj = u_proj;

#if BGFX_SHADER_LANGUAGE_GLSL
    offsetProj[2][0] += SubPixelOffset.x;
    offsetProj[2][1] -= SubPixelOffset.y;
#else
    offsetProj[0][2] += SubPixelOffset.x; // #line 8
    offsetProj[1][2] -= SubPixelOffset.y;
#endif
    return mul(offsetProj, mul(u_view, vec4(worldPosition, 1.0f)));
}

#endif // TAA_UTIL_H_HEADER_GUARD