$input v_texcoord0, CubePos, WorldPos, FOG_COLOR, FOGC, TIME

#include <bgfx_shader.sh>

SAMPLER2D(s_MatTexture, 0);

#define hp highp

#define PI 3.141592
#define TAU PI*2.0
#define ROT2D(a) mat2(cos(a),sin(a),cos(a),-sin(a))

#define rayleighCoeff (vec3(0.27,0.358,0.54) * 1e-5)	//Not really correct
#define mieCoeff vec3(0.5e-6,0.5e-6,0.5e-6)						//Not really correct

#define sunBrightness 3.0

#define d0(x) (abs(x) + 1e-8)
#define d02(x) (abs(x) + 1e-3)

#define totalCoeff rayleighCoeff + mieCoeff

/*
 This code below is from Origin Shader by linlin.
 https://github.com/origin0110/OriginShader
*/

highp float getTimeFromFog(const vec4 fogCol) {
	return fogCol.g > 0.213101 ? 1.0 : 
    dot(vec4(fogCol.g * fogCol.g * fogCol.g, fogCol.g * fogCol.g, fogCol.g, 1.0), 
	vec4(349.305545, -159.858192, 30.557216, -1.628452));
}

#define fogTime getTimeFromFog(FOG_COLOR)
#define DuskTime smoothstep(0.98,0.8,fogTime)
#define NightTime smoothstep(0.15,-0.1,fogTime)

//Suggested by Paixão.
//Thanks for the help.
hp float bayer2(hp vec2 a){
    a = floor(a);
    return fract( dot(a, vec2(.5, a.y * .75)) );
}

#define bayer4(a)   (bayer2( .5*(a))*.25+bayer2(a))
#define bayer8(a)   (bayer4( .5*(a))*.25+bayer2(a))
#define bayer16(a)  (bayer8( .5*(a))*.25+bayer2(a))
#define bayer32(a)  (bayer16(.5*(a))*.25+bayer2(a))
#define bayer64(a)  (bayer32(.5*(a))*.25+bayer2(a))
#define bayer128(a) (bayer64(.5*(a))*.25+bayer2(a))

float cloudHeight(){
     float height = 4200.0;
     return height;
}

float cloudThickness(){
    float thickness = 4050.0;
    return thickness;
}

vec3 saturation(vec3 rgb, float adjustment)
{
    // Algorithm from Chapter 16 of OpenGL Shading Language
    const vec3 W = vec3(0.2125, 0.7154, 0.0721);
    vec3 intensity = vec3(dot(rgb, W),dot(rgb, W),dot(rgb, W));
    return mix(intensity, rgb, adjustment);
}

vec3 HDR_Filter(vec3 color){
    vec3 hdrColor = vec3(1.0,1.0,1.0);    
    vec3 result = vec3(2.2,2.2,2.2) - exp(-hdrColor);
    result = result / (result + 1.0);
    result = pow(result, vec3(1.0 / 2.20,1.0 / 2.20,1.0 / 2.20));
    
    color = pow(abs(color), vec3(1.0/result));
    return color;
}

