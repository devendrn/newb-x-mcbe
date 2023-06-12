import json

# script to replace biomes_client.json values for water detection in newb

values = {
# name                             tint  lightness
"bamboo_jungle":                   [127, 191],
"bamboo_jungle_hills":             [127, 191],
"basalt_deltas":                   [127, 191],
"beach":                           [255, 255],
"birch_forest":                    [127, 191],
"birch_forest_hills":              [127, 191],
"cherry_grove":                    [127, 191],
"cold_beach":                      [170, 191],
"cold_ocean":                      [255, 191],
"cold_taiga":                      [170, 191],
"cold_taiga_hills":                [170, 191],
"cold_taiga_mutated":              [170, 191],
"crimson_forest":                  [127, 191],
"deep_cold_ocean":                 [255, 170],
"deep_frozen_ocean":               [255, 191],
"deep_lukewarm_ocean":             [255, 191],
"deep_ocean":                      [255, 170],
"deep_warm_ocean":                 [255, 191],
"default":                         [127, 191],
"desert":                          [170, 251],
"desert_hills":                    [255, 191],
"extreme_hills":                   [127, 191],
"extreme_hills_edge":              [127, 191],
"extreme_hills_mutated":           [127, 191],
"extreme_hills_plus_trees":        [127, 191],
"extreme_hills_plus_trees_mutated":[127, 191],
"flower_forest":                   [127, 191],
"forest":                          [127, 191],
"forest_hills":                    [127, 191],
"frozen_ocean":                    [255, 191],
"frozen_river":                    [255, 191],
"hell":                            [127, 191],
"ice_mountains":                   [255, 191],
"ice_plains":                      [255, 191],
"ice_plains_spikes":               [255, 191],
"jungle":                          [127, 191],
"jungle_edge":                     [127, 191],
"jungle_hills":                    [127, 191],
"jungle_mutated":                  [127, 191],
"lukewarm_ocean":                  [255, 191],
"mangrove_swamp":                  [127, 191],
"mega_spruce_taiga":               [127, 191],
"mega_spruce_taiga_mutated":       [127, 191],
"mega_taiga":                      [127, 191],
"mega_taiga_hills":                [127, 191],
"mega_taiga_mutated":              [127, 191],
"mesa":                            [127, 191],
"mesa_bryce":                      [127, 191],
"mesa_mutated":                    [127, 191],
"mesa_plateau":                    [127, 191],
"mesa_plateau_stone":              [127, 191],
"mushroom_island":                 [127, 170],
"mushroom_island_shore":           [127, 191],
"ocean":                           [255, 191],
"plains":                          [127, 191],
"river":                           [127, 191],
"roofed_forest":                   [127, 191],
"savanna":                         [127, 191],
"savanna_mutated":                 [127, 191],
"savanna_plateau":                 [127, 191],
"soulsand_valley":                 [127, 191],
"stone_beach":                     [127, 191],
"sunflower_plains":                [127, 191],
"swampland":                       [  0, 191],
"swampland_mutated":               [  0, 191],
"taiga":                           [127, 191],
"taiga_hills":                     [127, 191],
"taiga_mutated":                   [127, 191],
"the_end":                         [255, 191],
"warm_ocean":                      [255, 191],
"warped_forest":                   [127, 191],
}

json_file = "pack/biomes_client.json"

failed = False

with open(json_file, 'r') as biomes_file:
    data = json.load(biomes_file)
    biomes = data["biomes"]

    for i in biomes:
        if i in values:
            p = values[i]
            new_color = f'#{p[0]:0>2X}{p[1]:0>2X}00'
            biomes[i]["water_surface_color"] = new_color
            continue
        else:
            failed = True
            print(i, "not in values")

    if failed:
        print("biomes_client.json untouched:  Fix values first!")
        quit()

    biomes["the_end"]["fog_identifier"] = "newb:fog_the_end"
    biomes["default"]["fog_identifier"] = "newb:fog_default"

    data["biomes"] = biomes

with open(json_file, 'w') as biomes_file:
    json.dump(data, biomes_file, indent=2)

print("biomes_client.json updated")
