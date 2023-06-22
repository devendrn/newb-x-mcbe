//// Legacy code ported from newb-shader-mcbe
//// !! depreciated !!

//precision lowp float;

// values
// (toggle options can be commented to disable)

#define NL_CONSTRAST 0.74

// 1 - Exponential
// 2 - Simple Reinhard
// 3 - Extended Reinhard
// 4 - ACES
#define NL_TONEMAP_TYPE 3

// (toggle)
//#define NL_EXPOSURE 1.3
//#define NL_SATURATION 1.4
//#define NL_TINT vec3(1.0,0.75,0.5)


// terrain lighting
#define NL_SUN_INTENSITY 2.95
#define NL_TORCH_INTENSITY 1.0
#define NL_NIGHT_BRIGHTNESS 0.1
#define NL_CAVE_BRIGHTNESS 0.1
#define NL_SHADOW_INTENSITY 0.7

// fog
#define NL_MIST_DENSITY 0.18

// 0 - Off
// 1 - Vanilla fog
// 2 - Smoother vanilla fog
#define NL_FOG_TYPE 2


// top light color (sunlight/moonlight)
#define NL_MORNING_SUN_COL vec3(1.0,0.45,0.14)
#define NL_NOON_SUN_COL vec3(1.0,0.75,0.57)
#define NL_NIGHT_SUN_COL vec3(0.5,0.64,1.0)

// blinking torch light (toggle)
//#define NL_BLINKING_TORCH

// torch colors
#define NL_OVERWORLD_TORCH_COL vec3(1.0,0.52,0.18)
#define NL_UNDERWATER_TORCH_COL vec3(1.0,0.52,0.18)
#define NL_NETHER_TORCH_COL vec3(1.0,0.52,0.18)
#define NL_END_TORCH_COL vec3(1.0,0.52,0.18)

// sky/fog colors
#define NL_NIGHT_SKY_COL vec3(0.01,0.06,0.1)
#define NL_BASE_SKY_COL vec3(0.15,0.45,1.0)
#define NL_DAY_SKY_CLARITY 0.3
#define NL_BASE_HORIZON_COL vec3(1.0,0.4,0.3)
#define NL_EDGE_HORIZON_COL vec3(1.0,0.4,0.2)
#define NL_BASE_UNDERWATER_COL vec3(0.0,0.6,1.0)

// ore glow intensity
#define NL_GLOW_TEX 1.7

// plants wave intensity (toggle)
#define NL_PLANTS_WAVE 0.04

#define NL_WAVE_SPEED 2.8

// water
#define NL_WATER_TRANSPARENCY 0.47
#define NL_WATER_BUMP 0.07
#define NL_SEA_WATER_COL vec3(0.13,0.65,0.87)
#define NL_FRESH_WATER_COL vec3(0.07,0.55,0.55)
#define NL_MARSHY_WATER_COL vec3(0.27,0.4,0.1)

// water wave (toggle)
#define NL_WATER_WAVE

// use only surface angle for water transparency (gives more transparency)
//#define NL_WATER_ANGLE_BLEND

// water texture overlay
#define NL_WATER_TEX_OPACITY 0.0

// underwater lighting
#define NL_UNDERWATER_BRIGHTNESS 0.8
#define NL_UNDERWATER_COL vec3(0.2,0.6,1.0)
#define NL_CAUSTIC_INTENSITY 1.9

// lantern swing intensity (toggle)
#define NL_LANTERN_WAVE 0.16

// clouds
#define NL_CLOUD_START_RAIN 0.1
#define NL_CLOUD_START_NORMAL 0.73
#define NL_CLOUD_UV_SCALE vec2(0.0194, 0.0278)
#define NL_CLOUD_DEPTH 1.3
#define NL_CLOUD_SPEED 0.04
#define NL_CLOUD_DENSITY 0.54
#define NL_CLOUD_OPACITY 0.8

//️ aurora borealis brightness (toggle)
#define NL_AURORA 1.0

// rainy wind blow transparency (0-0.3) (toggle)
#define NL_RAIN_MIST_OPACITY 0.12

// chunk loading slide in animation (toggle)
//#define NL_CHUNK_LOAD_ANIM 100.0

// unsorted

// Toggle - Underwater Wave
// Value - Wave intensity
//#define UNDERWATER_WAVE 0.06

// Toggle - Cloud reflection on water
//#define CLOUD_REFLECTION