hp vec3 Unity_WhiteBalance_float(hp vec3 In,hp float Temperature,hp float Tint,hp vec3 Out)
{
    // Range ~[-1.67;1.67] works best
    hp float t1 = Temperature * float(10) / float(6);
    hp float t2 = Tint * float(10) / float(6);

    // Get the CIE xy chromaticity of the reference white point.
    // Note: 0.31271 = x value on the D65 white point
    hp float x = 0.31271 - t1 * (t1 < 0.0 ? 0.1 : 0.05);
    hp float standardIlluminantY = 2.87 * x - float(3) * x * x - 0.27509507;
    hp float y = standardIlluminantY + t2 * 0.05;

    // Calculate the coefficients in the LMS space.
    hp vec3 w1 = vec3(0.949237, 1.03542, 1.08728); // D65 white point

    // CIExyToLMS
    hp float Y = float(1);
    hp float X = Y * x / y;
    hp float Z = Y * (float(1) - x - y) / y;
    hp float L = 0.7328 * X + 0.4296 * Y - 0.1624 * Z;
    hp float M = -0.7036 * X + 1.6975 * Y + 0.0061 * Z;
    hp float S = 0.0030 * X + 0.0136 * Y + 0.9834 * Z;
    hp vec3 w2 = vec3(L, M, S);

    hp vec3 balance = vec3(w1.x / w2.x, w1.y / w2.y, w1.z / w2.z);

    hp mat3 LIN_2_LMS_MAT = mat3(
        3.90405e-1, 5.49941e-1, 8.92632e-3,
        7.08416e-2, 9.63172e-1, 1.35775e-3,
        2.31082e-2, 1.28021e-1, 9.36245e-1
    );

    hp mat3 LMS_2_LIN_MAT = mat3(
        2.85847e+0, -1.62879e+0, -2.48910e-2,
        -2.10182e-1,  1.15820e+0,  3.24281e-4,
        -4.18120e-2, -1.18169e-1,  1.06867e+0
    );

    hp vec3 lms = mul(In, LIN_2_LMS_MAT);
    lms *= balance;
    Out = mul(lms,LMS_2_LIN_MAT);
    
    return Out;
}

vec3 TonemapUncharted(vec3 color){
float a = 1.51;
float b = 0.03;
float c = 2.43;
float d = 0.59;
float e = 0.14;
color = (color*(a*color+b))/(color*(c*color+d)+e);
	
color = saturation(color,1.6);
color = Unity_WhiteBalance_float(color , 0.0380, -0.00250, color);
color = HDR_Filter(color);
 color *= 1.30;
return pow((color / 3.0), vec3(0.82,0.82,0.82)) * 3.0;
}

hp float DrawSun(hp vec3 p) {
return inversesqrt(p.x*p.x + p.y*p.y + p.z*p.z);
}

hp vec3 scatter(hp vec3 coeff,hp float depth){
	return coeff * depth;
}

hp vec3 absorb(hp vec3 coeff,hp float depth){
	return exp2(scatter(coeff, -depth));
}

hp float calcParticleThickness(hp float depth){
   	
    depth = depth * 2.0;
    depth = max(depth + 0.01, 0.01);
    depth = 1.0 / depth;
    
	return 100000.0 * depth;   
}

hp float calcParticleThicknessConst(const hp float depth){
    
	return 100000.0 / max(depth * 2.0 - 0.01, 0.01);   
}

hp float rayleighPhase(hp float x){
	return 0.375 * (1.0 + x*x);
}

hp float hgPhase(hp float x,hp float g)
{
    hp float g2 = g*g;
	return 0.25 * (1.0-max(g*g,0.0) * pow(abs(1.0 + max(g,g) - 2.0*g*x), -1.5));
}

hp float miePhaseSky(hp float x,hp float depth)
{
 	return hgPhase(x, exp2(-0.000003 * depth));
}

hp float powder(hp float od)
{
	return 1.0 - exp2(-od * 2.0);
}

hp float calculateScatterIntergral(hp float opticalDepth,hp float coeff){
    hp float a = -coeff * 1.0 / log(2.0);
    hp float b = -1.0 / coeff;
    hp float c =  1.0 / coeff;

    return exp2(a * opticalDepth) * b + c;
}

hp vec3 calculateScatterIntergral(hp float opticalDepth,hp vec3 coeff){
    hp vec3 a = -coeff * 1.0 / log(2.0);
    hp vec3 b = -1.0 / coeff;
    hp vec3 c =  1.0 / coeff;

    return exp2(a * opticalDepth) * b + c;
}

