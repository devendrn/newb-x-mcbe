#ifndef NEWB_X_H
#define NEWB_X_H

#define N_CLOUDS_STEPS 10
#define N_CLOUD_HEIGHT 15.0
#define N_CLOUD_SCALE 0.04
#define N_CLOUD_DENSITY 5.0
#define N_CLOUD_VELOCIY 0.8

float randtr(vec2 n, vec2 t) {
	return smoothstep(t.x, t.y, rand(n));
}

// apply bevel with radius r at at corner (1.0)
float bevel(float x, float r) {
	float y = max(x-r,0.0)/(1.0-r);
	return (1.0-r)*(1.0-sqrt(1.0-y*y));
}

float cloud_sdf(vec3 pos, float rain) {
	vec2 p0 = floor(pos.xz);
	vec2 u = smoothstep(0.6,1.0,pos.xz-p0);
	vec2 v = 1.0 - u;

	// rain transition
	vec2 t = vec2(0.101+0.2*rain, 0.099+0.2*rain*rain);

	// mix noise gradients
	float n = v.y*(randtr(p0,t)*v.x + randtr(p0+vec2(1.0,0.0),t)*u.x) +
			  u.y*(randtr(p0+vec2(0.0,1.0),t)*v.x + randtr(p0+vec2(1.0,1.0),t)*u.x);

	// round y
	float b = 0.5*bevel(2.0*abs(pos.y-0.5), 0.3);
	return smoothstep(b,0.5+b,n);
}

vec4 render_clouds(vec3 v_dir, vec3 v_pos, float rain, float time, vec3 fog_col, vec3 sky_col) {
	// local cloud pos
	vec3 pos = v_pos;
	pos.y = 0.0;
	pos.xz = N_CLOUD_SCALE*(v_pos.xz + vec2(1.0,0.5)*(time*N_CLOUD_VELOCIY));

	// scaled ray offset
	vec3 delta_p;
	delta_p.xz = ((1.0+0.5*rain)*N_CLOUD_SCALE*N_CLOUD_HEIGHT)*v_dir.xz/abs(v_dir.y);
	delta_p.y = 1.0;
	delta_p /= N_CLOUDS_STEPS.0;

	// alpha, gradient, ray depth temp
	vec3 d = vec3(0.0,1.0,1.0);
	for (int i=0; i<N_CLOUDS_STEPS; i++) {
		pos += delta_p;
		float m = cloud_sdf(pos.xyz, rain);

		d.x += m*N_CLOUD_DENSITY*(1.0-d.x)/N_CLOUDS_STEPS.0;
		d.y = mix(d.y, pos.y, d.z);
		d.z *= 1.0 - m;

		if (d.x > 0.99) {
			break;
		}
	}

	if (v_pos.y > 0.0) {
		d.y = 1.0 - d.y;
	}

	d.y = 1.0-0.7*d.y*d.y;

	vec4 col;
	col.rgb = 0.6*sky_col;
	col.rgb += (vec3(0.05,0.08,0.08)+0.8*fog_col)*d.y;
	col.rgb *= 1.0 - 0.5*rain;
	col.a = d.x;
	return col;
}

#endif
