// fake bgfx shader headers for clangd

#define SWIZZLE_VEC2(x,y) \
  float x,y; \
  vec2 &x##x,&x##y,&y##x,&y##y; \
  vec3 &x##x##x,&x##x##y,&x##y##x,&x##y##y,&y##x##x,&y##x##y,&y##y##x,&y##y##y; \
  vec4 &x##x##x##x,&x##x##x##y,&x##x##y##x,&x##x##y##y,&x##y##x##x,&x##y##x##y,&x##y##y##x,&x##y##y##y,&y##x##x##x,&y##x##x##y,&y##x##y##x,&y##x##y##y,&y##y##x##x,&y##y##x##y,&y##y##y##x,&y##y##y##y;

#define SWIZZLE_VEC3(x,y,z) \
  float x,y,z; \
  vec2 x##x,x##y,x##z,y##x,y##y,y##z,z##x,z##y,z##z; \
  vec3 &x##x##x,&x##x##y,&x##x##z,&x##y##x,&x##y##y,&x##y##z,&x##z##x,&x##z##y,&x##z##z,&y##x##x,&y##x##y,&y##x##z,&y##y##x,&y##y##y,&y##y##z,&y##z##x,&y##z##y,&y##z##z,&z##x##x,&z##x##y,&z##x##z,&z##y##x,&z##y##y,&z##y##z,&z##z##x,&z##z##y,&z##z##z; \
  vec4 &x##x##x##x,&x##x##x##y,&x##x##x##z,&x##x##y##x,&x##x##y##y,&x##x##y##z,&x##x##z##x,&x##x##z##y,&x##x##z##z,&x##y##x##x,&x##y##x##y,&x##y##x##z,&x##y##y##x,&x##y##y##y,&x##y##y##z,&x##y##z##x,&x##y##z##y,&x##y##z##z,&x##z##x##x,&x##z##x##y,&x##z##x##z,&x##z##y##x,&x##z##y##y,&x##z##y##z,&x##z##z##x,&x##z##z##y,&x##z##z##z,&y##x##x##x,&y##x##x##y,&y##x##x##z,&y##x##y##x,&y##x##y##y,&y##x##y##z,&y##x##z##x,&y##x##z##y,&y##x##z##z,&y##y##x##x,&y##y##x##y,&y##y##x##z,&y##y##y##x,&y##y##y##y,&y##y##y##z,&y##y##z##x,&y##y##z##y,&y##y##z##z,&y##z##x##x,&y##z##x##y,&y##z##x##z,&y##z##y##x,&y##z##y##y,&y##z##y##z,&y##z##z##x,&y##z##z##y,&y##z##z##z,&z##x##x##x,&z##x##x##y,&z##x##x##z,&z##x##y##x,&z##x##y##y,&z##x##y##z,&z##x##z##x,&z##x##z##y,&z##x##z##z,&z##y##x##x,&z##y##x##y,&z##y##x##z,&z##y##y##x,&z##y##y##y,&z##y##y##z,&z##y##z##x,&z##y##z##y,&z##y##z##z,&z##z##x##x,&z##z##x##y,&z##z##x##z,&z##z##y##x,&z##z##y##y,&z##z##y##z,&z##z##z##x,&z##z##z##y,&z##z##z##z;

