# SuperMiner
SuperMiner - Making mining easier...

_Inspiration has been taken from several different mods already avaliable; namely - VeinMiner, TreeCapitator, AutoSwitch & NEI's Magent Mode. My thanks and admiration go out to there respective developers._

***

**Be So Kind and Support Me, help me pay for Stuffs and Ensure I can continue to Update my Mods...**

**[![](http://i.imgur.com/CAJuExT.png) Support me via Patreon](https://www.patreon.com/DuelMonster)**
or
[**Donate via Paypal** ![](https://www.paypalobjects.com/en_GB/i/scr/pixel.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=9VMJMWCLDM4DE)

***
## Captivator:
#### Automatically pickup items and XP orbs. _[Magnet mode]_
```
Captivator is a magnet mode mod that not only picks up items within a specified range but will also pick up any XP Orbs as well. The magnetic range is defaulted to a 16 block radius of the player in both horizontal and vertical axis. The range is user definable.

Comes with a configurable Whitelist / Blacklist. This list allows you to tell Captivator which items it should collect or which it should ignore. By using the "Is Whitelist" configuration option you can tell Captivator to treat the "Item IDs" list as a Whitelist (True) or a Blacklist (False). Default is True (Whitelist).
```
Features:

*   Automatically picks up Items.
*   Automatically picks up XP Orbs.
*   Customizable Horizontal radius.
*   Customizable Vertical radius.
*   Customizable Whitelist / Blacklist.

***
## Cropinator:
#### Makes Farming easier and the Hoe more useful.
```
Auto Hoe: It is now really easy to till your land ready for crop growing. Just right click the dirt/grass your your Hoe as normal and Excavator will automagically till the ground for you. Only the areas that are in range of a water source are tilled. If no water source is found, the Hoe will just act as normal.

Auto Harvest: When right clicking any plant-able crop with a Hoe, all mature crops will be harvested and replanted.
An optional "Harvest Seeds" option is avaliable to allow you to gather seeds along with the mature crop. This is turned off by default, meaning that only the crop will be dropped.
```
Features:

*   Automatically till an area of dirt that has a valid water source.
*   Automatically harvest mature crops and replant.
*   Optionalanly "Harvest Seeds".

***
## Excavator:
#### Digs a big hole by breaking connected blocks, of the same type, within the specified range.
```
Excavator allows you to dig/mine big holes by breaking a single block. All blocks of the same type will be mined within the specified range. This range is user definable to allow you to fine tune the amount of land you want to harvest.
You are required to hold down a key while mining in order for it to work. Default button is the Grave key - ' ` '
There is a Tools list that can be defined so that you can restrict Excavator to only work with the specified tool(s). It is defaulted to an empty list, which allows all tools and items in your hand or just your hand.

If Lumbinator and/or Veinator are enabled Excavator will ignore all Blocks that have been defined within their respective Block lists.

If you have Illuminator enabled and the 'Auto Illuminate' option is switched on (default is on), then Excavator will tell Illuminator to place torches on the lowest level of the hole so you don't have to.

Single Layer Excavation: This feature allows you to excavate an area 1 level at a time. It uses the same block limit and radius to determine the area to excavate but will only dig at the hieght of the block initially destroyed. You are required to hold down a key while mining in order for it to work. Default button is the Backslash key - '/'

2.1.0 added a new feature to Excavator that allows you to automatically path an area using the Path block. This feature is avaliable for Minecraft 1.9 and above.
When holding the Excavator toggle key (default Grave ' ` ') and right clicking Grass ( or Dirt ) using a shovel, a path will be produced in the direction you are facing. Unlike the vanilla mechanic SuperMiner allows you to turn Dirt into Path blocks as well as Grass.
The 'Path Width' and 'Path Length' can be adjusted using the SuperMiner configuation. Width has a minimum of 1 block and maximum of 16 blocks. Length has a minimum of 3 blocks and maximum of 64 blocks.
The path will be layed up/down hill, depending on the terain, as long as the next block isn't more than 1 block higher or lower.
```
_**Small Warning:** Keep an eye on your inventory space, as excavating will fill your inventory quickly..._
Features:

*   Customizable Tools list.
*   Customizable Blocks list.
*   Customizable block limit.
*   Customizable block radius.
*   Ignores Lumbinator and/or Veinator blocks, if mod is enabled.
*   Customizable Path width.
*   Customizable Path length.

***
## Illuminator:
#### Automatically lights up the area while you mine.
```
Illuminator has been designed to allow you to mine away and not worry about lighting the area as you go. It checks the light level at your feet while you are breaking blocks and places a torch if the light level falls below the defined light level.
In order for the mod to work you must have torches within your inventory. The torches do not need to be on your hotbar.

2.1.0 introduced a new Keybind to allow Torches to be placed without the need to swap the currently held item.
This was greatly improved in version 2.1.2.037 as you were required to have torches on you hotbar for it to work and it wouldn't always work if your current held item had it's own right click result (like a block).
Torch placement now only requires you to have torches within your inventory.
```
Features:

*   Customizable Lowest Light Level.
*   Torch placement keybind. Default button is 'V'.

***
## Lumbinator:
#### Chop down an entire tree.
```
Lumbinator allows you to chop down an entire tree just like you would in real life.
You can define whether or not the whole tree will be chopped down when chopping it higher than the bottom block. The default is no, which will leave any blocks below the chopping point.
```
Features:

*   Customizable Tools list.
*   Customizable Wood blocks list.
*   Customizable Leaf blocks list.

***
## Shaftanator:
#### Dig yourself a mine shaft.
```
Shaftanator was created to make strip mining quicker and easier by digging a mine shaft automagically.
Once the shaft is dug, all you need to do is to take a walk and collect the Ores.
By default Shaftanator will dig a mine shaft two blocks high, one block wide and sixteen blocks long. The size of the shaft is customisable.
Using the Mod options or the config file you can specify the shaft Height, Width and Length within the default limits.
You are required to hold down a key while mining in order for it to work. Default button is the 'Left Alt' key.

If you have Illuminator enabled and the 'Auto Illuminate' option is switched on (default is on), then Shaftanator will tell Illuminator to place torches along the length of the shaft so you don't have to.

If you have Veinator enabled and the 'Mine Ore Veins' option is switched on (default is off), then Shaftanator will tell Veinator to mine the ore veins along the length of the shaft.
```
_**Small Warning:** Keep an eye on your inventory space, as shaft digging will fill your inventory quickly..._
Features:

*   Customizable Shaft Height - Limit of between 2 & 16.
*   Customizable Shaft Width - Limit of between 1 & 16.
*   Customizable Shaft Length - Limit of between 4 & 128.
*   Works with Illuminator to light the shaft.
*   Works with Veinator to mine the ore veins along the shaft.

***
## Substitutor:
#### Automatically select the correct tool while you mine.
```
Substitutor is a Client side mod that will automatically switch to the best tool when you start breaking a block and by default will switch back to the previously held item after you're done.
Only those tools that are in your hotbar inventory slots will be used. The quickest/best tool will be selected based on the block being attacked and the best enchantments.
By default, a Silk Touch tool will be selected for silk touchable blocks, like Ores, Glowstone, Glass etc. If silk touch is unavaliable Fortune will be favoured.
A sword will be selected if you are attacking a mob. By default all passive mobs (Cows, Sheep, Pigs etc) will be ignored by Substitutor, unless you disable the feature in the mod options.

Substitutor is roughly based upon Thebombzen's AutoSwitch mod and a fair amount of Substituors code has been acquired/modified from AutoSwitch. BIG thanks go out to Thebombzen for the excellent mod.
```
Features:

*   Client Only Mod
*   Optional: Switch back to the previously held item.
*   Optional: Favour a SilkTouch tool over any other tool.
*   Optional: Ignore other tools when deciding to substitute if the current Tool is valid for the block.
*   Optional: Ignore Passive Mobs when deciding to substitute to a weapon.

***
## Veinator:
#### Mine an entire vein of ore.
```
Veinator allows you to mine entire veins of ore.
It works on the same principle as VienMiner without the need to hold a button in order for the vein to mined.
```
Features:

*   Customizable Tools list.
*   Customizable Ore blocks list.

***
## Reviews/Spotlights:
Huge thanks to **[MininMartin](https://www.youtube.com/channel/UCYp90PP1avzZtsyfMlMquVA)** for the first ever video. Spanish Speaking:
[![Spotlight](https://j.gifs.com/pY1og1.gif)](https://www.youtube.com/watch?v=HyVO_wlpZ9Q)

***
## FAQ:
What's the reasoning behind these mods I hear you ask...?
```
Well as much as I love VeinMiner, TreeCapitator, AutoSwitch & NEI's Magent Mode, I have always had the odd niggle with them here and there. So I set out to improve a few areas and dumb down others, as well as produce a few ideas of my own.
```
## Mod Pack Policy:
```
Any Mod pack requests will probably be ignored for the most part, but please note that I give my full permission to include the SuperMiner mods in any mod pack, as long as the following conditions are met:
```
*   Ensure you provide a link to this forum post.
*   Properly credit me as the author of these mods - DuelMonster
*   You mustn't make any money off of your mod pack.
*   Be sure to remove these mods from your pack if I specifically request it.

***