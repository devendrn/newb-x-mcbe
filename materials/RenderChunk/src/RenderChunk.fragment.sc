$input v_color0, v_fog, v_texcoord0, v_lightmapUV, ChunkPos, WorldPos, ViewPos, ScreenPos, FOG_COLOR, FOGC, TIME, RENDER_CHUNKS

#include <bgfx_shader.sh>

uniform vec4 FogColor;

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_SeasonsTexture, 1);
SAMPLER2D(s_LightMapTexture, 2);

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

//Don't exceed more than 0.940.
//Minimum value should always be less than maximum one.
#define WATER_ABSORPTION_MIN 0.520
#define WATER_ABSORPTION_MAX 0.872

//These values works in opposite manner the more you increase any value the lesser it will get.
//To increase any value reduce it.
#define WATER_ABSORPTION_R  7
#define WATER_ABSORPTION_G 5
#define WATER_ABSORPTION_B 3
#define WATER_ABSORPTION_M 1.50 
#define WATER_ABSORPTION_ATTN 3.20
#define WATER_ABSORPTION_INTENSITY 1e-2 //1e-2 in decimal means 0.01

#define WATER_EXT vec3(WATER_ABSORPTION_R, WATER_ABSORPTION_G, WATER_ABSORPTION_B) * WATER_ABSORPTION_INTENSITY
#define WATER_SCAT vec3(WATER_ABSORPTION_M,WATER_ABSORPTION_M,WATER_ABSORPTION_M) * WATER_ABSORPTION_INTENSITY
#define WATER_ABSORPTION (WATER_EXT + WATER_SCAT) * WATER_ABSORPTION_ATTN

/*
 This code below is from Origin Shader by linlin.
 https://github.com/origin0110/OriginShader
*/

highp float getTimeFromFog(const vec4 fogCol) {
	return fogCol.g > 0.213101 ? 1.0 : 
		dot(vec4(fogCol.g * fogCol.g * fogCol.g, fogCol.g * fogCol.g, fogCol.g, 1.0), 
			vec4(349.305545, -159.858192, 30.557216, -1.628452));
}
#define WReflect 1
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

