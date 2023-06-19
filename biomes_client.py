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
"bamboo_jungle":                   [ 50,  85],
"bamboo_jungle_hills":             [ 40,  85],
"basalt_deltas":                   [ 50,  85],
"beach":                           [100,  96],
"birch_forest":                    [ 50,  85],
"birch_forest_hills":              [ 50,  85],
"cherry_grove":                    [ 50,  85],
"cold_beach":                      [ 67,  85],
"cold_ocean":                      [100,  85],
"cold_taiga":                      [ 67,  85],
"cold_taiga_hills":                [ 67,  85],
"cold_taiga_mutated":              [ 67,  85],
"crimson_forest":                  [ 50,  85],
"deep_cold_ocean":                 [100,  67],
"deep_frozen_ocean":               [100,  85],
"deep_lukewarm_ocean":             [100,  85],
"deep_ocean":                      [100,  67],
"deep_warm_ocean":                 [100,  85],
"default":                         [ 50,  85],
"desert":                          [ 67,  98],
"desert_hills":                    [100,  85],
"extreme_hills":                   [ 60,  85],
"extreme_hills_edge":              [ 50,  85],
"extreme_hills_mutated":           [ 40,  85],
"extreme_hills_plus_trees":        [ 50,  85],
"extreme_hills_plus_trees_mutated":[ 60,  85],
"flower_forest":                   [ 60,  85],
"forest":                          [ 50,  85],
"forest_hills":                    [ 50,  85],
"frozen_ocean":                    [100,  85],
"frozen_river":                    [100,  85],
"hell":                            [ 30,  85],
"ice_mountains":                   [100,  85],
"ice_plains":                      [100,  85],
"ice_plains_spikes":               [100,  85],
"jungle":                          [ 40,  85],
"jungle_edge":                     [ 30,  85],
"jungle_hills":                    [ 56,  85],
"jungle_mutated":                  [ 50,  85],
"lukewarm_ocean":                  [ 90,  85],
"mangrove_swamp":                  [ 20,  85],
"mega_spruce_taiga":               [ 50,  85],
"mega_spruce_taiga_mutated":       [ 50,  85],
"mega_taiga":                      [ 50,  85],
"mega_taiga_hills":                [ 50,  85],
"mega_taiga_mutated":              [ 50,  85],
"mesa":                            [ 70,  85],
"mesa_bryce":                      [ 50,  85],
"mesa_mutated":                    [ 50,  85],
"mesa_plateau":                    [ 55,  85],
"mesa_plateau_stone":              [ 60,  85],
"mushroom_island":                 [ 40,  67],
"mushroom_island_shore":           [ 25,  85],
"ocean":                           [100,  85],
"plains":                          [ 50,  85],
"river":                           [ 55,  85],
"roofed_forest":                   [ 50,  85],
"savanna":                         [ 46,  85],
"savanna_mutated":                 [ 40,  85],
"savanna_plateau":                 [ 45,  85],
"soulsand_valley":                 [ 80,  85],
"stone_beach":                     [ 65,  85],
"sunflower_plains":                [ 70,  85],
"swampland":                       [  0,  85],
"swampland_mutated":               [  5,  85],
"taiga":                           [ 60,  85],
"taiga_hills":                     [ 50,  85],
"taiga_mutated":                   [ 50,  85],
"the_end":                         [100,  90],
"warm_ocean":                      [100,  85],
"warped_forest":                   [ 50,  85],
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