hp vec3 calcAtmosphericScatter(hp vec3 SunDIR,hp vec3 pos, out hp vec3 absorbLight){
    hp float ln2 = log(2.0);
    
    hp float lDotW = dot(SunDIR, pos);
    hp float lDotU = dot(SunDIR, vec3(0.0, 1.0, 0.0));
    hp float uDotW = dot(vec3(0.0, 1.0, 0.0), pos);
    
    hp float opticalDepth = calcParticleThickness(uDotW);
    hp float opticalDepthLight = calcParticleThickness(lDotU);
    
    hp vec3 scatterView = scatter(totalCoeff, opticalDepth);
    hp vec3 absorbView = absorb(totalCoeff, opticalDepth);
    
    hp vec3 scatterLight = scatter(totalCoeff, opticalDepthLight);
         absorbLight = absorb(totalCoeff, opticalDepthLight);
    	 
    hp vec3 absorbSun = abs(absorbLight - absorbView) / d0((scatterLight - scatterView) * ln2);
    
    hp vec3 mieScatter = scatter(mieCoeff, opticalDepth) * miePhaseSky(lDotW, opticalDepth);
    hp vec3 rayleighScatter = scatter(rayleighCoeff, opticalDepth) * rayleighPhase(lDotW);
    
    hp vec3 scatterSun = mieScatter + rayleighScatter;
    
    hp vec3 sunSpot = smoothstep(0.9999, 0.99993, lDotW) * absorbView * sunBrightness;
    
    return mix((scatterSun * absorbSun + sunSpot) * sunBrightness,vec3(0.28,0.34,0.4)*1.29 ,smoothstep(0.0,0.09,-pos.y));
}

highp float hash2D( highp vec2 n ){
    return fract(sin(dot(n,vec2(12.9898,78.233)))*43758.5453);
}

highp float noise2D( highp vec2 p){
     highp vec2 i = floor(p);
     highp vec2 f = fract(p);
     
     f = f*f*(3.0-2.0*f);
     
     highp float r = mix(mix(hash2D(i+vec2(0.0,0.0)),hash2D(i+vec2(1.0,0.0)),f.x),mix(hash2D(i+vec2(0.0,1.0)),hash2D(i+vec2(1.0,1.0)),f.x),f.y);
    
    return r;
}

float hash3D( vec3 n ){
    return fract(sin(dot(n,vec3(12.9898,78.233,48.798)))*43758.5453);
}

float noise3D(vec3 x){
    vec3 i = floor(x),
    f = fract(x);

    f = f*f*(3.0-2.0*f);

    return mix(mix(mix(hash3D(i+vec3(0,0,0)),
    hash3D(i+vec3(1,0,0)),f.x),
    mix(hash3D(i+vec3(0,1,0)),
    hash3D(i+vec3(1,1,0)),f.x),f.y),
    mix(mix(hash3D(i+vec3(0,0,1)),
    hash3D(i+vec3(1,0,1)),f.x),
    mix(hash3D(i+vec3(0,1,1)),
    hash3D(i+vec3(1,1,1)),f.x),f.y),f.z);
}

highp vec3 nrand3(vec2 co)
{
	highp vec3 a = fract(cos(co.x*8.3e-3 + co.y) * vec3(1.3e5, 4.7e5, 2.9e5));
	highp vec3 b = fract(sin(co.x*0.3e-3 + co.y) * vec3(8.1e5, 1.0e5, 0.1e5));
	highp vec3 c = mix(a, b, 0.25);
	return c;
}

highp vec3 starLayer(highp vec2 p,highp float time)
{
	highp vec2 seed = 1025.0 * p.xy + 0.2;
	highp vec3 rnd = nrand3(floor(seed));
	highp vec3 col = vec3(pow(rnd.y, 30.0));
	highp float mx =  10.0 * rnd.x;
	col.xyz *= sin(time * mx + mx) * 0.25 + 1.0;
	return col;
}

highp float fbm(highp vec3 p,highp float r,int x, float TIME)
{
p *= 7.0;
highp float thickness = 0.5;
highp float alpha = 0.035;
highp float time = TIME;
highp float z = 3.8;
highp float rz = 0.0;
p *= 15.0;
for(int i = 0; i < x; i++)
{
p /= 1.023*1.06;
rz += smoothstep(hash2D(round(p.xz+time*0.025)),0.2,1.0)*r/z;
}
highp float ppl = 4.0 * pow(abs(rz), 2.0);
ppl *= 2.0;
highp float clouds = ppl - (1.0 - thickness);
if( clouds < 0.0 )
{ clouds = 0.0; }
ppl = 1.0 - (pow(alpha, clouds));
return ppl;
}

