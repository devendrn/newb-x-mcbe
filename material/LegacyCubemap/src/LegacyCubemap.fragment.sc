$input v_texcoord0, v_color

#include <bgfx_shader.sh>
#include <newb_legacy.sh>

SAMPLER2D(s_MatTexture, 0);

void main() {
    vec4 color = v_color;
	highp vec3 col = colorCorrection(color.rgb);
    gl_FragColor = vec4(col,clamp(color.a,0.0,1.0));

    //gl_FragColor = texture2D(s_MatTexture, v_texcoord0);
}