#define SWIZZLE_VEC4(x,y,z,w) \
  float x,y,z,w; \
  vec2 x##x,x##y,x##z,x##w,y##x,y##y,y##z,y##w,z##x,z##y,z##z,z##w,w##x,w##y,w##z,w##w; \
  vec3 x##x##x,x##x##y,x##x##z,x##x##w,x##y##x,x##y##y,x##y##z,x##y##w,x##z##x,x##z##y,x##z##z,x##z##w,x##w##x,x##w##y,x##w##z,x##w##w,y##x##x,y##x##y,y##x##z,y##x##w,y##y##x,y##y##y,y##y##z,y##y##w,y##z##x,y##z##y,y##z##z,y##z##w,y##w##x,y##w##y,y##w##z,y##w##w,z##x##x,z##x##y,z##x##z,z##x##w,z##y##x,z##y##y,z##y##z,z##y##w,z##z##x,z##z##y,z##z##z,z##z##w,z##w##x,z##w##y,z##w##z,z##w##w,w##x##x,w##x##y,w##x##z,w##x##w,w##y##x,w##y##y,w##y##z,w##y##w,w##z##x,w##z##y,w##z##z,w##z##w,w##w##x,w##w##y,w##w##z,w##w##w; \
  vec4 &x##x##x##x,&x##x##x##y,&x##x##x##z,&x##x##x##w,&x##x##y##x,&x##x##y##y,&x##x##y##z,&x##x##y##w,&x##x##z##x,&x##x##z##y,&x##x##z##z,&x##x##z##w,&x##x##w##x,&x##x##w##y,&x##x##w##z,&x##x##w##w,&x##y##x##x,&x##y##x##y,&x##y##x##z,&x##y##x##w,&x##y##y##x,&x##y##y##y,&x##y##y##z,&x##y##y##w,&x##y##z##x,&x##y##z##y,&x##y##z##z,&x##y##z##w,&x##y##w##x,&x##y##w##y,&x##y##w##z,&x##y##w##w,&x##z##x##x,&x##z##x##y,&x##z##x##z,&x##z##x##w,&x##z##y##x,&x##z##y##y,&x##z##y##z,&x##z##y##w,&x##z##z##x,&x##z##z##y,&x##z##z##z,&x##z##z##w,&x##z##w##x,&x##z##w##y,&x##z##w##z,&x##z##w##w,&x##w##x##x,&x##w##x##y,&x##w##x##z,&x##w##x##w,&x##w##y##x,&x##w##y##y,&x##w##y##z,&x##w##y##w,&x##w##z##x,&x##w##z##y,&x##w##z##z,&x##w##z##w,&x##w##w##x,&x##w##w##y,&x##w##w##z,&x##w##w##w,&y##x##x##x,&y##x##x##y,&y##x##x##z,&y##x##x##w,&y##x##y##x,&y##x##y##y,&y##x##y##z,&y##x##y##w,&y##x##z##x,&y##x##z##y,&y##x##z##z,&y##x##z##w,&y##x##w##x,&y##x##w##y,&y##x##w##z,&y##x##w##w,&y##y##x##x,&y##y##x##y,&y##y##x##z,&y##y##x##w,&y##y##y##x,&y##y##y##y,&y##y##y##z,&y##y##y##w,&y##y##z##x,&y##y##z##y,&y##y##z##z,&y##y##z##w,&y##y##w##x,&y##y##w##y,&y##y##w##z,&y##y##w##w,&y##z##x##x,&y##z##x##y,&y##z##x##z,&y##z##x##w,&y##z##y##x,&y##z##y##y,&y##z##y##z,&y##z##y##w,&y##z##z##x,&y##z##z##y,&y##z##z##z,&y##z##z##w,&y##z##w##x,&y##z##w##y,&y##z##w##z,&y##z##w##w,&y##w##x##x,&y##w##x##y,&y##w##x##z,&y##w##x##w,&y##w##y##x,&y##w##y##y,&y##w##y##z,&y##w##y##w,&y##w##z##x,&y##w##z##y,&y##w##z##z,&y##w##z##w,&y##w##w##x,&y##w##w##y,&y##w##w##z,&y##w##w##w,&z##x##x##x,&z##x##x##y,&z##x##x##z,&z##x##x##w,&z##x##y##x,&z##x##y##y,&z##x##y##z,&z##x##y##w,&z##x##z##x,&z##x##z##y,&z##x##z##z,&z##x##z##w,&z##x##w##x,&z##x##w##y,&z##x##w##z,&z##x##w##w,&z##y##x##x,&z##y##x##y,&z##y##x##z,&z##y##x##w,&z##y##y##x,&z##y##y##y,&z##y##y##z,&z##y##y##w,&z##y##z##x,&z##y##z##y,&z##y##z##z,&z##y##z##w,&z##y##w##x,&z##y##w##y,&z##y##w##z,&z##y##w##w,&z##z##x##x,&z##z##x##y,&z##z##x##z,&z##z##x##w,&z##z##y##x,&z##z##y##y,&z##z##y##z,&z##z##y##w,&z##z##z##x,&z##z##z##y,&z##z##z##z,&z##z##z##w,&z##z##w##x,&z##z##w##y,&z##z##w##z,&z##z##w##w,&z##w##x##x,&z##w##x##y,&z##w##x##z,&z##w##x##w,&z##w##y##x,&z##w##y##y,&z##w##y##z,&z##w##y##w,&z##w##z##x,&z##w##z##y,&z##w##z##z,&z##w##z##w,&z##w##w##x,&z##w##w##y,&z##w##w##z,&z##w##w##w,&w##x##x##x,&w##x##x##y,&w##x##x##z,&w##x##x##w,&w##x##y##x,&w##x##y##y,&w##x##y##z,&w##x##y##w,&w##x##z##x,&w##x##z##y,&w##x##z##z,&w##x##z##w,&w##x##w##x,&w##x##w##y,&w##x##w##z,&w##x##w##w,&w##y##x##x,&w##y##x##y,&w##y##x##z,&w##y##x##w,&w##y##y##x,&w##y##y##y,&w##y##y##z,&w##y##y##w,&w##y##z##x,&w##y##z##y,&w##y##z##z,&w##y##z##w,&w##y##w##x,&w##y##w##y,&w##y##w##z,&w##y##w##w,&w##z##x##x,&w##z##x##y,&w##z##x##z,&w##z##x##w,&w##z##y##x,&w##z##y##y,&w##z##y##z,&w##z##y##w,&w##z##z##x,&w##z##z##y,&w##z##z##z,&w##z##z##w,&w##z##w##x,&w##z##w##y,&w##z##w##z,&w##z##w##w,&w##w##x##x,&w##w##x##y,&w##w##x##z,&w##w##x##w,&w##w##y##x,&w##w##y##y,&w##w##y##z,&w##w##y##w,&w##w##z##x,&w##w##z##y,&w##w##z##z,&w##w##z##w,&w##w##w##x,&w##w##w##y,&w##w##w##z,&w##w##w##w; \