// CONSTANTS
#define NL_CONST_SHADOW_EDGE 0.876
#define NL_CONST_PI_HALF 1.5708
#define NL_CONST_PI_QUART 0.7854


bool detectEnd(vec3 FOG_COLOR) {
	// end is given a custom fog color in biomes_client.json to help in detection
	// dark color (issue- rain transition when entering end)
	return FOG_COLOR.r == FOG_COLOR.b && FOG_COLOR.r > 0.1  && FOG_COLOR.g < FOG_COLOR.r*0.4;
}

bool detectNether(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
	// FOG_CONTROL x and y varies with renderdistance
	// x range (0.03,0.14)

	// reverse plotted relation (5,6,7,8,9,11,12,20,96 chunks data) with an accuracy of 0.02
	float expectedFogX = 0.029 + (0.09*FOG_CONTROL.y*FOG_CONTROL.y);	// accuracy of 0.015

	// nether wastes, basalt delta, crimson forest, wrapped forest, soul sand valley
	bool netherFogCtrl = (FOG_CONTROL.x<0.14  && abs(FOG_CONTROL.x-expectedFogX) < 0.02);
	bool netherFogCol = (FOG_COLOR.r+FOG_COLOR.g)>0.0;

	// consider underlava as nether
	bool underLava = FOG_CONTROL.x == 0.0 && FOG_COLOR.b == 0.0 && FOG_COLOR.g < 0.18 && FOG_COLOR.r-FOG_COLOR.g > 0.1;

	return (netherFogCtrl && netherFogCol) || underLava;
}

bool detectUnderwater(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
	return FOG_CONTROL.x < 0.001 && max(FOG_COLOR.b, FOG_COLOR.g) > FOG_COLOR.r;
}

float detectRain(vec3 FOG_CONTROL) {
	// FOG_CONTROL (x,y) values when clear/rain
	// clear x varies with render distance (z)
	// reverse plotted (low accuracy) as 0.5 + 1.09/(k-0.8) where k is renderdistance in chunks
	// remaining values are equal to those specified in json file

	vec2 start = vec2(0.5 + 1.09/(FOG_CONTROL.z*0.0625 - 0.8), 0.99);
	vec2 end = vec2(0.2305, 0.7005);

	vec2 factor = clamp((start-FOG_CONTROL.xy)/(start-end), vec2(0.0, 0.0),vec2(1.0, 1.0));

	// ease in ease out for Y
	factor.y = factor.y*factor.y*(3.0 - 2.0*factor.y);

	return factor.x*factor.y;
}

// 1D noise - used in plants,lantern wave
highp float noise1D(highp float x) {
	float x0 = floor(x);
	float t0 = x-x0;
	t0 *= t0*(3.0-2.0*t0);
	return mix(fract(sin(x0)*84.85), fract(sin(x0+1.0)*84.85), t0);
}