highp float hgPhase2(highp float cosTheta, highp float g){
highp float g2 = g*g;
return 0.25*(1.0/PI)*(1.0-g2)*pow(1.0+g2-2.0*g*cosTheta, -1.5);
}

highp float twoLobePhase(highp float cosTheta) {
const highp float a = 0.8;
highp float phase1 = hgPhase2(cosTheta, -0.5 * a);
highp float phase2 = hgPhase2(cosTheta, 0.8 * a);
return mix(phase1, phase2, a);
}

highp float remap(float value, float originalMin, float originalMax, float newMin, float newMax) {
highp float t = (value - originalMin) / (originalMax - originalMin);
return t * (newMax - newMin) + newMin;
}

highp float calcCloudOD(highp vec3 rp, float TIME) {
highp float height = rp.y - cloudHeight();
highp float nheight = height * (1.0 / cloudThickness());
highp float aheight = clamp(mix(remap(nheight, 0.0, 0.4, 0.0, 1.0), remap(nheight, 0.6, 1.0, 1.0, 0.0), step(0.4, nheight)), 0.0, 1.0);
rp *= 0.000002 / (1.023 * 1.06);
highp float fbmValue = fbm(rp, 1.0, 5, TIME);
highp float cloudFactor = (0.5 / 3.0) / 4.0;
highp float result = clamp(((fbmValue + cloudFactor) * aheight) * 2.0 - (0.6 * aheight + nheight * 0.5 + 0.3), 0.0, 1.0) * 0.0389;
return result;
}

highp float calcSI(highp float stept,highp float coeff){
highp float a = -1.0/coeff;
return stept*a-a;
}

highp float calcSA(highp vec3 rp, vec3 dir, float TIME) {
highp float od = 0.0;
highp float rayLength = cloudThickness() * (1.0 / 10.0);
highp float invLog2 = 1.0 / log(2.0);
highp vec3 increment = dir.xyz * rayLength;
for (int i = 0; i < 4; i++) {
od += calcCloudOD(rp, TIME);
rp += increment;
}
return od * rayLength * invLog2 * 0.78;
}

void doScattering(highp vec3 p, vec3 path, float tmt, float stm, float od, highp vec3 wp, float dir, inout highp float sunLS, float TIME, vec2 FOGC, vec4 FOG_COLOR) {
highp float sunAD = calcSA(p, path.xyz, TIME);
highp float intergal = calcSI(stm, 0.78);
highp float cloudShadowThicknessPow = 1.0;
highp float cloudSunrayThicknessPow = 1.0;
highp float cloudBrightnessPow = 1.0;
highp float sunLA = 0.0;
highp float phase = 0.0;
for (int i = 0; i < 5; i++) {
sunLA = exp2(-sunAD * cloudShadowThicknessPow);
phase = twoLobePhase(dir * cloudSunrayThicknessPow);
sunLS += phase * sunLA * intergal * cloudBrightnessPow * tmt;
cloudShadowThicknessPow *= mix(0.45, 0.42, smoothstep(0.5,0.28,FOGC.x));
cloudSunrayThicknessPow *= mix(0.69, 0.725, smoothstep(0.5,0.28,FOGC.x));
cloudBrightnessPow *= mix(mix(2.10,2.05,DuskTime),1.74,NightTime);
}
}

