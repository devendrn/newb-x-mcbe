#define DEFAULT

/*
  EDITING CONFIG:

  > TOGGLES
  if (toggle) is mentioned, then
  options can be commented to disable (by adding/removing '//')
  eg: #define PLANTS_WAVE    -> this is ON
      //#define PLANTS_WAVE  -> this is OFF

  > COLOR VALUES
  color format: vec3(<red>,<green>,<blue>)
  1.0 means 100%, 0.0 means 0%
  eg. vec3(1.0,1.0,0.0) = yellow

  > VALUES
  values must be decimal
  eg. 32 is wrong, 32.0 is correct

  > TYPES
  should be integer. options to choose will be mentioned there
  eg: #define NL_FOG_TYPE 2

  BUILD THE SHADER AFTER EACH EDIT
*/


/* -------- CONFIG STARTS HERE ----------- */

/* Tonemap type
   1:Exponential, 2:Reinhard, 3:Extended Reinhard, 4:ACES */
#define NL_TONEMAP_TYPE 3

/* Color correction options (toggle) */
//#define NL_EXPOSURE   1.3
//#define NL_SATURATION 1.4
//#define NL_TINT       vec3(1.0,0.75,0.5)

#define NL_CONSTRAST 0.75

/* Terrain lighting */
#define NL_SUN_INTENSITY    2.95
#define NL_TORCH_INTENSITY  1.0
#define NL_NIGHT_BRIGHTNESS 0.1
#define NL_CAVE_BRIGHTNESS  0.1

/* Shadow darkness (0.0 - 1.0) - 0.0 means no shadow */
#define NL_SHADOW_INTENSITY 0.7

/* Mist density */
#define NL_MIST_DENSITY 0.18

/* Fog fade
   0:No fog, 1:Vanilla fog, 2:Smoother vanilla fog */
#define NL_FOG_TYPE 2

/* Sun/moon light color on terrain */
#define NL_MORNING_SUN_COL vec3(1.0,0.45,0.14)
#define NL_NOON_SUN_COL    vec3(1.0,0.75,0.57)
#define NL_NIGHT_SUN_COL   vec3(0.5,0.64,1.00)

/* Blinking torch light (toggle) */
//#define NL_BLINKING_TORCH

/* Torch colors */
#define NL_OVERWORLD_TORCH_COL  vec3(1.0,0.52,0.18)
#define NL_UNDERWATER_TORCH_COL vec3(1.0,0.52,0.18)
#define NL_NETHER_TORCH_COL     vec3(1.0,0.52,0.18)
#define NL_END_TORCH_COL        vec3(1.0,0.52,0.18)

/* Sky/fog colors */
#define NL_NIGHT_SKY_COL    vec3(0.01,0.06,0.1)
#define NL_BASE_SKY_COL     vec3(0.15,0.45,1.0)
#define NL_BASE_HORIZON_COL vec3(1.00,0.40,0.3)
#define NL_EDGE_HORIZON_COL vec3(1.00,0.40,0.2)
#define NL_DAY_SKY_CLARITY  0.3

/* Ore glow intensity */
#define NL_GLOW_TEX 2.0

/* Plants wave intensity (toggle) */
#define NL_PLANTS_WAVE 0.04

/* Waving speed */
#define NL_WAVE_SPEED 2.8

/* Water */
#define NL_WATER_TRANSPARENCY 0.9
#define NL_WATER_BUMP         0.07
#define NL_WATER_TINT         vec3(0.52,0.9,0.45)

/* Water wave (toggle) */
#define NL_WATER_WAVE

/* Fade water opacity with fog (toggle) */
#define NL_WATER_FOG_FADE

/* Water texture overlay - 0.0 means no texture on water */
#define NL_WATER_TEX_OPACITY 0.3

/* Vnderwater lighting */
#define NL_UNDERWATER_BRIGHTNESS 0.8
#define NL_CAUSTIC_INTENSITY     1.9
#define NL_UNDERWATER_TINT       vec3(0.9,1.0,0.9)

/* Underwater wave intensity (toggle) */
#define NL_UNDERWATER_WAVE 0.1

/* Lantern swing intensity (toggle) */
#define NL_LANTERN_WAVE 0.16

/* Clouds */
#define NL_CLOUD_UV_SCALE vec2(0.0194, 0.0278)
#define NL_CLOUD_DEPTH    1.3
#define NL_CLOUD_SPEED    0.04
#define NL_CLOUD_DENSITY  0.54
#define NL_CLOUD_OPACITY  0.9

/*️ Aurora brightness (toggle) */
#define NL_AURORA 1.0

/* Rainy wind blow transparency (toggle) */
#define NL_RAIN_MIST_OPACITY 0.12

/* Chunk loading slide in animation (toggle) */
//#define NL_CHUNK_LOAD_ANIM 100.0

/* -------- CONFIG ENDS HERE ----------- */


/* these are config of different subpacks. dont change! */

#ifdef NO_WAVE_NO_FOG
	#define NO_WAVE
	#define NO_FOG
#endif

#ifdef NO_FOG
	#undef NL_FOG_TYPE
	#define NL_FOG_TYPE 0
#endif

#ifdef NO_WAVE
	#undef NL_PLANTS_WAVE
	#undef NL_LANTERN_WAVE
	#undef NL_UNDERWATER_WAVE
	#undef NL_WATER_WAVE
	#undef NL_RAIN_MIST_OPACITY
#endif

#ifdef CHUNK_ANIM
	#define NL_CHUNK_LOAD_ANIM 100.0
#endif

