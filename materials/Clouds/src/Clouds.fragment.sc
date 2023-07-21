$input v_color0, v_color1, v_color2

#include <bgfx_shader.sh>
#include <newb_legacy.sh>
#include <newb_x.sh>

void main() {
#ifdef TRANSPARENT
	vec4 color;
	vec3 v_dir = normalize(v_color0.xyz);

	color = render_clouds(v_dir, v_color0.xyz, v_color1.a, v_color2.a, v_color2.rgb, v_color1.rgb);
	color.a *= v_color0.a;

	color.rgb = colorCorrection(color.rgb);
	gl_FragColor = color;
#else
	gl_FragColor = vec4(0.0,0.0,0.0,0.0);
#endif
}
