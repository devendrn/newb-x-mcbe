# Pack config

# Materials to compile by default
DEFAULT_MATERIALS="RenderChunk Clouds Sky EndSky LegacyCubemap Actor SunMoon"

# Subpacks:
#  OPTIONS   = Subpack options
#  NAMES     = Names/descriptions for options
#  MATERIALS = Materials to compile for options
SUBPACK_OPTIONS=(
  ROUNDED_CLOUDS
  CHUNK_ANIM
  NO_WAVE_NO_FOG
  NO_FOG
  NO_WAVE
  DEFAULT
)
SUBPACK_NAMES=(
  "Rounded Clouds"
  "Chunk loading animation"
  "No wave, No fog"
  "No fog"
  "No wave"
  "Default"
)
SUBPACK_MATERIALS=(
  "Clouds"
  "RenderChunk"
  "RenderChunk"
  "RenderChunk"
  "RenderChunk"
  ""
)

