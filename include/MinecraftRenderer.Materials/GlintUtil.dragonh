#ifndef GLINT_UTIL_H_HEADER_GUARD
#define GLINT_UTIL_H_HEADER_GUARD

vec2 calculateLayerUV(const vec2 origUV, const float offset, const float rotation, const vec2 scale) { // #line 1
    vec2 uv = origUV;
    uv -= 0.5;

    float rsin = sin(rotation);
    float rcos = cos(rotation);

    uv = mul(uv, mat2(rcos, -rsin, rsin, rcos));
    uv.x += offset;
    uv += 0.5;

    return uv * scale;
}

vec4 glintBlend(const vec4 dest, const vec4 source) {
    return vec4(source.rgb * source.rgb, abs(source.a)) + vec4(dest.rgb, 0.0);
}

vec4 applyGlint(const vec4 diffuse, const vec4 layerUV, const sampler2D glintTexture, const vec4 glintColor, const vec4 tileLightColor) {
    vec4 tex1 = texture2D(glintTexture, fract(layerUV.xy)).rgbr * glintColor;
    vec4 tex2 = texture2D(glintTexture, fract(layerUV.zw)).rgbr * glintColor;

    vec4 glint = (tex1 + tex2) * tileLightColor;
    glint = glintBlend(diffuse, glint);

    return glint;
}

#endif // GLINT_UTIL_H_HEADER_GUARD