highp vec4 calcVolumetricClouds(highp float e, highp vec4 albedo, highp vec3 path, highp vec3 cloudsMixed, highp vec3 cloudsShadowMix, highp vec3 wp, highp float dir, float dither, float TIME, vec2 FOGC, vec4 FOG_COLOR) {
int volumetricSteps = 6;
highp float dist = cloudHeight() / wp.y;
highp vec3 increment = (((cloudThickness() + cloudHeight()) / wp.y) - dist) * wp * (1.0 / float(volumetricSteps));
highp float tmt = 1.0, sunLS = 0.0, cc = 0.0, phase = twoLobePhase(dir);
highp vec3 rp = increment * dither + dist * wp;
for (int i = 0; i < volumetricSteps; i++, rp += increment) {
highp float od = calcCloudOD(rp, TIME) * length(increment);
if (od <= 0.0) continue;
highp float stm = exp2(-od * (1.0 / log(2.0)) * 0.78);
doScattering(rp, path, tmt, stm, od, wp, dir, sunLS, TIME, FOGC, FOG_COLOR);
wp /= 1.023 * 1.06;
tmt *= stm;
cc += od;
}
vec4 colour = mix(vec4(cloudsShadowMix,1.0), vec4(cloudsMixed, 1.0), sunLS);
return mix(albedo, albedo * tmt + colour, clamp(cc, 0.0, 1.0) * e);
}

void main() {

    vec4 diffuse;
    highp vec3 CubetoSphereConv = normalize(vec3(CubePos.x,-CubePos.y + 0.128,-CubePos.z));
    highp vec3 SpherePos = vec3(CubetoSphereConv.x,-CubetoSphereConv.y,-CubetoSphereConv.z);
  
    highp vec3 SphereWorldPos = normalize(vec3(WorldPos.x,WorldPos.y+0.128,WorldPos.z));
    bool MenuDetect = FOGC.x==0.0;
if(!MenuDetect){
    highp float SunTIME;
    SunTIME = fogTime;

    highp vec3 Dir = normalize(vec3(0.5,1.0,0.5));
    highp vec3 Rotate = vec3(cos(mod(SunTIME,PI)),sin(mod(SunTIME,PI)),cos(mod(SunTIME,PI)));
    highp vec3 mpos = SpherePos/SpherePos.y;

    diffuse.rgb = vec3(0.0,0.0,0.0);
    
    hp float Dither = bayer16(gl_FragCoord.xy);
    
    highp vec3 lightAbsorb = vec3(0.0,0.0,0.0);
    highp vec3 Atmosphere = calcAtmosphericScatter(normalize(Dir * Rotate * 3.0), SpherePos, lightAbsorb)*mix(1.0,0.05,NightTime);
    if(NightTime > 0.2){
    Atmosphere += starLayer(SpherePos.xz, TIME)*1.3*smoothstep(0.0,0.8,SpherePos.y);
    }

    highp float Sun = DrawSun(cross(mpos*6.0,normalize(Dir * Rotate * 3.0)));
  
    vec3 SunColour = mix(vec3(1.0,0.95,0.9)*Sun,vec3(1.0,0.93,0.69)*Sun,DuskTime);
    SunColour = mix(SunColour,vec3(0.76,0.74,0.68)*Sun,NightTime);

    vec4 CloudColour = mix(vec4(0.9,0.9,1.0,1.0)*1.10,vec4(1.0,0.93,0.69,1.0)*1.10, DuskTime);
    CloudColour = mix(CloudColour,vec4(saturation(vec3(0.18,0.26,0.35)*0.819,0.18),1.0)*0.91,NightTime);
   
    vec4 CloudsShdColour = mix(vec4(vec3(0.27,0.449 ,0.59),0.8)*0.52,vec4(vec3(0.27,0.449 ,0.59),0.8)*0.52, DuskTime);
    CloudsShdColour = mix(CloudsShdColour, vec4(saturation(vec3(0.18,0.26,0.35)*0.719,0.18),1.0)*0.32,NightTime);
    
    highp vec4 Clouds = calcVolumetricClouds(smoothstep(0.0, 0.35, SpherePos.y), vec4(Atmosphere + Sun, 1.0), normalize(Dir * Rotate * 3.0), CloudColour.rgb, CloudsShdColour.rgb, SpherePos, fogTime, Dither, TIME, FOGC, FOG_COLOR);
    Clouds.rgb = TonemapUncharted(Clouds.rgb);
    gl_FragColor = vec4(Clouds.rgb,1.0);
    }else{
    gl_FragColor = texture2D(s_MatTexture, v_texcoord0);
    }
}
