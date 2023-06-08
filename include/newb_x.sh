// obsolete code for newb-x-mcbe

#define N_SHADOW_EDGE 0.876

#define N_SUNLIGHT_INTENSITY 2.95
#define N_TORCH_INTENSITY 1.0

#define N_SHADOW_INTENSITY 0.7

#define N_TORCH_COLOR vec3(1.0, 0.52, 0.18)

vec3 n_tonemap(vec3 col) {
	float w_scale = 0.063;
	col = col * (1.0 + col*w_scale) / (1.0 + col);
	return col;
}

vec3 n_to_linear(vec3 col) {
	return col*col;
}

vec3 n_lighting(vec2 light_uv, float time) {
	vec2 light2_uv = light_uv * light_uv;
	float shadow = light_uv.y > N_SHADOW_EDGE ? 1.0 : 0.0;

	vec3 sky_light = vec3(0.3, 0.5, 1.0);
	sky_light *= 2.5;
	vec3 sun_light = vec3(1.0, 0.8, 0.7);
	sun_light *= N_SUNLIGHT_INTENSITY;
	vec3 torch_light = N_TORCH_COLOR;
	torch_light *= N_TORCH_INTENSITY*light_uv.x / (0.5 - 0.45*light2_uv.x);

	vec3 light = vec3(1.45*(1.0 - light2_uv.x)*(1.0 - light_uv.y));

	light += sky_light;

	light += shadow*sun_light;

	light += torch_light;

	return light;
}

float n_fresnel(float cosine, float r0) {
	float a = 1.0 - cosine;
	float a5 = a*a;
	a5 *= a5*a;
	return r0 + (1.0 - r0)*a5;
}