hp vec3 SeperateSunAbsorb(hp vec3 SunDIR,hp vec3 pos, out hp vec3 absorbLight){
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
    
   return  absorbSun;
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

hp vec3 calcAtmosphericScatterTerrain(hp vec3 SunDIR,hp vec3 pos, out hp vec3 absorbLight){
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
    
    return (scatterSun * absorbSun + sunSpot) * sunBrightness;
}

hp float lumaGrayscale(hp vec3 x){
    hp vec3 lumaVector = vec3(0.299, 0.587, 0.114);
    
    return dot(x, lumaVector);
}

hp vec3 FastApproximateAntiAliasing(sampler2D texture0,hp vec2 resolution,hp vec2 ScreenPos,hp vec2 texcoord,hp vec3 outColor){
    hp vec3 outputColor;

    hp vec2 perScreenPixels = 1.00 / resolution/(ScreenPos.xy / 64.0 * 0.25 + 0.75);
    
    hp vec2 perPixelsOffset = vec2(-1.00, 1.00) * perScreenPixels;
    hp vec2 perPixelsPosition = texcoord;
    
    hp vec3 pixelsNorthWestDirection = texture2D(texture0, perPixelsPosition + perPixelsOffset.xx).rgb;
    hp vec3 pixelsNorthEastDirection = texture2D(texture0, perPixelsPosition + perPixelsOffset.yx).rgb;
    
    hp vec3 pixelsSouthWestDirection = texture2D(texture0, perPixelsPosition + perPixelsOffset.xy).rgb;
    hp vec3 pixelsSouthEastDirection = texture2D(texture0, perPixelsPosition + perPixelsOffset.yy).rgb;
    
    hp vec3 pixelsMainDirection = texture2D(texture0, perPixelsPosition).rgb;
    
       hp float grayscaledPixelsInNorthWestDirection = lumaGrayscale(pixelsNorthWestDirection);
       hp float grayscaledPixelsInNorthEastDirection = lumaGrayscale(pixelsNorthEastDirection);
        
       hp float grayscaledPixelsInSouthWestDirection = lumaGrayscale(pixelsSouthWestDirection);
       hp float grayscaledPixelsInSouthEastDirection = lumaGrayscale(pixelsSouthEastDirection);
        
       hp float grayscaledPixelsInMainDirection = lumaGrayscale(pixelsMainDirection);
        
           hp float grayscaledPixelsInShortestLength = min(grayscaledPixelsInMainDirection, min(min(grayscaledPixelsInNorthWestDirection, grayscaledPixelsInNorthEastDirection), min(grayscaledPixelsInSouthWestDirection, grayscaledPixelsInSouthEastDirection)));
           hp float grayscaledPixelsInLongestLength = max(grayscaledPixelsInMainDirection, max(max(grayscaledPixelsInNorthWestDirection, grayscaledPixelsInNorthEastDirection), max(grayscaledPixelsInSouthWestDirection, grayscaledPixelsInSouthEastDirection)));
hp vec2 blendingDirection;
             blendingDirection.x = (grayscaledPixelsInNorthWestDirection + grayscaledPixelsInNorthEastDirection) - (grayscaledPixelsInSouthWestDirection + grayscaledPixelsInSouthEastDirection);
             blendingDirection.y = (grayscaledPixelsInNorthWestDirection + grayscaledPixelsInSouthWestDirection) - (grayscaledPixelsInNorthEastDirection + grayscaledPixelsInSouthEastDirection);
             
             blendingDirection = blendingDirection * vec2(-1.00, 1.00);
             
            hp float grayscaledPixelsInEveryDirection = (grayscaledPixelsInNorthWestDirection + grayscaledPixelsInNorthEastDirection + grayscaledPixelsInSouthWestDirection + grayscaledPixelsInSouthEastDirection) / 4.00;
            hp float blendingPerPixels = max(grayscaledPixelsInEveryDirection * 1.00 / 8.00, 1.00 / 128.0); 
            
            hp float pixelsBlendingDirection = 1.00 / (min(abs(blendingDirection.x), abs(blendingDirection.y)) + blendingPerPixels);
             
             blendingDirection = min(vec2(8.00, 8.00), max(vec2(-8.00, -8.00), blendingDirection * pixelsBlendingDirection)) * perScreenPixels;
             
hp vec3 outsideBlending = (texture2D(texture0, texcoord + blendingDirection * (1.00 / 3.00 - 0.50)).rgb + texture2D(texture0, texcoord + blendingDirection * (2.00 / 3.00 - 0.50)).rgb) / 2.00;
    hp vec3 insideBlending = (texture2D(texture0, texcoord + blendingDirection * -0.50).rgb + texture2D(texture0, texcoord + blendingDirection * 0.50).rgb) / 4.00 + outsideBlending / 2.00;
    
            hp float grayscaledPixelsInBlend = lumaGrayscale(insideBlending);
                    
                    if(grayscaledPixelsInBlend < grayscaledPixelsInShortestLength || grayscaledPixelsInBlend > grayscaledPixelsInLongestLength){
                        
                        outputColor = outsideBlending;
                    
                    } else {
                        
                        outputColor = insideBlending;
                    
                    }
    
    return outColor;
}

highp vec3 AO_SET(highp vec4 diffuse,highp vec3 color,highp vec3 n_color,highp vec3 N,highp vec3 Cp,highp vec2 uv1, bool isWater, vec4 FOG_COLOR,vec3 SunDIR){
 
bool fixer = (color.g * 1.0 >= color.r + color.b)&&((-N.y)>0.5);
bool detection = (color.g * 1.90 >= color.r + color.b)&&((N.y)>0.5);
bool detection2 = (color.g * 1.90 >= color.r + color.b)&&((N.y) < 1.0);
bool detection3 = (color.r *1.90 >= color.g)&&((N.y)>0.5);
bool detection4 = (color.r *1.90 >= color.g)&&((N.y)>1.0);
bool detection_other = (color.g * 1.0 >= color.r + color.b)&&(saturate(Cp.y)==1.0);


if(detection){
diffuse.rgb *= mix( pow(n_color,vec3(0.98,0.98,0.98)),color.rgb,pow(color.g+0.553*mix(1.0,0.78,NightTime),2.5));
}else if(detection2){
diffuse.rgb *= mix( pow(n_color,vec3(1.0,1.0,1.0)),color.rgb,pow(color.g+0.1,2.5));
}else if(detection3){
diffuse.rgb *= mix( pow(n_color,vec3(0.051,0.051,0.051)),color.rgb,pow(color.g+0.33*mix(1.0,0.78,NightTime),2.5));
}

if(detection_other){diffuse.rgb *= 1.10*mix(1.0,0.66,NightTime);}

if(fixer){
diffuse.rgb *= 0.58;
       }

return diffuse.rgb;
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

float Hash11(in float x) {
    x = fract(x * 0.1031);
    x *= x + 33.33;
    x *= x + x;
    return fract(x);
}

float smin(in float a, in float b, in float s) {
    float i = clamp(0.5 + 0.5 * (b - a) / s, 0.0, 1.0);
    return mix(b, a, i) - s * i * (1.0 - i);
}

vec2 getCellPoint(in vec2 p,highp float TIME) {
    float rndx = Hash11(p.x + 1000.0) * 2.0, rndy = Hash11(p.y + 1000.0) * 2.0;
    return 0.5 + 0.5 * vec2(sin(p.x * rndx + p.y * rndy + TIME * rndx), cos(p.y * rndx - p.x * rndy + 2.0 * TIME * rndy * rndx));
}

float voronoi(in vec2 uv,highp float TIME) {
    vec2 cellLocation = floor(uv);

    float minDist = 1000000000.0;
    for (int i=-1; i < 2; i++) {
        for (int j=-1; j < 2; j++) {
            vec2 curCellLocation = cellLocation + vec2(i, j);
            vec2 curCellPoint = curCellLocation + getCellPoint(curCellLocation, TIME);
            minDist = smin(minDist, length(uv - curCellPoint), 0.1);
        }
    }

    return minDist;
}

float fakeCaustic(vec2 pos, highp float TIME){
    float color = voronoi(pos, TIME);
    return color;
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

mat3 TBNCalculation(vec3 N){
vec3 up = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
vec3 tangent   = normalize(cross(up, N));
vec3 bitangent = cross(N, tangent);

mat3 TBN_Matrix = transpose(mat3(tangent,bitangent,N));

return TBN_Matrix;}

vec2 wavedx(vec2 position, vec2 direction, float frequency, float timeshift) {
  float x = dot(direction, position) * frequency + timeshift;
  float wave = exp(sin(x) - 1.0);
  float dx = wave * cos(x);
  return vec2(wave, -dx);
}

// Calculates waves by summing octaves of various waves with various parameters
float getwaves(vec2 position, int iterations, float TIME) {
  float iter = 0.0; // this will help generating well distributed wave directions
  float frequency = 1.0; // frequency of the wave, this will change every iteration
  float timeMultiplier = 2.0; // time multiplier for the wave, this will change every iteration
  float weight = 1.0;// weight in final sum for the wave, this will change every iteration
  float sumOfValues = 0.0; // will store final sum of values
  float sumOfWeights = 0.0; // will store final sum of weights
  for(int i=0; i < iterations; i++) {
    // generate some wave direction that looks kind of random
    vec2 p = vec2(sin(iter), cos(iter));
    // calculate wave data
    vec2 res = wavedx(position, p, frequency, TIME * timeMultiplier);

    // shift position around according to wave drag and derivative of the wave
    position += p * res.y * weight * 0.48;

    // add the results to sums
    sumOfValues += res.x * weight;
    sumOfWeights += weight;

    // modify next octave parameters
    weight *= 0.82;
    frequency *= 1.18;
    timeMultiplier *= 1.07;

    // add some kind of random value to make next wave look random too
    iter += 43758.5453;
  }
  // calculate and return
  return sumOfValues / sumOfWeights;
}

highp vec3 GenerateWaveMap(highp vec3 p,highp vec3 N,highp float TIME){

highp float wave = getwaves(p.xz, 5, TIME);

highp float w1 = (wave-getwaves(vec2(p.x-0.078,p.z), 5, TIME));
highp float w2 = (wave-getwaves(vec2(p.x,p.z-0.078), 5, TIME));

highp vec3 Normals_w = normalize(vec3(w1,w2,1.0))*0.5+0.5;

highp vec3 results =  normalize(mul((Normals_w*2.0-1.0),TBNCalculation(N)));

  return results;
}

vec3 fresnelSchlick(vec3 f0, float normaldotview){
	return f0 + (1.0 - f0) * pow(1.0 - normaldotview, 5.0);
}

bool EndWorld(hp vec4 fogcolor){
if(((fogcolor.r>fogcolor.g)&&(fogcolor.b>fogcolor.g)&&(fogcolor.b>fogcolor.r))&&(fogcolor.r<0.05&&fogcolor.b<0.05&&fogcolor.g<0.05))
{return true;	
} else {
return false;}
}

bool NetherWorld(hp vec2 fogControl){
hp float hell = step(0.1, fogControl.x / fogControl.y) - step(0.12, fogControl.x / fogControl.y);
if(hell == 1.0){
return true;
} else {
return false;}
}

void main() {
     vec4 diffuse;
    
    highp float SunTIME;
    SunTIME = fogTime;

    highp vec3 Dir = normalize(vec3(0.5,1.0,0.5));
    highp vec3 Rotate = vec3(cos(mod(SunTIME,PI)),sin(mod(SunTIME,PI)),cos(mod(SunTIME,PI)));
    highp vec3 SunDIR = normalize(Dir * Rotate);
    
    highp vec3 fN = 1.0-ceil(fract(ChunkPos));
    highp vec3 nColor = normalize(v_color0.rgb);
    highp vec3 N = normalize(cross(dFdx(ChunkPos),dFdy(ChunkPos)));
  
#if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)
    diffuse.rgb = vec3(1.0, 1.0, 1.0);
#else
    diffuse.rgb = FastApproximateAntiAliasing(s_MatTexture,gl_FragCoord.xy,ScreenPos.xy,v_texcoord0,texture2D(s_MatTexture, v_texcoord0).rgb);
    diffuse.a = texture2D(s_MatTexture, v_texcoord0).a;

#if defined(ALPHA_TEST)
    if (diffuse.a < 0.5) {
        discard;
    }
#endif

highp vec3 AO = AO_SET(diffuse, v_color0.rgb, nColor, N, fN, v_lightmapUV, bool(v_color0.b > v_color0.r), FOG_COLOR, SunDIR);

#if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
    diffuse.rgb *=
        mix(vec3(1.0, 1.0, 1.0),
            texture2D(s_SeasonsTexture, v_color0.xy).rgb * 2.0, v_color0.b);
    diffuse.rgb *= v_color0.aaa;
#else
diffuse.rgb = mix(mix(AO,AO,DuskTime),saturation(AO, 0.38),NightTime);
#endif
#endif

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif

    bool WaterDetect = false;
    bool UnderWaterDetect = FOGC.x==0.0;
    bool NetherDetect = NetherWorld(FOGC.xy);
    
    #ifdef TRANSPARENT
    WaterDetect = v_color0.b > v_color0.r;
    #endif
    
     highp float DepthDetect = 0.0;
     highp float Dirlight = saturate(dot(SunDIR * 3.0, N));

#if (!defined(ALPHA_TEST) || !defined(SEASONS) || !defined(OPAQUE))
    DepthDetect = smoothstep(0.869,0.84, v_lightmapUV.y)*step(abs((2.0 * ChunkPos.y - 14.0) / 15.0 + saturate(v_texcoord0.y - 14.0) -  v_lightmapUV.y),0.2)*saturate(ChunkPos.y-WorldPos.y-15.0)*Dirlight*(1.0-saturate(-N.y));
#endif

if((v_color0.g > v_color0.r)||(v_color0.g < v_color0.r)){
    DepthDetect = 0.0;
}

    highp vec3 V = normalize(-WorldPos);
    vec4 tex = texture2D(s_MatTexture, v_texcoord0);

    float BlockDetector = tex.a;
    bool AlphaDetect_Emessive = ((BlockDetector > 0.93 && BlockDetector < 0.9460) && (v_color0.b == v_color0.g) && (v_color0.r == v_color0.g));
    
    bool Alpha_IRON = BlockDetector > 0.99 && BlockDetector < 1.0 && (v_color0.b == v_color0.g) && (v_color0.r == v_color0.g);
    bool Alpha_GOLD = BlockDetector > 0.98 && BlockDetector < 0.99 && (v_color0.b == v_color0.g) && (v_color0.r == v_color0.g);
    bool Alpha_DIAMOND = BlockDetector > 0.97 && BlockDetector < 0.98 && (v_color0.b == v_color0.g) && (v_color0.r == v_color0.g);
    bool Alpha_COPPER = BlockDetector > 0.96 && BlockDetector < 0.97 && (v_color0.b == v_color0.g) && (v_color0.r == v_color0.g);
    bool Alpha_Other = BlockDetector > 0.90 && BlockDetector < 0.909 && (v_color0.b == v_color0.g) && (v_color0.r == v_color0.g);

    highp float ShadowDetect = smoothstep(0.878,0.848,v_lightmapUV.y)*float(!UnderWaterDetect)*mix(1.0,0.0,DepthDetect);
    highp float noLight = 1.0-v_lightmapUV.y;
    highp float fogDist = length(-WorldPos.xyz)/RENDER_CHUNKS;
    float uv1x = clamp(0.14+smoothstep(0.54,1.0,v_lightmapUV.x*0.85),0.0,1.0);
    highp float LightSource = saturate(uv1x * uv1x * uv1x * uv1x);
    
    highp vec3 lightAbsorb = vec3(0.0,0.0,0.0);
    highp vec3 NightColor = saturation(vec3(0.18,0.26,0.35)*0.719,0.18)*2.07;
    highp vec3 SkyLight = calcAtmosphericScatter(normalize(Dir * Rotate * 3.0), SunDIR, lightAbsorb);
    highp vec3 TerrainLight = calcAtmosphericScatterTerrain(normalize(Dir * Rotate * 3.0), vec3(0.0,-1.0,0.0), lightAbsorb);
    highp vec3 ShadowColor = calcAtmosphericScatterTerrain(normalize(Dir * Rotate * 3.0), vec3(0.0,1.0,0.0), lightAbsorb);
    
    highp vec3 TerrainLightning = mix(saturate(TerrainLight), NightColor, NightTime);
    highp vec3 SkyLightning = mix(saturate(SkyLight), NightColor*0.26, NightTime);
    highp vec3 ShadowLightning = mix(ShadowColor, NightColor*1.5, NightTime);

if(!AlphaDetect_Emessive){
    diffuse.rgb *= mix(max(ShadowLightning, Dirlight),ShadowLightning,ShadowDetect);
}
    diffuse.rgb *= TerrainLightning;
    if(!NetherDetect){
    diffuse.rgb *= mix(TerrainLightning, ShadowLightning*0.68, (noLight));
    }
    
    highp vec3 LightTemp = vec3(1.0,0.78,0.6)*1.20;
    highp vec3 LightSet = saturation(LightTemp,2.0)*(LightSource)*(sqrt(diffuse.y)*3.10);
    
    if(AlphaDetect_Emessive){
    #ifdef ALPHA_TEST
    diffuse.rgb *= vec3(4.5);
    #else
    diffuse.rgb *= vec3(4.5);
    #endif
   }
    
    diffuse.rgb += LightSet*mix(1.0,3.0,noLight);
       
#ifdef TRANSPARENT
if(WaterDetect){
    diffuse.rgb = vec3(0.0,0.0,0.0);
    vec3 Normals = N;

    N = reflect(normalize(WorldPos),GenerateWaveMap(ChunkPos, N, TIME));

    highp float normaldotview = max(0.001, dot(N,V));

    highp float Dither = bayer16(gl_FragCoord.xy);
    highp vec3 mpos = N/N.y;
    
    highp vec3 lightAbsorb = vec3(0.0,0.0,0.0);
    highp vec3 Atmosphere = calcAtmosphericScatter(normalize(Dir * Rotate * 3.0), N, lightAbsorb)*mix(1.0,0.05,NightTime);
    
    if(NightTime > 0.2){
    Atmosphere += starLayer(N.xz, TIME)*1.3;
    }

    highp float Sun = DrawSun(cross(mpos*14.0,normalize(Dir * Rotate * 3.0)));
  
    vec3 SunColour = mix(vec3(1.0,0.95,0.9)*Sun,vec3(1.0,0.93,0.69)*Sun,DuskTime);
    SunColour = mix(SunColour,vec3(0.76,0.74,0.68)*Sun,NightTime);
    
    vec4 CloudColour = mix(vec4(0.9,0.9,1.0,1.0)*1.10,vec4(1.0,0.93,0.69,1.0)*1.10, DuskTime);
    CloudColour = mix(CloudColour,vec4(saturation(vec3(0.18,0.26,0.35)*0.819,0.18),1.0)*0.91,NightTime);
   
    vec4 CloudsShdColour = mix(vec4(vec3(0.27,0.449 ,0.59),0.8)*0.52,vec4(vec3(0.27,0.449 ,0.59),0.8)*0.52, DuskTime);
    CloudsShdColour = mix(CloudsShdColour, vec4(saturation(vec3(0.18,0.26,0.35)*0.719,0.18),1.0)*0.32,NightTime);
    
    highp vec4 Clouds = calcVolumetricClouds(smoothstep(0.0, 0.35, N.y), vec4(Atmosphere+SunColour, 1.0), normalize(Dir * Rotate * 3.0), CloudColour.rgb, CloudsShdColour.rgb, N, fogTime, Dither, TIME, FOGC, FOG_COLOR);

    diffuse.rgb = mix(diffuse.rgb,TonemapUncharted(Clouds.rgb),fresnelSchlick(vec3(0.5), normaldotview*(1.0-Sun)*(1.0-noLight)));
    diffuse.rgb = mix(diffuse.rgb,Atmosphere*0.4, fresnelSchlick(vec3(0.5), normaldotview)*noLight);
    diffuse.a = mix(0.95,0.08,smoothstep(0.03,0.8,N.y)*(1.0-Sun));
}
#endif

    vec3 f0;
    
    if(Alpha_IRON){
    f0 = vec3(0.56, 0.57, 0.58);
    f0 = mix(f0, diffuse.rgb, float(Alpha_IRON));
    }else if(Alpha_GOLD){
    f0 = vec3(1.00, 0.71, 0.29);
    f0 = mix(f0, diffuse.rgb, float(Alpha_GOLD));
    }else if(Alpha_DIAMOND){
    f0 = vec3(0.17,0.17,0.17);
    f0 = mix(f0, diffuse.rgb, float(Alpha_DIAMOND));
    }else if(Alpha_COPPER){
    f0 = vec3(0.95,0.64,0.54);
    f0 = mix(f0, diffuse.rgb, float(Alpha_COPPER));
    }else if(Alpha_Other){
    f0 = vec3(0.065,0.065,0.065);
    f0 = mix(f0, diffuse.rgb, float(Alpha_Other));
    }

    float normaldotview = max(0.001, dot(N,V));

#if !defined(TRANSPARENT)
if(!NetherDetect){
   if((Alpha_IRON)||(Alpha_GOLD)||(Alpha_DIAMOND)||(Alpha_COPPER)||(Alpha_Other)){
   vec3 base0 = texture2D(s_MatTexture, v_texcoord0).rgb;
   highp float base = lumaGrayscale(base0);

highp float w1 = (base-lumaGrayscale(texture2D(s_MatTexture, vec2(v_texcoord0.x-0.00018,v_texcoord0.y)).rgb));
highp float w2 = (base-lumaGrayscale(texture2D(s_MatTexture, vec2(v_texcoord0.x,v_texcoord0.y-0.00018)).rgb));

highp vec3 Normals_w = normalize(vec3(w1,w2,1.0))*0.5+0.5;

highp vec3 Normal = N;
highp vec3 results =  normalize(mul((Normals_w*2.0-1.0),TBNCalculation(Normal)));

N = reflect(normalize(WorldPos),results);

    highp float Dither = bayer16(gl_FragCoord.xy);
    highp vec3 mpos = N/N.y;

    highp vec3 lightAbsorb = vec3(0.0,0.0,0.0);
    highp vec3 Atmosphere = calcAtmosphericScatter(normalize(Dir * Rotate * 3.0), N, lightAbsorb)*mix(1.0,0.05,NightTime);

    if(NightTime > 0.2){
    Atmosphere += starLayer(N.xz, TIME)*1.3*smoothstep(0.0,0.6,N.y);
    }

    highp float Sun = DrawSun(cross(mpos*10.0,normalize(Dir * Rotate * 3.0)))*Dirlight;
  
    highp vec3 SunColour = mix(vec3(1.0,0.95,0.9)*Sun,vec3(1.0,0.93,0.69)*Sun,DuskTime);
    SunColour = mix(SunColour,vec3(0.76,0.74,0.68)*Sun,NightTime);
    
    highp vec4 CloudColour = mix(vec4(0.9,0.9,1.0,1.0)*1.10,vec4(1.0,0.93,0.69,1.0)*1.10, DuskTime);
    CloudColour = mix(CloudColour,vec4(saturation(vec3(0.18,0.26,0.35)*0.819,0.18),1.0)*0.91,NightTime);
   
    highp vec4 CloudsShdColour = mix(vec4(vec3(0.27,0.449 ,0.59),0.8)*0.52,vec4(vec3(0.27,0.449 ,0.59),0.8)*0.52, DuskTime);
    CloudsShdColour = mix(CloudsShdColour, vec4(saturation(vec3(0.18,0.26,0.35)*0.719,0.18),1.0)*0.32,NightTime);
    
    highp vec4 Clouds = calcVolumetricClouds(smoothstep(0.0, 0.35, N.y), vec4(Atmosphere+SunColour, 1.0), normalize(Dir * Rotate * 3.0), CloudColour.rgb, CloudsShdColour.rgb, N, fogTime, Dither, TIME, FOGC, FOG_COLOR);

if(NightTime < 0.2){
diffuse.rgb = mix(diffuse.rgb,Clouds.rgb,fresnelSchlick(f0, normaldotview)*(1.0-noLight)*(1.0-saturate(-N.y)));
}else{
diffuse.rgb = mix(diffuse.rgb,Clouds.rgb,0.8*(1.0-noLight)*(1.0-saturate(-N.y)));
}
  }
   }
#endif

if(NetherDetect){
diffuse.rgb *= vec3(2.5);
}

if(!NetherDetect){
if(DepthDetect > 0.5){
    vec3 pos = ChunkPos/ChunkPos.y;
    pos *= 3.8;
    float w = fakeCaustic(pos.xz, TIME);
    float intensity = exp(w*4.0 - 1.0);
    diffuse.rgb = mix(diffuse.rgb,diffuse.rgb*Dirlight*(intensity*mix(1.0,0.38,NightTime)),(1.0-noLight));
   }
 }

if(!NetherDetect){
highp float AbsorptionDepth = (sqrt(dot(normalize(WorldPos), (WorldPos)))/1.0)*(1.0-v_lightmapUV.x);
    AbsorptionDepth *= smoothstep(WATER_ABSORPTION_MAX,WATER_ABSORPTION_MIN,v_lightmapUV.y);
    diffuse.rgb = mix(diffuse.rgb,(diffuse.rgb*2.75)*exp(-((WATER_ABSORPTION))*pow(abs(AbsorptionDepth),1.23)),noLight);
}

    diffuse.rgb = mix(diffuse.rgb, SkyLightning, clamp(fogDist*0.34*clamp(max(FOG_COLOR.r,0.5)*(0.5-v_lightmapUV.y),0.5,1.0),0.0,1.0));
    
#if !defined(TRANSPARENT)
    diffuse.rgb = TonemapUncharted(diffuse.rgb);
#endif

    gl_FragColor = diffuse;
}