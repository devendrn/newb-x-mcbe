#ifndef NEWB_X_H
#define NEWB_X_H

const float cloud_height = 18.0;
const float cloud_scale_uv = 0.03;

float randt(vec2 n, vec2 t) {
	return smoothstep(t.x, t.y, rand(n));
}

float cloud_sdf(vec3 pos, float rain) {
	vec2 p0 = floor(pos.xz);
	vec2 u = pos.xz - p0;

	u = smoothstep(0.7,0.95,u);
	vec2 v = 1.0 - u;

	// rain transition
	vec2 t = vec2(0.101+0.2*rain, 0.099+0.2*rain*rain);

	// mix noise gradients
	float n = v.y*(randt(p0,t)*v.x + randt(p0+vec2(1.0,0.0),t)*u.x) +
			  u.y*(randt(p0+vec2(0.0,1.0),t)*v.x + randt(p0+vec2(1.0,1.0),t)*u.x);

	//n *= smoothstep(0.53,0.44,abs(pos.y-0.5));
	return n;
}

vec2 trace_cloud(vec3 v_dir, vec3 pos, float rain, float t) {

	// local cloud pos
	pos.y = 0.0;
	pos.xz *= cloud_scale_uv;
	pos.xz += vec2(0.01,0.02)*t;

	// 3 samples at top
	int max_steps = 7 - int(4.0*abs(v_dir.y));
	float step_size = 1.0/float(max_steps);
	float density = 8.0*step_size;

	// scaled ray offset
	vec3 delta_p;
	delta_p.xz = ((1.0+0.5*rain)*cloud_scale_uv*cloud_height*step_size)*v_dir.xz/abs(v_dir.y);
	delta_p.y = step_size;

	// alpha, gradient, ray depth temp
	vec3 temp = vec3(0.0,0.0,step_size);

	for (int i=0; i<max_steps; i++) {
		pos += delta_p;
		float m = cloud_sdf(pos, rain);

		temp.x += m*density;
		temp.y += temp.z*pos.y;
		temp.z *= 1.0 - m;

		if (temp.x > 0.99) {
			break;
		}
	}

	temp.y = min(temp.y,1.0);
	return temp.xy;
}

vec4 render_clouds(vec3 v_dir, vec3 pos, float rain, float time, vec3 fog_col, vec3 sky_col) {
	vec2 d = trace_cloud(v_dir, v_pos, rain, time);

	d.y = 1.0-0.7*pow(1.0-d.y,8.0);

	vec4 col;
	col.rgb += 0.6*sky_col;
	col.rgb += (vec3(0.05,0.08,0.08)+0.8*fog_col)*d.y;
	col.a = d.x;

	return col;
}

#endif
