(; program to build houses and cities

  ; see http://www.minecraftwiki.net/wiki/Data_values#Block_IDs for the block names
  (val house-blocks (list 1 4 5 7 14 15 16 17 20 21 22 24 35 41 42 43 45 47
                          48 49 56 57 73 74 87 89 98 112 121 23 124 125 129 133))
  (val nr-house-blocks (.size house-blocks))
  (def random-house-block [] (material (.apply house-blocks (random-int 0 nr-house-blocks))))

  ; build a pyramid!
  ; Cube -> Material -> Cube
  (defrec pyramid [c m]
    (cube:set-walls c m)
    (let (closed (lam [c] (or (<= (- (.maxX c) (.minX c)) 1) (<= (- (.maxZ c) (.minZ c)) 1))))
      (unless (closed c) (pyramid (.shiftY (.shrinkXZ c 1) 1) m))
    )
  )

  ; build a single house
  ; Location -> Int -> Int -> Int -> Material -> Material -> Material -> Cube
  (def build-house [start-point h w d floor-m walls-m roof-m]
    (let* ((c (.growUp (.expandZ (.expandX (new jcdc.pluginfactory.Cube start-point start-point) w) d) h))
           (build-ceiling (lam (c m) (pyramid (.expandXZ (.ceiling c) 1) m) c)))
      (begin
        (build-ceiling (cube:set-walls (cube:set-floor (cube:set-all c "air") floor-m) walls-m) roof-m)
        c
      )
    )
  )

  ; build a row of houses
  ; Location -> Int -> (Location -> Cube) -> Cube
  (defrec build-house-row [at nr-houses house-builder-f]
    (unless (eq? nr-houses 0)
      (let (c (house-builder-f at))
        ; TODO: 20 isnt right here. the houses could be bigger than 20 wide...
        (build-house-row
          (loc
            (+ (* 2 (.width c)) (.getX at))
            (.getY at)
            (.getZ at)
          )
          (- nr-houses 1)
          house-builder-f
        )
      )
    )
  )

  (def random-building [center-point
                        min-h max-h
                        min-w max-w
                        min-d max-d]
    (build-house
      center-point
      (random-int min-h max-h)
      (random-int min-w max-w)
      (random-int max-w max-d)
      (random-house-block)
      (random-house-block)
      (random-house-block)
    )
  )

  ; builds a skyscraper
  ; Location -> Cube
  (def build-skyscraper [l] (build-house l 50 8 10 "stone" "obsidian" "diamond_block"))
  (def build-random-skyscraper [l] (random-building l
      20 100 ; min-h max-h
       4  10 ; min-w max-w
       4  10 ; min-d max-d
  ))

  ; builds a house - {5 -> wood plank, 17 -> wood, 20 -> glass}
  ; Location -> Cube
  (def build-normal-house [l] (build-house l 4 3 3 "5" "17" "20"))

  ; builds a row of skyscrapers. not really a full city, yet.
  ; -> Cube
  (def city    [] (build-house-row XYZ 10 build-random-skyscraper))

  ; builds a row of little houses. could be considered a village.
  ; -> Cube
  (def village [] (build-house-row XYZ  6 build-normal-house))

  ; build a house, and then change the wall its walls every second for 100 seconds.
  (def living-house []
    (let (c (normal-house XYZ))
      (spawn 100 1 (lam (n) (cube:set-walls c (if (even n) "gold_block" "gold_ore"))))
    )
  )

  (def materials2 ()
    (fmap (lam (m) (+
      "(-> " (.name m) " " (.ordinal m) ")"
    )) (to-list (org.bukkit.Material/values)))
  )

  ; http://www.minecraftwiki.net/wiki/Data_values#Block_IDs
  ; reproduce with:
  ;org.bukkit.Material.values.foreach(m =>
  ;  println(s"""(-> "${m.name}"\t${m.ordinal})"""))
  (val materials (
    (-> "AIR"	0)
    (-> "STONE"	1)
    (-> "GRASS"	2)
    (-> "DIRT"	3)
    (-> "COBBLESTONE"	4)
    (-> "WOOD"	5)
    (-> "SAPLING"	6)
    (-> "BEDROCK"	7)
    (-> "WATER"	8)
    (-> "STATIONARY_WATER"	9)
    (-> "LAVA"	10)
    (-> "STATIONARY_LAVA"	11)
    (-> "SAND"	12)
    (-> "GRAVEL"	13)
    (-> "GOLD_ORE"	14)
    (-> "IRON_ORE"	15)
    (-> "COAL_ORE"	16)
    (-> "LOG"	17)
    (-> "LEAVES"	18)
    (-> "SPONGE"	19)
    (-> "GLASS"	20)
    (-> "LAPIS_ORE"	21)
    (-> "LAPIS_BLOCK"	22)
    (-> "DISPENSER"	23)
    (-> "SANDSTONE"	24)
    (-> "NOTE_BLOCK"	25)
    (-> "BED_BLOCK"	26)
    (-> "POWERED_RAIL"	27)
    (-> "DETECTOR_RAIL"	28)
    (-> "PISTON_STICKY_BASE"	29)
    (-> "WEB"	30)
    (-> "LONG_GRASS"	31)
    (-> "DEAD_BUSH"	32)
    (-> "PISTON_BASE"	33)
    (-> "PISTON_EXTENSION"	34)
    (-> "WOOL"	35)
    (-> "PISTON_MOVING_PIECE"	36)
    (-> "YELLOW_FLOWER"	37)
    (-> "RED_ROSE"	38)
    (-> "BROWN_MUSHROOM"	39)
    (-> "RED_MUSHROOM"	40)
    (-> "GOLD_BLOCK"	41)
    (-> "IRON_BLOCK"	42)
    (-> "DOUBLE_STEP"	43)
    (-> "STEP"	44)
    (-> "BRICK"	45)
    (-> "TNT"	46)
    (-> "BOOKSHELF"	47)
    (-> "MOSSY_COBBLESTONE"	48)
    (-> "OBSIDIAN"	49)
    (-> "TORCH"	50)
    (-> "FIRE"	51)
    (-> "MOB_SPAWNER"	52)
    (-> "WOOD_STAIRS"	53)
    (-> "CHEST"	54)
    (-> "REDSTONE_WIRE"	55)
    (-> "DIAMOND_ORE"	56)
    (-> "DIAMOND_BLOCK"	57)
    (-> "WORKBENCH"	58)
    (-> "CROPS"	59)
    (-> "SOIL"	60)
    (-> "FURNACE"	61)
    (-> "BURNING_FURNACE"	62)
    (-> "SIGN_POST"	63)
    (-> "WOODEN_DOOR"	64)
    (-> "LADDER"	65)
    (-> "RAILS"	66)
    (-> "COBBLESTONE_STAIRS"	67)
    (-> "WALL_SIGN"	68)
    (-> "LEVER"	69)
    (-> "STONE_PLATE"	70)
    (-> "IRON_DOOR_BLOCK"	71)
    (-> "WOOD_PLATE"	72)
    (-> "REDSTONE_ORE"	73)
    (-> "GLOWING_REDSTONE_ORE"	74)
    (-> "REDSTONE_TORCH_OFF"	75)
    (-> "REDSTONE_TORCH_ON"	76)
    (-> "STONE_BUTTON"	77)
    (-> "SNOW"	78)
    (-> "ICE"	79)
    (-> "SNOW_BLOCK"	80)
    (-> "CACTUS"	81)
    (-> "CLAY"	82)
    (-> "SUGAR_CANE_BLOCK"	83)
    (-> "JUKEBOX"	84)
    (-> "FENCE"	85)
    (-> "PUMPKIN"	86)
    (-> "NETHERRACK"	87)
    (-> "SOUL_SAND"	88)
    (-> "GLOWSTONE"	89)
    (-> "PORTAL"	90)
    (-> "JACK_O_LANTERN"	91)
    (-> "CAKE_BLOCK"	92)
    (-> "DIODE_BLOCK_OFF"	93)
    (-> "DIODE_BLOCK_ON"	94)
    (-> "LOCKED_CHEST"	95)
    (-> "TRAP_DOOR"	96)
    (-> "MONSTER_EGGS"	97)
    (-> "SMOOTH_BRICK"	98)
    (-> "HUGE_MUSHROOM_1"	99)
    (-> "HUGE_MUSHROOM_2"	100)
    (-> "IRON_FENCE"	101)
    (-> "THIN_GLASS"	102)
    (-> "MELON_BLOCK"	103)
    (-> "PUMPKIN_STEM"	104)
    (-> "MELON_STEM"	105)
    (-> "VINE"	106)
    (-> "FENCE_GATE"	107)
    (-> "BRICK_STAIRS"	108)
    (-> "SMOOTH_STAIRS"	109)
    (-> "MYCEL"	110)
    (-> "WATER_LILY"	111)
    (-> "NETHER_BRICK"	112)
    (-> "NETHER_FENCE"	113)
    (-> "NETHER_BRICK_STAIRS"	114)
    (-> "NETHER_WARTS"	115)
    (-> "ENCHANTMENT_TABLE"	116)
    (-> "BREWING_STAND"	117)
    (-> "CAULDRON"	118)
    (-> "ENDER_PORTAL"	119)
    (-> "ENDER_PORTAL_FRAME"	120)
    (-> "ENDER_STONE"	121)
    (-> "DRAGON_EGG"	122)
    (-> "REDSTONE_LAMP_OFF"	123)
    (-> "REDSTONE_LAMP_ON"	124)
    (-> "WOOD_DOUBLE_STEP"	125)
    (-> "WOOD_STEP"	126)
    (-> "COCOA"	127)
    (-> "SANDSTONE_STAIRS"	128)
    (-> "EMERALD_ORE"	129)
    (-> "ENDER_CHEST"	130)
    (-> "TRIPWIRE_HOOK"	131)
    (-> "TRIPWIRE"	132)
    (-> "EMERALD_BLOCK"	133)
    (-> "SPRUCE_WOOD_STAIRS"	134)
    (-> "BIRCH_WOOD_STAIRS"	135)
    (-> "JUNGLE_WOOD_STAIRS"	136)
    (-> "COMMAND"	137)
    (-> "BEACON"	138)
    (-> "COBBLE_WALL"	139)
    (-> "FLOWER_POT"	140)
    (-> "CARROT"	141)
    (-> "POTATO"	142)
    (-> "WOOD_BUTTON"	143)
    (-> "SKULL"	144)
    (-> "ANVIL"	145)
    (-> "IRON_SPADE"	146)
    (-> "IRON_PICKAXE"	147)
    (-> "IRON_AXE"	148)
    (-> "FLINT_AND_STEEL"	149)
    (-> "APPLE"	150)
    (-> "BOW"	151)
    (-> "ARROW"	152)
    (-> "COAL"	153)
    (-> "DIAMOND"	154)
    (-> "IRON_INGOT"	155)
    (-> "GOLD_INGOT"	156)
    (-> "IRON_SWORD"	157)
    (-> "WOOD_SWORD"	158)
    (-> "WOOD_SPADE"	159)
    (-> "WOOD_PICKAXE"	160)
    (-> "WOOD_AXE"	161)
    (-> "STONE_SWORD"	162)
    (-> "STONE_SPADE"	163)
    (-> "STONE_PICKAXE"	164)
    (-> "STONE_AXE"	165)
    (-> "DIAMOND_SWORD"	166)
    (-> "DIAMOND_SPADE"	167)
    (-> "DIAMOND_PICKAXE"	168)
    (-> "DIAMOND_AXE"	169)
    (-> "STICK"	170)
    (-> "BOWL"	171)
    (-> "MUSHROOM_SOUP"	172)
    (-> "GOLD_SWORD"	173)
    (-> "GOLD_SPADE"	174)
    (-> "GOLD_PICKAXE"	175)
    (-> "GOLD_AXE"	176)
    (-> "STRING"	177)
    (-> "FEATHER"	178)
    (-> "SULPHUR"	179)
    (-> "WOOD_HOE"	180)
    (-> "STONE_HOE"	181)
    (-> "IRON_HOE"	182)
    (-> "DIAMOND_HOE"	183)
    (-> "GOLD_HOE"	184)
    (-> "SEEDS"	185)
    (-> "WHEAT"	186)
    (-> "BREAD"	187)
    (-> "LEATHER_HELMET"	188)
    (-> "LEATHER_CHESTPLATE"	189)
    (-> "LEATHER_LEGGINGS"	190)
    (-> "LEATHER_BOOTS"	191)
    (-> "CHAINMAIL_HELMET"	192)
    (-> "CHAINMAIL_CHESTPLATE"	193)
    (-> "CHAINMAIL_LEGGINGS"	194)
    (-> "CHAINMAIL_BOOTS"	195)
    (-> "IRON_HELMET"	196)
    (-> "IRON_CHESTPLATE"	197)
    (-> "IRON_LEGGINGS"	198)
    (-> "IRON_BOOTS"	199)
    (-> "DIAMOND_HELMET"	200)
    (-> "DIAMOND_CHESTPLATE"	201)
    (-> "DIAMOND_LEGGINGS"	202)
    (-> "DIAMOND_BOOTS"	203)
    (-> "GOLD_HELMET"	204)
    (-> "GOLD_CHESTPLATE"	205)
    (-> "GOLD_LEGGINGS"	206)
    (-> "GOLD_BOOTS"	207)
    (-> "FLINT"	208)
    (-> "PORK"	209)
    (-> "GRILLED_PORK"	210)
    (-> "PAINTING"	211)
    (-> "GOLDEN_APPLE"	212)
    (-> "SIGN"	213)
    (-> "WOOD_DOOR"	214)
    (-> "BUCKET"	215)
    (-> "WATER_BUCKET"	216)
    (-> "LAVA_BUCKET"	217)
    (-> "MINECART"	218)
    (-> "SADDLE"	219)
    (-> "IRON_DOOR"	220)
    (-> "REDSTONE"	221)
    (-> "SNOW_BALL"	222)
    (-> "BOAT"	223)
    (-> "LEATHER"	224)
    (-> "MILK_BUCKET"	225)
    (-> "CLAY_BRICK"	226)
    (-> "CLAY_BALL"	227)
    (-> "SUGAR_CANE"	228)
    (-> "PAPER"	229)
    (-> "BOOK"	230)
    (-> "SLIME_BALL"	231)
    (-> "STORAGE_MINECART"	232)
    (-> "POWERED_MINECART"	233)
    (-> "EGG"	234)
    (-> "COMPASS"	235)
    (-> "FISHING_ROD"	236)
    (-> "WATCH"	237)
    (-> "GLOWSTONE_DUST"	238)
    (-> "RAW_FISH"	239)
    (-> "COOKED_FISH"	240)
    (-> "INK_SACK"	241)
    (-> "BONE"	242)
    (-> "SUGAR"	243)
    (-> "CAKE"	244)
    (-> "BED"	245)
    (-> "DIODE"	246)
    (-> "COOKIE"	247)
    (-> "MAP"	248)
    (-> "SHEARS"	249)
    (-> "MELON"	250)
    (-> "PUMPKIN_SEEDS"	251)
    (-> "MELON_SEEDS"	252)
    (-> "RAW_BEEF"	253)
    (-> "COOKED_BEEF"	254)
    (-> "RAW_CHICKEN"	255)
    (-> "COOKED_CHICKEN"	256)
    (-> "ROTTEN_FLESH"	257)
    (-> "ENDER_PEARL"	258)
    (-> "BLAZE_ROD"	259)
    (-> "GHAST_TEAR"	260)
    (-> "GOLD_NUGGET"	261)
    (-> "NETHER_STALK"	262)
    (-> "POTION"	263)
    (-> "GLASS_BOTTLE"	264)
    (-> "SPIDER_EYE"	265)
    (-> "FERMENTED_SPIDER_EYE"	266)
    (-> "BLAZE_POWDER"	267)
    (-> "MAGMA_CREAM"	268)
    (-> "BREWING_STAND_ITEM"	269)
    (-> "CAULDRON_ITEM"	270)
    (-> "EYE_OF_ENDER"	271)
    (-> "SPECKLED_MELON"	272)
    (-> "MONSTER_EGG"	273)
    (-> "EXP_BOTTLE"	274)
    (-> "FIREBALL"	275)
    (-> "BOOK_AND_QUILL"	276)
    (-> "WRITTEN_BOOK"	277)
    (-> "EMERALD"	278)
    (-> "ITEM_FRAME"	279)
    (-> "FLOWER_POT_ITEM"	280)
    (-> "CARROT_ITEM"	281)
    (-> "POTATO_ITEM"	282)
    (-> "BAKED_POTATO"	283)
    (-> "POISONOUS_POTATO"	284)
    (-> "EMPTY_MAP"	285)
    (-> "GOLDEN_CARROT"	286)
    (-> "SKULL_ITEM"	287)
    (-> "CARROT_STICK"	288)
    (-> "NETHER_STAR"	289)
    (-> "PUMPKIN_PIE"	290)
    (-> "GOLD_RECORD"	291)
    (-> "GREEN_RECORD"	292)
    (-> "RECORD_3"	293)
    (-> "RECORD_4"	294)
    (-> "RECORD_5"	295)
    (-> "RECORD_6"	296)
    (-> "RECORD_7"	297)
    (-> "RECORD_8"	298)
    (-> "RECORD_9"	299)
    (-> "RECORD_10"	300)
    (-> "RECORD_11"	301)
  ))

  (val cool-building-materials (list
    "STONE"
    "COBBLESTONE"
    "WOOD"
    "BEDROCK"
    "GOLD_ORE"
    "IRON_ORE"
    "COAL_ORE"
    "LOG"
    "GLASS"
    "LAPIS_ORE"
    "LAPIS_BLOCK"
    "DISPENSER"
    "SANDSTONE"
    "WOOL"
    "GOLD_BLOCK"
    "IRON_BLOCK"
    "DOUBLE_STEP"
    "BRICK"
    "BOOKSHELF"
    "MOSSY_COBBLESTONE"
    "OBSIDIAN"
    "DIAMOND_ORE"
    "DIAMOND_BLOCK"
    "REDSTONE_ORE"
    "GLOWING_REDSTONE_ORE"
    "NETHERRACK"
    "GLOWSTONE"
    "SMOOTH_BRICK"
    "NETHER_BRICK"
    "ENDER_STONE"
    "REDSTONE_LAMP_ON"
    "WOOD_DOUBLE_STEP"
    "EMERALD_ORE"
  ))
)