// hash function for noise (for highp only)
highp float rand(highp vec2 n) {
	return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

// interpolation of noise - used by rainy air blow
// see https://thebookofshaders.com/11/
float noise2D(vec2 p) {
	vec2 p0 = floor(p);
	vec2 u = p-p0;

	u *= u*(3.0-2.0*u);
	vec2 v = 1.0 - u;

	float c1 = rand(p0);
	float c2 = rand(p0+vec2(1.0,0.0));
	float c3 = rand(p0+vec2(0.0,1.0));
	float c4 = rand(p0+vec2(1.0,1.0));

	float n = v.y*(c1*v.x+c2*u.x) + u.y*(c3*v.x+c4*u.x);

	return min(n*n, 1.0);
}


vec4 renderMist(vec3 fog, float dist, float lit, float rain, bool nether, bool underwater, bool end, vec3 FOG_COLOR) {

	float density = NL_MIST_DENSITY;
	vec4 mist;

	if (nether) {
		mist.rgb = FOG_COLOR.rgb;
		mist.rgb = mix(2.6*mist.rgb*mist.rgb,vec3(2.1,0.7,0.2),lit*0.7);
	} else {
		mist.rgb = fog*vec3(1.0,1.1-0.1*rain,1.4-0.4*rain);

		// increase density based on darkness
		density += density*(0.99-FOG_COLOR.g)*18.0;
	}

	// exponential mist
	mist.a = 0.31-0.3*exp(-dist*dist*density);

	if (underwater) {
		mist.rgb = fog;
		mist.a = 0.2+0.5*min(dist*dist,1.0);
	}

	return mist;
}

vec4 renderFog(vec3 fogColor, float len, bool nether, vec3 FOG_COLOR, vec2 FOG_CONTROL) {

#if NL_FOG_TYPE > 0
	vec4 fog;
	if (nether) {
		// inverse color correction
		fog.rgb = pow(FOG_COLOR, vec3_splat(1.37));
		vec3 w = vec3_splat(0.7966);
		fog.rgb = fog.rgb*(w + fog.rgb)/(w + fog.rgb*(vec3_splat(1.0) - w));
	} else {
		fog.rgb = fogColor;
	}

	fog.a = clamp((len-FOG_CONTROL.x)/(FOG_CONTROL.y-FOG_CONTROL.x), 0.0, 1.0);

#if NL_FOG_TYPE > 1
		fog.a = (fog.a*fog.a)*(3.0-2.0*fog.a);
#endif

	return fog;
#else
	return vec4(0.0,0.0,0.0,0.0);
#endif
}

vec3 getUnderwaterCol(vec3 FOG_COLOR) {
	return NL_BASE_UNDERWATER_COL*FOG_COLOR.b;
}

vec3 getEndSkyCol() {
	return vec3(0.57,0.063,0.66)*0.5;
}

vec3 getZenithCol(float rainFactor, vec3 FOG_COLOR) {

	// value needs tweaking
	float val = max(FOG_COLOR.r*0.6, max(FOG_COLOR.g, FOG_COLOR.b));

	// zenith color
	vec3 zenithCol = (0.77*val*val + 0.33*val)*NL_BASE_SKY_COL;
	zenithCol += NL_NIGHT_SKY_COL*(0.4-0.4*FOG_COLOR.b);

	// rain sky
	float brightness = min(FOG_COLOR.g, 0.26);
	brightness *= brightness*13.2;

	zenithCol = mix(zenithCol*(1.0+0.5*rainFactor), vec3(0.85,0.9,1.0)*brightness, rainFactor);

	return zenithCol;
}

vec3 getHorizonCol(float rainFactor, vec3 FOG_COLOR) {
	// value needs tweaking
	float val = max(FOG_COLOR.r*0.65, max(FOG_COLOR.g*1.1, FOG_COLOR.b));

	float sun = max(FOG_COLOR.r-FOG_COLOR.b, 0.0);

	// horizon color
	vec3 horizonCol = NL_BASE_HORIZON_COL*(((0.7*val*val) + (0.4*val) + sun)*2.4);
	horizonCol += NL_NIGHT_SKY_COL;

	horizonCol = mix(
		horizonCol,
		2.0*val*mix(vec3(0.7,1.0,0.9), NL_BASE_SKY_COL, NL_DAY_SKY_CLARITY),
		val*val);

	// rain horizon
	float brightness = min(FOG_COLOR.g, 0.26);
	brightness *= brightness*19.6;
	horizonCol = mix(horizonCol, vec3_splat(brightness), rainFactor);

	return horizonCol;
}

vec3 getHorizonEdgeCol(vec3 horizonCol, float rainFactor, vec3 FOG_COLOR) {
	float val = (1.1-FOG_COLOR.b)*FOG_COLOR.g*2.1;
	val *= 1.0-rainFactor;

	// tinting
    horizonCol *= vec3_splat(1.0)-val*(vec3_splat(1.0)-NL_EDGE_HORIZON_COL);
	return horizonCol;
}

// 1D sky with three color gradient
// A copy of this is in sky.fragment, make changes there aswell
vec3 renderSky(vec3 reddishTint, vec3 horizonColor, vec3 zenithColor, float h) {
	h = 1.0-h*h;

	float hsq = h*h;

	// gradient 1  h^16
	// gradient 2  h^8 mix h^2
	float gradient1 = hsq*hsq*hsq*hsq;
	float gradient2 = 0.6*gradient1 + 0.4*hsq;
	gradient1 *= gradient1;

	horizonColor = mix(horizonColor, reddishTint, gradient1);
	return mix(zenithColor,horizonColor, gradient2);
}

// see https://64.github.io/tonemapping/
#if NL_TONEMAP_TYPE==3
// extended reinhard tonemapping
vec3 tonemap(vec3 x) {
	//float white = 4.0;
	//float white_scale = 1.0/(white*white);
	float white_scale = 0.063;
	x = (x*(1.0+(x*white_scale)))/(1.0+x);
	return x;
}
#elif NL_TONEMAP_TYPE==4
// aces tone mapping
vec3 tonemap(vec3 x) {
	x *= 0.85;
	const float a = 1.04;
	const float b = 0.03;
	const float c = 0.93;
	const float d = 0.56;
	const float e = 0.14;
	return clamp((x*(a*x + b)) / (x*(c*x + d) + e), 0.0, 1.0);
}
#elif NL_TONEMAP_TYPE==2
// simple reinhard tonemapping
vec3 tonemap(vec3 x) {
	return x/(1.0+x);
}
#elif NL_TONEMAP_TYPE==1
// exponential tonemapping
vec3 tonemap(vec3 x) {
	return 1.0-exp(-x*0.8);
}
#endif

vec3 colorCorrection(vec3 color) {
	#ifdef NL_EXPOSURE
		color *= NL_EXPOSURE;
	#endif

	color = tonemap(color);

	// actually supposed to be gamma correction
	color = pow(color, vec3_splat(NL_CONSTRAST));

	#ifdef NL_SATURATION
		color = mix(vec3(dot(color,vec3(0.21, 0.71, 0.08))), color, NL_SATURATION);
	#endif

	#ifdef NL_TINT
		color *= NL_TINT;
	#endif

	return color;
}


// clamp rand for cloud noise
highp float rand01(highp vec2 seed, float start) {
	float result = rand(seed);
	result = clamp((result-start)*3.4, 0.0, 1.0);
	return result*result;
}

// 2D cloud noise - used by clouds
float cloudNoise2D(vec2 p, highp float t, float rain) {

	t *= NL_CLOUD_SPEED;

	// start threshold - for bigger clouds during rain
	float start = NL_CLOUD_START_NORMAL + (1.0 - NL_CLOUD_START_NORMAL)*(0.1+0.1*sin(t + p.y*0.3));
	start = mix(start, NL_CLOUD_START_RAIN, rain);

	p += vec2_splat(t);
	p.x += sin(p.y*0.4 + t);

	vec2 p0 = floor(p);
	vec2 u = p-p0;

	u *= u*(3.0-2.0*u);
	vec2 v = 1.0-u;

	float c1 = rand01(p0, start);
	float c2 = rand01(p0+vec2(1.0,0.0), start);
	float c3 = rand01(p0+vec2(0.0,1.0), start);
	float c4 = rand01(p0+vec2(1.0,1.0), start);

	return v.y*(c1*v.x+c2*u.x) + u.y*(c3*v.x+c4*u.x);
}

// simple cloud
vec4 renderClouds(vec4 color, vec2 uv, highp float t, float rain) {
	uv *= NL_CLOUD_UV_SCALE;

	float cloudAlpha = cloudNoise2D(uv, t, rain);
	float cloudShadow = cloudNoise2D(uv, (t+0.16), rain);

	color.a = max(cloudAlpha-0.2*cloudShadow, 0.0);

	// rainy clouds color
	color.rgb = mix(color.rgb, vec3_splat(0.7), rain*0.5);

	// highlight at edge
	color.rgb += vec3(0.12,0.12,0.2)*(1.0-cloudShadow);

	// cloud shadow
	color.rgb *= (1.0-0.6*cloudShadow*NL_CLOUD_DENSITY);

	return color;
}

// simple northern night sky effect
vec4 renderAurora(vec2 uv, highp float t, float rain) {
	uv *= 0.05;

	float auroraCurves = sin(uv.x*0.09 + 0.07*t) + 0.3*sin(uv.x*0.5 + 0.09*t) + 0.03*sin((uv.x+uv.y)*3.0 + 0.2*t);
	float auroraBase = uv.y*0.4 + 2.0*auroraCurves;
	float auroraFlow = 0.5+0.5*sin(uv.x*0.3 + 0.07*t + 0.7*sin(auroraBase*0.9));

	float auroraCol = sin(uv.y*0.06 + 0.07*t);
	auroraCol = abs(auroraCol*auroraCol*auroraCol);

	float aurora = sin(auroraBase)*sin(auroraBase*0.3);
	aurora = abs(aurora*auroraFlow);

	return vec4(0.0, (1.0-auroraCol)*aurora, auroraCol*aurora, aurora*aurora*(0.5-0.5*rain));
}

// sunlight tinting
vec3 sunLightTint(float dayFactor, float rain, vec3 FOG_COLOR) {

	float tintFactor = FOG_COLOR.g + 0.1*FOG_COLOR.r;
	float noon = clamp((tintFactor-0.37)/0.45,0.0,1.0);
	float morning = clamp((tintFactor-0.05)*3.125,0.0,1.0);

	float r = 1.0-rain;
	r *= r;

	return mix(vec3(0.65,0.65,0.75), mix(
		mix(NL_NIGHT_SUN_COL, NL_MORNING_SUN_COL, morning),
		mix(NL_MORNING_SUN_COL, NL_NOON_SUN_COL, noon),
		dayFactor), r*r);
}

// bool between function
bool is(float val, float val1, float val2) {
	return (val > val1 && val < val2);
}

// water transparency
float getWaterAlpha(vec3 col) {
	// tint - col.r,
	if (col.r < 0.5) {
		return mix(0.9, NL_WATER_TRANSPARENCY, col.r*2.0);
	} else {
		return NL_WATER_TRANSPARENCY;
	}
}

// simpler rand for disp,wetmap
float fastRand(vec2 n) {
	float a = cos(dot(n, vec2(4.2683, 1.367)));
	float b = dot(n, vec2(1.367, 4.683));
	return fract(a+b);
}

// water displacement map (also used by caustic)
float disp(vec3 pos, highp float t) {
	float val = 0.5 + 0.5*sin(t*1.7 + (pos.x+pos.y)*NL_CONST_PI_HALF);
	return mix(fastRand(pos.xz), fastRand(pos.xz+vec2_splat(1.0)), val);
}

// sky reflection on plane - used by water, wet reflection
vec3 getSkyRefl(vec3 horizonEdgeCol, vec3 horizonCol, vec3 zenithCol, float y, float h) {

	// offset the reflection based on height from camera
	float offset = h/(50.0+h); 	// (h*0.02)/(1.0+h*0.02)
	y = max((y-offset)/(1.0-offset), 0.0);

	return renderSky(horizonEdgeCol, horizonCol, zenithCol, y);
}

// simpler sky reflection for rain
vec3 getRainSkyRefl(vec3 horizonCol, vec3 zenithCol, float h) {
	h = 1.0-h*h;
	h *= h; // hsq

	return mix(zenithCol, horizonCol, h*h);
}

// sunrise/sunset reflection
vec3 getSunRefl(float viewDirX, float fog_brightness, vec3 FOG_COLOR) {
	float sunRefl = clamp((abs(viewDirX)-0.9)/0.099,0.0,1.0);
	float factor = FOG_COLOR.r/length(FOG_COLOR);
	factor *= factor;
	sunRefl *= sunRefl*sunRefl*factor*factor;
	sunRefl *= sunRefl;
	return (fog_brightness*sunRefl)*vec3(2.5,1.6,0.8);
}

// fresnel - Schlick's approximation
float calculateFresnel(float cosR, float r0) {
	float a = 1.0-cosR;
	float a5 = a*a;
	a5 *= a5*a;

	return r0 + (1.0-r0)*a5;
}


//// Implementation
// functions are used while porting to wrap previous code easily
// parameters will be simplified later

vec3 nl_lighting(out vec3 torchColor, vec3 COLOR, vec3 FOG_COLOR, float rainFactor, vec2 uv1, vec2 lit, bool isTree,
                 vec3 horizonCol, vec3 zenithCol, float shade, bool end, bool nether, bool underwater) {
    // all of these will be multiplied by tex uv1 in frag so functions should be divided by uv1 here

    vec3 light;

	if (underwater) {
		torchColor = NL_UNDERWATER_TORCH_COL;
	} else if (end) {
		torchColor = NL_END_TORCH_COL;
	} else if (nether) {
		torchColor = NL_NETHER_TORCH_COL;
	} else {
		torchColor = NL_OVERWORLD_TORCH_COL;
	}

    float torch_attenuation = (NL_TORCH_INTENSITY*uv1.x)/(0.5-0.45*lit.x);

#ifdef NL_BLINKING_TORCH
	torch_attenuation *= 1.0 - 0.19*noise1D(t*8.0);
#endif

    vec3 torchLight = torchColor*torch_attenuation;

    if(nether || end){
        // nether & end lighting

        // ambient - end and nether
        light = end ? vec3(1.98,1.25,2.3) : vec3(2.0,1.44,1.26);

		light += horizonCol*0.7;

        // torch light
        light += torchLight*0.5;
    }
    else{
        // overworld lighting

        float dayFactor = min(dot(FOG_COLOR.rgb, vec3(0.5,0.4,0.4))*(1.0 + 1.9*rainFactor), 1.0);
        float nightFactor = 1.0-dayFactor*dayFactor;
        float rainDim = min(FOG_COLOR.g, 0.25)*rainFactor;
        float lightIntensity = NL_SUN_INTENSITY*(1.0 - rainDim)*(1.0 + NL_NIGHT_BRIGHTNESS*nightFactor);

        // min ambient in caves
        light = vec3_splat((1.35+NL_CAVE_BRIGHTNESS)*(1.0-uv1.x)*(1.0-uv1.y));

        // sky ambient
        light += mix(horizonCol,zenithCol,0.5+uv1.y-0.5*lit.y)*(lit.y*(3.0-2.0*uv1.y)*(1.3 + (4.0*nightFactor) - rainDim));

        // shadow cast by top light
        float shadow = float(uv1.y > NL_CONST_SHADOW_EDGE);

        // make shadow a bit softer and more softer when raining
        shadow += uv1.y > 0.85 ? (0.2+0.3*rainFactor)*(1.0-shadow) : 0.0;

        shadow = max(shadow, (1.0 - NL_SHADOW_INTENSITY + (0.6*NL_SHADOW_INTENSITY*nightFactor))*lit.y);
        shadow *= shade>0.8 ? 1.0 : 0.8;

        // direct light from top
        float dirLight = shadow*(1.0-uv1.x*nightFactor)*lightIntensity;
        light += dirLight*sunLightTint(dayFactor, rainFactor, FOG_COLOR);

        // extra indirect light
        light += vec3_splat(0.3*lit.y*uv1.y*(1.2-shadow)*lightIntensity);

        // torch light
        light += torchLight*(1.0-(max(shadow, 0.65*lit.y)*dayFactor*(1.0-0.3*rainFactor)));
    }

    // darken at crevices
    light *= COLOR.g > 0.35 ? 1.0 : 0.8;

    // brighten tree leaves
    if (isTree) {
		light *= 1.25;
	}

    return light;
}

vec4 nl_water(inout vec3 wPos, inout vec4 color, vec3 viewDir, vec3 light, vec3 cPos, float fractCposY, vec4 COLOR, vec3 FOG_COLOR, vec3 horizonCol,
			  vec3 horizonEdgeCol, vec3 zenithCol, vec2 uv1, float t, float camDist,
			  float rainFactor, vec3 tiledCpos, bool end, vec3 torchColor) {

	float cosR;
	float bump = NL_WATER_BUMP;
	vec3 waterRefl;

	// reflection for top plane
	if (fractCposY > 0.0) {

		// calculate cosine of incidence angle and apply water bump
		bump *= disp(tiledCpos, t) + 0.12*sin(t*2.0 + dot(cPos, vec3_splat(NL_CONST_PI_HALF)));

		cosR = abs(viewDir.y);
		cosR = mix(cosR, (1.0-cosR*cosR), bump);

		// sky reflection
		waterRefl = getSkyRefl(horizonEdgeCol, horizonCol, zenithCol, cosR, -wPos.y);
		waterRefl += getSunRefl(viewDir.x,horizonEdgeCol.r, FOG_COLOR);

		// sky,cloud reflection mask
		if (uv1.y < 0.93 && !end) {
			waterRefl *= 0.7*uv1.y;
		}

		// ambient,torch light reflection
		waterRefl += vec3_splat(0.02-0.02*uv1.y);
		waterRefl += torchColor*NL_TORCH_INTENSITY*((uv1.x > 0.83 ? 0.6 : 0.0) + uv1.x*uv1.x*bump*10.0);

		if (is(fractCposY, 0.8, 0.9)) {
			// flat plane
			waterRefl *= 1.0 - 0.66*clamp(wPos.y, 0.0, 1.0);
		} else {
			// slanted plane and highly slanted plane
			waterRefl *= (0.1*sin(t*2.0+cPos.y*12.566)) + (fractCposY > 0.9 ? 0.2 : 0.4);
		}
	}
	// reflection for side plane
	else{
		bump *= 0.5 + 0.5*sin(1.5*t + dot(cPos, vec3_splat(NL_CONST_PI_HALF)));
		cosR = max(sqrt(dot(viewDir.xz, viewDir.xz)), float(wPos.y < 0.5));
		cosR += (1.0-cosR*cosR)*bump;

		waterRefl = zenithCol*uv1.y*uv1.y*1.3;
	}

	float fresnel = calculateFresnel(cosR, 0.03);
	float opacity = 1.0-cosR;

#ifdef NL_WATER_ANGLE_BLEND
	color.a = getWaterAlpha(COLOR.rgb) + opacity*(1.0-color.a);
#else
	color.a = color.a + (1.0-color.a)*opacity*opacity;
#endif

	// get water color (r-tint,g-lightness)
	color.rgb = NL_FRESH_WATER_COL;
	if (COLOR.r < 0.5) {
		color.rgb = mix(NL_MARSHY_WATER_COL, color.rgb, COLOR.r*2.0);
	} else {
		color.rgb = mix(color.rgb, NL_SEA_WATER_COL, 2.0*COLOR.r -1.0);
	}
	color.rgb *= COLOR.g;
	color.rgb *= color.rgb*0.3*(1.0-0.4*fresnel);

#ifdef NL_WATER_WAVE
	if(camDist < 10.0) {
		wPos.y += bump;
	}
#endif

	return vec4(waterRefl, fresnel);
}

void nl_glow(vec4 diffuse, inout vec4 color, inout vec3 light_tint, vec2 uv1) {
	if(diffuse.a>0.9875 && diffuse.a<0.9925 && abs(diffuse.r-diffuse.b)+abs(diffuse.b-diffuse.g)>0.02){
		color.rgb = max(color.rgb, (vec3_splat(NL_GLOW_TEX*(diffuse.a>0.989 ? 0.4 : 1.0)) + 0.6*diffuse.rgb)*(1.0-uv1.y));
		light_tint = vec3_splat(0.2) + 0.8*light_tint;
	}
}

void nl_wave(inout vec3 worldPos, inout vec3 light, float rainFactor, vec2 uv1, vec2 lit,
					 vec2 uv0, vec3 bPos, vec4 COLOR, vec3 cPos, vec3 tiledCpos, float t,
					 bool isColored, float camDist, bool underWater, bool isTreeLeaves) {

	if (camDist < 13.0) {	// only wave nearby

	// texture space - (32x64) textures in uv0.xy
	vec2 texMap = uv0*vec2(32.0, 64.0);
	float texPosY = fract(texMap.y);

	int texNoX = int(texMap.x);
	int texNoY = int(texMap.y);

	// x and z distance from block center
	vec2 bPosC = abs(bPos.xz-0.5);

	bool isTop = texPosY < 0.5;
	bool isPlants = (COLOR.r/COLOR.g<1.9);
	bool isVines = (bPos.y + bPos.x*bPos.z)<0.00005 && is(bPosC.x+bPosC.y,0.94921,0.94922);
	bool isFarmPlant = (bPos.y==0.9375) && (bPosC.x==0.25 ||  bPosC.y==0.25);
	bool shouldWave = ((isTreeLeaves || isPlants || isVines) && isColored) || (isFarmPlant && isTop && !underWater);

	// darken plants bottom - better to not move it elsewhere
	//light *= isFarmPlant && !isTop ? 0.65 : 1.0;
	//if(isColored && !isTreeLeaves && texNoY==12){
	//	light *= isTop ? 1.2 : 1.2 - (bPos.y>0.0 ? 1.5-bPos.y : 0.5);
	//}

	float windStrength = lit.y*(noise1D(t*0.36) + (rainFactor*0.4));

#ifdef NL_PLANTS_WAVE
	if (shouldWave) {

		float wave = NL_PLANTS_WAVE*windStrength;

		wave *= isTreeLeaves ? 0.5 : 1.0;
		wave = isVines ? min(0.024,wave*fract(0.01+tiledCpos.y*0.5)) : wave;

		// wave the bottom of plants in opposite direction to make it look fixed
		if (isPlants && isColored && !(isVines || isTreeLeaves || isTop)) {
			wave *= bPos.y > 0.0 ? bPos.y-1.0 : 0.0 ;
		}

		// values must be a multiple of pi/4
		float phaseDiff = dot(cPos,vec3_splat(NL_CONST_PI_QUART)) + fastRand(tiledCpos.xz + tiledCpos.y);

		wave *= 1.0 + mix(
			sin(t*NL_WAVE_SPEED + phaseDiff),
			sin(t*NL_WAVE_SPEED*1.5 + phaseDiff),
			rainFactor);

		//worldPos.y -= 1.0-sqrt(1.0-wave*wave);
		worldPos.xyz -= vec3(wave, wave*wave*0.5, wave);
	}
#endif

#ifdef NL_LANTERN_WAVE
	bool y6875 = bPos.y==0.6875;
	bool y5625 = bPos.y==0.5625;

	bool isLantern = ( (y6875 || y5625) && bPosC.x==0.125 ) || ( (y5625 || bPos.y==0.125) && (bPosC.x==0.1875) );
	bool isChain = bPosC.x==0.0625 && y6875;

	// fix for non-hanging lanterns waving top part (works only if texPosY is correct)
	if (texPosY < 0.3 || is(texPosY, 0.67, 0.69) || is(texPosY, 0.55, 0.6)) {
		isLantern = isLantern && !y5625;
	}

	// X,Z axis rotation
	if (uv1.x > 0.6 && (isChain || isLantern)) {
		// wave phase diff for individual lanterns
		float offset = dot(floor(cPos), vec3_splat(0.3927));

		// simple random wave for angle
		highp vec2 theta = vec2(t + offset, t*1.4 + offset);
		theta = sin(vec2(theta.x,theta.x+0.7)) + rainFactor*sin(vec2(theta.y,theta.y+0.7));
		theta *= NL_LANTERN_WAVE*windStrength;

		vec2 sinA = sin(theta);
		vec2 cosA = cos(theta);

		vec3 pivotPos = vec3(0.5,1.0,0.5) - bPos;

		worldPos.x += dot(pivotPos.xy, vec2(1.0-cosA.x, -sinA.x));
		worldPos.y += dot(pivotPos, vec3(sinA.x*cosA.y, 1.0-cosA.x*cosA.y, sinA.y));
		worldPos.z += dot(pivotPos, vec3(sinA.x*sinA.y, -cosA.x*sinA.y, 1.0-cosA.y));
	}
#endif
	}
}

void nl_underwater_lighting(inout vec3 light, vec2 lit, vec2 uv1, vec3 tiledCpos, vec3 cPos, float t) {
	// soft caustic effect
	if (uv1.y < 0.9) {
		float caustics = disp(tiledCpos*vec3(1.0,0.1,1.0), t);
		caustics += (1.0 + sin(t + (cPos.x+cPos.z)*NL_CONST_PI_HALF));

		light += NL_UNDERWATER_BRIGHTNESS + NL_CAUSTIC_INTENSITY*caustics*(0.1 + lit.y + lit.x*0.7);

		light *= mix(NL_UNDERWATER_COL, vec3(1.0,1.0,1.0), lit.y*0.7);
	}
}

vec4 nl_refl(inout vec4 color, inout vec4 mistColor, vec2 lit, vec2 uv1, vec3 tiledCpos,
			 float camDist, vec3 wPos, vec3 viewDir, vec3 torchColor, vec3 horizonCol,
			 vec3 zenithCol, float rainFactor, float render_dist, float t, vec3 pos) {
	vec4 wetRefl = vec4_splat(0.0);
	if (rainFactor > 0.0) {
		float wetness = lit.y*lit.y*rainFactor;

#ifdef NL_RAIN_MIST_OPACITY
		// humid air blow
		float humidAir = max(noise2D((pos.xy*vec2(1.5,1.0)/(1.0+pos.z)) + (t*vec2(1.4,0.7)) ), 0.0);
		humidAir *= wetness;
		mistColor.a = min(mistColor.a + humidAir*NL_RAIN_MIST_OPACITY, 1.0);
#endif

		// wet effect
		float endDist = render_dist*0.6;

		if (camDist < endDist) {

			// puddles map
			wetness *= 1.25*min(0.4+0.6*fastRand(tiledCpos.xz*1.4), 1.0);

			// incidence angle
			float cosR = max(viewDir.y,float(wPos.y > 0.0));

			wetRefl.rgb = getRainSkyRefl(horizonCol, zenithCol, cosR);
			wetRefl.a = calculateFresnel(cosR, 0.03)*wetness;

			// torch light
			wetRefl.rgb += torchColor*lit.x*NL_TORCH_INTENSITY;

			// hide effect far from player
			wetRefl.a *= clamp(2.0-(2.0*camDist/endDist), 0.0, 0.9);
		}

		// darken wet parts
		color.rgb *= 1.0 - 0.4*wetness;
	}
	return wetRefl;
}