#define VEC_OPS(x) \
  inline x operator+(x,x); \
  inline x operator+(float,x); \
  inline x operator+(x,float); \
  inline x operator-(x,x); \
  inline x operator-(float,x); \
  inline x operator-(x,float); \
  inline x operator*(float,x); \
  inline x operator*(x,float); \
  inline x operator*(x,x); \
  inline x operator/(float,x); \
  inline x operator/(x,float); \
  inline x operator/(x,x); \
  inline x operator*=(x,x); \
  inline x operator*=(float,x); \
  inline x operator*=(x,float); \
  inline x operator/=(x,x); \
  inline x operator/=(float,x); \
  inline x operator/=(x,float); \
  inline x operator+=(x,x); \
  inline x operator+=(float,x); \
  inline x operator+=(x,float); \
  inline x operator-=(x,x); \

struct vec2;
struct vec3;
struct vec4;
struct mat4;

typedef struct vec2{
  SWIZZLE_VEC2(x,y)
  SWIZZLE_VEC2(u,v)

  vec2();
  vec2(float, float);
  vec2& operator = (vec2 *a);
  vec2& operator = (vec2 a);
  float operator[](unsigned int);
} vec2;

typedef struct vec3 {
  SWIZZLE_VEC3(x,y,z)
  SWIZZLE_VEC3(r,g,b)

  vec3();
  vec3(float, float, float);
  vec3(vec2, float);
  vec3(float, vec2);
  vec3& operator = (vec3 *a);
  vec3& operator = (vec3 a);
  float operator[](unsigned int);
} vec3;

typedef struct vec4 {
  SWIZZLE_VEC4(x,y,z,w)
  SWIZZLE_VEC4(r,g,b,a)
 
  vec4();
  vec4(float, float, float, float);
  vec4(vec2, float, float);
  vec4(float, vec2, float);
  vec4(float, float, vec2);
  vec4(vec2, vec2);
  vec4(vec3, float);
  vec4(float, vec3);
  vec4& operator = (vec4 *a);
  vec4& operator = (vec4 a);
  float operator[](unsigned int);
} vec4;

typedef struct mat4 {
  vec4 operator[](unsigned int);
} mat4;

