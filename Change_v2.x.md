#### **2.1.3:**
> | Veinator: | ---
> | --- | ---
> | _FIX_: | Solved an issue that would cause loss of ores when using Veinator and a tool that is below the ores harvest level.

#### **2.1.2:**
> | Core: | ---
> | --- | ---
> | _FIX_: | Identified an issue that caused the version checker to fail.
> | _CHANGE_: | Modified the Build script to update version checker files automatically.
> | _CHANGE_: | Renamed several classes to de-clutter the code a little.
> | _CHANGE_: | Modified build script to include cursegradle for auto submission to Curse Forge.

> | Cropinator: | NEW SuperMiner _sub-mod_
> | --- | ---
> | | Cropinator is a new sub mod that is designed to make Farming easier and the Hoe more useful.
> | | Moved Auto Hoe feature here and removed the need to hold a key in order to use it.  This means that when Cropinator is enabled and you right click a block of Grass/Dirt with a Hoe (or custom tool association), Cropinator will search for a valid water source and automatically convert an area into farmland.
> | | When right clicking any plant-able crop with a Hoe, all mature crops will be harvested and replanted.
> | | An optional "Harvest Seeds" option is available to allow you to gather seeds along with the mature crop.  This is turned off by default, meaning that only the crop will be dropped.

> | Excavator: | ---
> | --- | ---
> | _CHANGE_: | Modified Excavator to detect the different types of stone and only excavate matching the variant.
> | _CHANGE_: | Add an option to disable this new variation detection to allow previous functionality to be restored.  Default is new variation detection enabled.
> | _REMOVED_: | Auto Hoe feature has been removed from Excavator and moved to Cropinator.

> | Illuminator: | ---
> | --- | ---
> | _CHANGE_: | Improved the Torch placement keybind to work without the need to have torches on your hotbar.

> | Shaftanator: | ---
> | --- | ---
> | _FIX_: | Solved an issue where the MineVeins option wasn't mining ore veins correctly.
> | _FIX_: | Solved an issue that prevented Illuminator from lighting a shaft correctly.

> | Substitutor: | ---
> | --- | ---
> | _FIX_: | Solved an issue that caused Substitutor not to change to a valid tool if the current slot in empty.

> | Veinator: | ---
> | --- | ---
> | _FIX_: | Solved an issue that would cause loss of ores when using Veinator combined with Substitutor.

#### **2.1.1:**
> | Core: | ---
> | --- | ---
> | _FIX_: | Fixed an issue where third-party tools would be forgotten about after initial load.
> | _CHANGE_: | Added native Forge version checker support.
> | _MISC_: | Various other tidy ups and minor fixes.

> | Lumbinator: | ---
> | --- | ---
> | _CHANGE_: | Made some improvements to the Tree Detection code and tidied up the code a little.
> | _FIX_: | Fixed an ArrayIndexOutOfBoundsException caused by Lumbinator while destroying leaves without affecting tool durability.

> | Substitutor: | ---
> | --- | ---
> | _FIX_: | Solved an issue where Substitutor was swapping tools even tho the tool being used was valid.

> | Veinator: | ---
> | --- | ---
> | _FIX_: | Fixed Veinator crash that was caused by an incompatibility third-party mod Ores.

#### **2.1.0:**
> | Core: | ---
> | --- | ---
> | _CHANGE_: | Updated Item and Block IDs to match the latest IDs for Biomes-O-Plenty.

> | Captivator: | ---
> | --- | ---
> | _FIX_: | Solved issue where items were still being picked up by Captivator after the player has died.  This caused items to be lost after respawning.
> | _CHANGE_: | Stopped Captivator picking up items while sleeping.

> | Illuminator: | ---
> | --- | ---
> | _CHANGE_: | Added a new key-bind to allow Illuminator to Place Torches without having to change held item.

> | Lumbinator: | ---
> | --- | ---
> | _CHANGE_: | Rewrote the Tree Detection Algorithm in order to solve issues with third party trees.  Still not perfect as it's leaving some leaves behind, but much better than it was.
> | _CHANGE_: | Added code to ensure snow on top of trees is removed correctly.

> | Substitutor: | ---
> | --- | ---
> | _CHANGE_: | Modified Substitutor to not change held item if no sword is found when attacking mobs.
> | _CHANGE_: | Modified Substitutor to not change held item in Creative Mode.

> | Veinator: | ---
> | --- | ---
> | _FIX_: | Solved a NullPointerException error within Veinator while the game was being initialized along side supported mods.

#### **2.0.0:**
> | Core: | ---
> | --- | ---
> | _MERGE_: | Finally bit the bullet and merged all SuperMiner mods into a single mod rather than having multiples.
> | _CHANGE_: | Unified some of the duplicated code caused by the merging.
> | _CHANGE_: | Rearranged the code structure to be less messy and more compact, and renamed several classes.
> | _CHANGE_: | Had to adjust the block checking code to treat rotated blocks as the same block type, eg. Logs, Quarts Pillars etc.
> | _CHANGE_: | Made changes throughout all mods to allow more information to be translated into multiple languages.
> | _CHANGE_: | Adjusted the Enable/Disable toggle button code to update the Config file so that Enabled and Disabled states are persisted across game sessions. 

> | Captivator: | ---
> | --- | ---
> | _CHANGE_: | Added an option to control wither Captivator will pickup items while you are within any in-game GUI.  Default is True.
> | _CHANGE_: | Reordered config elements.

> | Excavator: | ---
> | --- | ---
> | _CHANGE_: | MAJOR rewrite of the excavation block detection code! Excavator now excavates a cube shaped hole based on the Block Radius and Block Limit.
> | _CHANGE_: | Adjusted the Maximum Block Radius and Block Limit to stop you from causing massive lag. Max Block Radius = 16 and Max Block Limit = 4096 ((16*16)*16)
> | _CHANGE_: | Adjusted the Minimum Block Radius and Block Limit. Min Block Radius = 3 and Min Block Limit = 27 ((3*3)*3)
> | _CHANGE_: | Added a new "single layer only" excavation toggle button. Default key binding is '\' (Backslash).
> | _FIX_: | Solved an issue that caused Sandstone and Quarts blocks to only return the same type of the block initially attacked. For example: a wall of Sand Stone, Smooth Sand Stone and Chiselled Sand Stone when broken using Excavator would result in a collection of the initial block broken and nothing else, eg. Chiselled Sand Stone.
> | _CHANGE_: | Reordered config elements.  

> | Illuminator: | ---
> | --- | ---
> | _CHANGE_: | Reordered config elements.

> | Lumbinator: | ---
> | --- | ---
> | _CHANGE_: | Improved the tree detection algorithm (again).
> | _CHANGE_: | Reordered config elements.

> | Shaftanator: | ---
> | --- | ---
> | _CHANGE_: | Reordered config elements.

> | Substitutor: | ---
> | --- | ---
> | _CHANGE_: | Added the Storage Drawers mod blocks to the default excluded blocks list.  (http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2198533)
> | _CHANGE_: | Reordered config elements.

> | Veinator: | ---
> | --- | ---
> | _CHANGE_: | Reordered config elements.