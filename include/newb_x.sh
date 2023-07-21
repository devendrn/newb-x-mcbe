#ifndef NEWB_X_H
#define NEWB_X_H

#define N_CLOUDS_STEPS 10

const float cloud_height = 14.0;
const float cloud_scale_uv = 0.04;
const float cloud_step_size = 1.0/float(N_CLOUDS_STEPS);
const float cloud_density = 5.0*cloud_step_size;

float randtr(vec2 n, vec2 t) {
	return smoothstep(t.x, t.y, rand(n));
}

float bevel(float x, float r) {
	float y = max(x-r,0.0)/(1.0-r);
	return (1.0-r)*(1.0-sqrt(1.0-y*y));
}

float cloud_sdf(vec3 pos, float rain) {
	vec2 p0 = floor(pos.xz);
	vec2 u = pos.xz - p0;

	u = smoothstep(0.6,1.0,u);
	vec2 v = 1.0 - u;

	// rain transition
	vec2 t = vec2(0.101+0.2*rain, 0.099+0.2*rain*rain);

	// mix noise gradients
	float n = v.y*(randtr(p0,t)*v.x + randtr(p0+vec2(1.0,0.0),t)*u.x) +
			  u.y*(randtr(p0+vec2(0.0,1.0),t)*v.x + randtr(p0+vec2(1.0,1.0),t)*u.x);

	// round y
	float b = 0.5*bevel(2.0*abs(pos.y-0.5), 0.3);
	n = smoothstep(b,0.5+b,n);
	return n;
}

vec2 trace_cloud(vec3 v_dir, vec3 pos, float rain, float t) {

	// local cloud pos
	pos.y = 0.0;
	pos.xz *= cloud_scale_uv;
	pos.xz += vec2(0.01,0.02)*t;

	// scaled ray offset
	vec3 delta_p;
	delta_p.xz = ((1.0+0.5*rain)*cloud_scale_uv*cloud_height*cloud_step_size)*v_dir.xz/abs(v_dir.y);
	delta_p.y = cloud_step_size;

	// alpha, gradient, ray depth temp
	vec3 temp = vec3(0.0,1.0,1.0);

	for (int i=0; i<N_CLOUDS_STEPS; i++) {
		pos += delta_p;
		float m = cloud_sdf(pos, rain);

		temp.x += m*cloud_density*(1.0-temp.x);
		temp.y = mix(temp.y, pos.y, temp.z);
		temp.z *= 1.0 - m;

		if (temp.x > 0.99) {
			break;
		}
	}
	return temp.xy;
}

vec4 render_clouds(vec3 v_dir, vec3 v_pos, float rain, float time, vec3 fog_col, vec3 sky_col) {
	vec2 d = trace_cloud(v_dir, v_pos, rain, time);

	if(v_pos.y>0.0) {
		d.y = 1.0 - d.y;
	}
	d.y = 1.0-0.7*d.y*d.y;

	vec4 col;
	col.rgb += 0.6*sky_col;
	col.rgb += (vec3(0.05,0.08,0.08)+0.8*fog_col)*d.y;
	col.rgb *= 1.0 - 0.5*rain;
	col.a = d.x;

	return col;
}

#endif
