#define DEFAULT

// modified by pack script to build each subpacks

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
