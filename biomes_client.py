import json

# script to replace biomes_client.json values for water detection in newb

# tint
#   0        50       100
# swamp -> normal -> ocean

# lightness
# 0 - 100

# detection
# 0

values = {
# name                             tint  lightness
"bamboo_jungle":                   [ 50,  75],
"bamboo_jungle_hills":             [ 50,  75],
"basalt_deltas":                   [ 50,  75],
"beach":                           [100, 100],
"birch_forest":                    [ 50,  75],
"birch_forest_hills":              [ 50,  75],
"cherry_grove":                    [ 50,  75],
"cold_beach":                      [ 67,  75],
"cold_ocean":                      [100,  75],
"cold_taiga":                      [ 67,  75],
"cold_taiga_hills":                [ 67,  75],
"cold_taiga_mutated":              [ 67,  75],
"crimson_forest":                  [ 50,  75],
"deep_cold_ocean":                 [100,  67],
"deep_frozen_ocean":               [100,  75],
"deep_lukewarm_ocean":             [100,  75],
"deep_ocean":                      [100,  67],
"deep_warm_ocean":                 [100,  75],
"default":                         [ 50,  75],
"desert":                          [ 67,  98],
"desert_hills":                    [100,  75],
"extreme_hills":                   [ 50,  75],
"extreme_hills_edge":              [ 50,  75],
"extreme_hills_mutated":           [ 50,  75],
"extreme_hills_plus_trees":        [ 50,  75],
"extreme_hills_plus_trees_mutated":[ 50,  75],
"flower_forest":                   [ 50,  75],
"forest":                          [ 50,  75],
"forest_hills":                    [ 50,  75],
"frozen_ocean":                    [100,  75],
"frozen_river":                    [100,  75],
"hell":                            [ 50,  75],
"ice_mountains":                   [100,  75],
"ice_plains":                      [100,  75],
"ice_plains_spikes":               [100,  75],
"jungle":                          [ 50,  75],
"jungle_edge":                     [ 50,  75],
"jungle_hills":                    [ 50,  75],
"jungle_mutated":                  [ 50,  75],
"lukewarm_ocean":                  [100,  75],
"mangrove_swamp":                  [ 50,  75],
"mega_spruce_taiga":               [ 50,  75],
"mega_spruce_taiga_mutated":       [ 50,  75],
"mega_taiga":                      [ 50,  75],
"mega_taiga_hills":                [ 50,  75],
"mega_taiga_mutated":              [ 50,  75],
"mesa":                            [ 50,  75],
"mesa_bryce":                      [ 50,  75],
"mesa_mutated":                    [ 50,  75],
"mesa_plateau":                    [ 50,  75],
"mesa_plateau_stone":              [ 50,  75],
"mushroom_island":                 [ 50,  67],
"mushroom_island_shore":           [ 50,  75],
"ocean":                           [100,  75],
"plains":                          [ 50,  75],
"river":                           [ 50,  75],
"roofed_forest":                   [ 50,  75],
"savanna":                         [ 50,  75],
"savanna_mutated":                 [ 50,  75],
"savanna_plateau":                 [ 50,  75],
"soulsand_valley":                 [ 50,  75],
"stone_beach":                     [ 50,  75],
"sunflower_plains":                [ 50,  75],
"swampland":                       [  0,  75],
"swampland_mutated":               [  0,  75],
"taiga":                           [ 50,  75],
"taiga_hills":                     [ 50,  75],
"taiga_mutated":                   [ 50,  75],
"the_end":                         [100,  75],
"warm_ocean":                      [100,  75],
"warped_forest":                   [ 50,  75],
}

json_file = "pack/biomes_client.json"

col_id = "water_surface_color"
alpha_id = "water_surface_transparency"

failed = False

with open(json_file, 'r') as biomes_file:
    data = json.load(biomes_file)
    biomes = data["biomes"]

    for i in biomes:
        if i in values:
            p = values[i]
            p[0] = int((p[0]*255)/100)
            p[1] = int((p[1]*255)/100)

            # red = tint
            # green = lightness
            # blue = 0 for detection
            new_color = f'#{p[0]:0>2X}{p[1]:0>2X}00'
            biomes[i][col_id] = new_color

            # alpha < 0.95 for detection
            if alpha_id in biomes[i]:
                if biomes[i][alpha_id] > 0.94:
                    biomes[i][alpha_id] = 0.94;
        else:
            failed = True
            print(i, "not in values")

    if failed:
        print(json_file, "untouched:  Fix values first!")
        quit()

    biomes["the_end"]["fog_identifier"] = "newb:fog_the_end"
    biomes["default"]["fog_identifier"] = "newb:fog_default"

    data["biomes"] = biomes

with open(json_file, 'w') as biomes_file:
    json.dump(data, biomes_file, indent=2)

print("biomes_client.json updated")
