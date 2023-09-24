#ifndef NL_CONFIG_H
#define NL_CONFIG_H
// line 3 reserved

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

/* Tonemap type */
#define NL_TONEMAP_TYPE 3 // 1:Exponential, 2:Reinhard, 3:Extended Reinhard, 4:ACES

/* Color correction options (toggle) */
//#define NL_EXPOSURE 1.3   // 0.5 (dark) - 2.0 (bright)
//#define NL_SATURATION 1.4 // 0.0 (grayscale) - 3.0 (super saturated)
//#define NL_TINT vec3(1.0,0.75,0.5)

/* Color correction contrast */
#define NL_CONSTRAST 0.75 // 0.3 (low) - 2.0 (high)

/* Terrain lighting */
#define NL_SUN_INTENSITY 2.95   // 0.5 (weak) - 5.0 (bright)
#define NL_TORCH_INTENSITY 1.0  // 0.5 (weak) - 3.0 (bright)
#define NL_NIGHT_BRIGHTNESS 0.1 // 0.0 (dark) - 2.0 (bright)
#define NL_CAVE_BRIGHTNESS  0.1 // 0.0 (dark) - 2.0 (bright)
#define NL_SHADOW_INTENSITY 0.7 // 0.0 (no shadow) - 1.0 (strong shadow)

/* Mist density */
#define NL_MIST_DENSITY 0.18 // 0.0 (no mist) - 1.0 (misty)

/* Fog fade */
#define NL_FOG_TYPE 2 // 0 (No fog), 1 (Vanilla fog), 2 (Smoother vanilla fog)

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
#define NL_DAY_SKY_CLARITY 0.3 // 0.0 (fogy sky) - 1.0 (clear sky)

/* Ore glow intensity */
#define NL_GLOW_TEX 2.2 // 0.4 (weak) - 8.0 (bright)

/* Plants wave intensity (toggle) */
#define NL_PLANTS_WAVE 0.04 // 0.02 (gentle) - 0.4 (violent)

/* Waving speed */
#define NL_WAVE_SPEED 2.8 // 0.5 (slow) - 5.0 (very fast)

/* Water */
#define NL_WATER_TRANSPARENCY 0.9 // 0.0 (transparent) - 1.0 (normal)
#define NL_WATER_BUMP 0.07        // 0.001 (plain) - 0.2 (bumpy water)
#define NL_WATER_TINT vec3(0.52,0.9,0.45)

/* Water wave (toggle) */
#define NL_WATER_WAVE

/* Fade water opacity with fog (toggle) */
#define NL_WATER_FOG_FADE

/* Water texture overlay */
#define NL_WATER_TEX_OPACITY 0.3 // 0.0 (plain water) - 1.0 (vanilla water texture)

/* Vnderwater lighting */
#define NL_UNDERWATER_BRIGHTNESS 0.8 // 0.0 (dark) - 3.0 (very bright)
#define NL_CAUSTIC_INTENSITY 1.9     // 0.5 (weak) - 5.0 (very bright)
#define NL_UNDERWATER_TINT vec3(0.9,1.0,0.9)

/* Underwater wave intensity (toggle) */
#define NL_UNDERWATER_WAVE 0.1 // 0.02 (subtle) - 0.6 (trippy)

/* Lantern swing intensity (toggle) */
#define NL_LANTERN_WAVE 0.16 // 0.05 (subtle) - 0.4 (large swing)

/* Cloud type */
#define NL_CLOUD_TYPE 2 // 0 (Vanilla), 1 (Soft), 2 (Rounded)

/* Vanilla cloud settings - make sure to remove clouds.png when using this */
#define NL_CLOUD0_THICKNESS 2.0      // 0.5 (slim) - 8.0 (fat)
#define NL_CLOUD0_RAIN_THICKNESS 4.0 // 0.5 (slim) - 8.0 (fat)

/* Soft cloud settings */
#define NL_CLOUD1_SCALE vec2(0.016, 0.022) // 0.003 (large) - 0.2 (tiny)
#define NL_CLOUD1_DEPTH 1.3                // 0.0 (no bump) - 10.0 (large bumps)
#define NL_CLOUD1_SPEED 0.04               // 0.0 (static) - 0.4 (fast moving)
#define NL_CLOUD1_DENSITY 0.54             // 0.1 (less clouds) - 0.8 (more clouds)
#define NL_CLOUD1_OPACITY 0.9              // 0.0 (invisible) - 1.0 (opaque)

/* Rounded cloud Settings */
#define NL_CLOUD2_THICKNESS 2.1      // 0.5 (slim) - 5.0 (fat)
#define NL_CLOUD2_RAIN_THICKNESS 2.5 // 0.5 (slim) - 5.0 (fat)
#define NL_CLOUD2_STEPS 5            // 3 (low quality) - 16 (high quality)
#define NL_CLOUD2_SCALE 0.033        // 0.003 (large) - 0.3 (tiny)
#define NL_CLOUD2_SHAPE 0.6          // 0.0 (round) - 1.0 (box)
#define NL_CLOUD2_DENSITY 5.0        // 1.0 (blurry) - 100.0 (sharp)
#define NL_CLOUD2_VELOCIY 0.8        // 0.0 (static) - 4.0 (very fast)

/*️ Aurora brightness (toggle) */
#define NL_AURORA 1.2 // 0.4 (dim) - 4.0 (very bright)

/*️ Aurora settings */
#define NL_AURORA_COL1 vec3(0.1,1.0,0.0)
#define NL_AURORA_COL2 vec3(0.1,0.0,1.0)
#define NL_AURORA_VELOCITY 0.03 // 0.0 (static) - 0.3 (very fast)
#define NL_AURORA_SCALE 0.04    // 0.002 (large) - 0.4 (tiny)
#define NL_AURORA_WIDTH 0.18    // 0.04 (thin line) - 0.4 (thick lines)

/* Rainy wind blow transparency (toggle) */
#define NL_RAIN_MIST_OPACITY 0.12 // 0.04 (very subtle) - 0.5 (thick)

/* Chunk loading slide in animation (toggle) */
//#define NL_CHUNK_LOAD_ANIM 100.0 // -600.0 (fall from top) - 600.0 (rise from bottom)

/* -------- CONFIG ENDS HERE ----------- */


/* these are config of different subpacks. dont change! */
/* names in pack.sh */

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

#ifdef ROUNDED_CLOUDS
  #undef NL_CLOUD_TYPE
  #define NL_CLOUD_TYPE 2
#endif

#endif