typedef struct mat3 {
  vec3 operator[](unsigned int);
} mat3;

typedef struct mat2 {
  vec2 operator[](unsigned int);
} mat2;

#define EMPTY_STRUCT(x) typedef struct x {} x;

EMPTY_STRUCT(sampler2D)

vec4 texture2D(sampler2D, vec2);
vec4 texture2DLod(sampler2D, vec2, float);

VEC_OPS(vec2)
VEC_OPS(vec3)
VEC_OPS(vec4)

inline vec4 operator*(mat4, vec4);
inline vec3 operator*(mat3, vec3);
inline vec3 operator*(mat2, vec2);

#define TF_TF(x) \
  float x(float); \
  vec2 x(vec2); \
  vec3 x(vec3); \
  vec4 x(vec4);
#define F_TF(x) \
  float x(float); \
  float x(vec2); \
  float x(vec3); \
  float x(vec4);
#define TF_TF_TF(x) \
  float x(float, float); \
  vec2 x(vec2, vec2); \
  vec3 x(vec3, vec3); \
  vec4 x(vec4, vec4);
#define TF_F_TF(x) \
  float x(float, float); \
  vec2 x(float, vec2); \
  vec3 x(float, vec3); \
  vec4 x(float, vec4);
#define TF_TF_F(x) \
  float x(float, float); \
  vec2 x(vec2, float); \
  vec3 x(vec3, float); \
  vec4 x(vec4, float);
#define F_TF_TF(x) \
  float x(float, float); \
  float x(vec2, vec2); \
  float x(vec3, vec3); \
  float x(vec4, vec4);
#define TF_TF_TF_TF(x) \
  float x(float, float, float); \
  vec2 x(vec2, vec2, vec2); \
  vec3 x(vec3, vec3, vec3); \
  vec4 x(vec4, vec4, vec4);
#define TF_TF_TF_F(x) \
  float x(float, float, float); \
  vec2 x(vec2, vec2, float); \
  vec3 x(vec3, vec3, float); \
  vec4 x(vec4, vec4, float);
#define TF_F_F_TF(x) \
  float x(float, float, float); \
  vec2 x(float, float, vec2); \
  vec3 x(float, float, vec3); \
  vec4 x(float, float, vec4);
#define TF_TF_F_F(x) \
  float x(float, float, float); \
  vec2 x(vec2, float, float); \
  vec3 x(vec3, float, float); \
  vec4 x(vec4, float, float);

TF_TF(radians)
TF_TF(degrees)
TF_TF(sin)
TF_TF(cos)
TF_TF(tan)
TF_TF(asin)
TF_TF(acos)
TF_TF(atan)
TF_TF_TF(atan)
TF_TF_TF(atan2)

TF_TF(exp)
TF_TF_TF(pow)
TF_TF(log)
TF_TF(exp2)
TF_TF(log2)
TF_TF(sqrt)
TF_TF(inversesqrt)

TF_TF(abs)
TF_TF(sign)
TF_TF(floor)
TF_TF(ceil)
TF_TF(fract)
TF_TF(mod)
TF_TF_TF(min)
TF_TF_TF(max)
TF_TF_TF(step)
TF_TF_F(min)
TF_TF_F(max)
TF_TF_TF(step)
TF_F_TF(step)
TF_TF_TF_TF(mix)
TF_TF_TF_F(mix)
TF_TF_TF_TF(smoothstep)
TF_TF_TF_TF(clamp)
TF_F_F_TF(smoothstep)
TF_TF_F_F(clamp)

F_TF(length)
F_TF_TF(distance)
F_TF_TF(dot)
TF_TF(normalize)
TF_TF_TF_TF(faceforward)
TF_TF(reflect)

vec3 cross(vec3, vec3);

TF_TF(dFdy)
TF_TF(dFdx)

vec4 mul(mat4, vec4);

vec2 vec2_splat(float);
vec3 vec3_splat(float);
vec4 vec4_splat(float);

#define uniform static 
#define out 
#define inout 
#define highp

// bgfx uniforms
uniform mat4 u_invProj;
uniform mat4 u_invView;
uniform mat4 u_view;

#define SAMPLER2D(_sampler, _reg) uniform sampler2D _sampler;

