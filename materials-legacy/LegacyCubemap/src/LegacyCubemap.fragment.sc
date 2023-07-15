$input v_texcoord0, v_color

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

SAMPLER2D(s_MatTexture, 0);

void main() {

    vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);
    vec4 color = vec4(colorCorrection(v_color.rgb), clamp(v_color.a, 0.0, 1.0));

    diffuse = mix(color, diffuse, diffuse.a);

    gl_FragColor = diffuse;
}
