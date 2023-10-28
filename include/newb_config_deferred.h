ifndef NL_CONFIG_H
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
