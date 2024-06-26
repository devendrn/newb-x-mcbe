#ifndef NL_MAIN_H
#define NL_MAIN_H

/* Merges all functions into a single file and provides a global config.
 * shaderc will optimize away unused functions per material.
 */

// Global configuration
#include "config.h"

// Newb legacy functions
#include "functions/tonemap.h"
#include "functions/detection.h"
#include "functions/fog.h"
#include "functions/sky.h"
#include "functions/clouds.h"
#include "functions/lighting.h"
#include "functions/water.h"
#include "functions/rain.h"
#include "functions/wave.h"
#include "functions/glow.h"

#endif
