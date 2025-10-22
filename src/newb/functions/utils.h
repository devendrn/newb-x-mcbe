#ifndef UTILS_H
#define UTILS_H

#define PI 3.141592
#define PI_HALF 1.570796
#define PI_QUART 0.785398

mat2 rmat2(float t) {
  float sint = sin(t);
  float cost = cos(t);
  return mtxFromRows(vec2(cost, -sint), vec2(sint, cost));
}

float degToRad(float t) { return 0.0174533*t; }

#endif
