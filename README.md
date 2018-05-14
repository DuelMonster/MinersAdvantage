![](http://cf.way2muchnoise.eu/versions/For%20MC_291972_all.svg) ![](http://cf.way2muchnoise.eu/full_291972_Downloads.svg) [![Build Status](https://travis-ci.org/DuelMonster/MinersAdvantage.svg?branch=MC_1.12.2)](https://travis-ci.org/DuelMonster/MinersAdvantage)
## Overview
**MinersAdvantage** is a re-imagining of my previous mod 'SuperMiner'. It is a rewrite from the ground up that aims to solve all previous issues and to add additional features.

The original collection of components are still here however, they have just had minor name changes. All components have been re-developed from scratch, with the intention of greatly improving your Minecraft experience and to continue to make life easier...

*My original inspiration was fueled by several well known mods; namely - VeinMiner, TreeCapitator, AutoSwitch & NEI's Magent Mode. My thanks and admiration still go out to their respective developers and I shall be forever grateful to them for inspiring me to develop my own mods.*

Although the components and features list are, near enough, identical to SuperMiner, I felt that rather than releasing a new version, it warranted a complete name change due to the fact that only around 5% of the original code remains.

This decision has resulted in the birth of:
<p align="center"><img src="/src/main/resources/MinersAdvantage_Banner.png"/></p>

***

<p align="center">**Be So Kind and Support Me, help me pay for Stuffs and Ensure I can continue to Update my Mods...**</p>

[![](http://i.imgur.com/CAJuExT.png) **Support me via Patreon**](https://www.patreon.com/DuelMonster)
or
[![](https://www.paypalobjects.com/en_GB/i/btn/btn_donate_SM.gif) **via Paypal**](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=9VMJMWCLDM4DE)

***
## Details
**MAJOR** improvements in the way that blocks are harvested have been made in order to cure a very **nasty** bug that exists in SuperMiner which causes the client and/or server to crash. Due to the way SuperMiner was developed, it wasn't possible to solve the issue without a complete rewriting of the base code. There were also other areas that had become stagnant and difficult to improve upon.

During this re-development I took the opportunity to add some additional features that were either on my personal todo list or had been suggested by some of SuperMiners users.

Some of these additions are as follows:

- Customisable 'Blocks per Tick' and 'Tick Delay' options that allow you to speed up or slow down the harvesting of blocks.
- Improvements to the block harvesting algorithms have allowed me to add particle effects that appear while blocks are being harvested, giving you the added visual indication that MinersAdvantage is still processing an area.
- Huge multiplayer related improvements have been made to ensure that MinersAdvantage is much more server friendly than SuperMiner ever was.
- Multi Threaded worker processes:
  - This means that you can use all of MinerAdvantages components without waiting for Minecraft to recover from the dreadful lag spikes that were present with SuperMiner.
  - It also means that all MinerAdvantages components can be used simultaneously and executed multiple times without having to wait for one process to finish first.  If you want to dig a series of 2x1x64 sized strip mine with Shaftanation in the quickest time possible, MinerAdvantages will allow you to do it.
  - These worker processes have been designed to respect which player initiated which component, ensuring a server will never get one area confused with another.
- A players settings are synchronized with the server to ensure that custom options are respected per player and not confused with another players settings.
- Servers are now able to ( *optionally* ) override what components can be used by the players:
  - For example, if you disabled the Captivation component within the server config, all connected players would have Captivation disabled and would be unable to enable it.
- **All** component options can ( *optionally* ) be enforced by the server:
  - This allows server admins to restrict MinerAdvantages component settings to whatever they feel is best for their servers:
    - the number of blocks that can be harvested at one time,
    - the speed in which blocks are harvested to avoid server lag,
    - tools that can be used,
    - ores that can be harvested,
    - etc, etc.

## Backstory
<details open><summary>The re-development process has taken much longer than anticipated due to unforeseen IRL circumstances.</summary>
  
I was on track and hoping to have released v1 of MinersAdvantage back in November, but my daughter was taken into hospital on the 19th of October 2017 with suspected diabetes.  I spent five days in hospital with her ( *day and night* ) before we were allowed to return home again.

She was diagnosed with Type 1 Diabetes and is now totally dependent on Insulin.  The next few months were spent learning all we could about managing her diabetes; carb counting, the effects of and how to treat high and low blood sugars, dealing with mood swings and numerous other things.  We have had to buy quite a few gadgets and things to make life easier and my wife has done a wonderful job of creating low carb recipes, a blood sugar/insulin dose recording spreadsheet and various other time saving bits and bobs.
It's been a difficult time for us all mentally, emotionally and physically.  Her being so young ( *3 years at time of diagnosis* ) has also had it's issues with regards to what she eats, dealing with finger pricking and injections, having to treat highs or lows in the night, and trying to keep her calm and content.

She has now been fitted with an Insulin Pump which is slowly making life a little easier, but comes with more learning and tweaking.  So far her sugar levels have been so much better.

My wife and I are absolutely shattered and could do with a break from it all, but that's not going to happen for many years; not until our daughter is able to manage her diabetes herself.  Until then we must persevere and be strong for our little princess.  It has become a full time job for my wife, but it is taking it's toll on her.  I help out as much as I can when I'm not at work, but I do wish I could be with them 24 hours a day.

My work life is being affected and I have had to have a lot of time off for hospital visits, illness ( *both myself and family* ) and various other reasons.  My manager has been great and very supportive about it all, but the bosses higher up are starting to get a bit annoyed with it and have started questioning my commitment to the company.  I don't need that added stress on top of the already heavy emotional baggage I'm carrying.

So I guess what I'm really trying to say here is, if you find an issue with MinersAdvantage, please be patient with me.  I will get around to looking into any issues and, will do my best to reply to each and every message.
</details>

## MinersAdvantage component List:
<details><summary><strong>Captivation</strong> - Automatically pickup items and XP orbs. <em>[Magnet mode]</em></summary>
  
Captivation is a magnet mode mod that not only picks up items within a specified range but will also pick up any XP Orbs as well. The magnetic range is defaulted to a 16 block radius of the player in both horizontal and vertical axis. The range is user definable.

Comes with a configurable Whitelist / Blacklist. This list allows you to tell Captivation which items it should collect or which it should ignore. By using the "Is Whitelist" configuration option you can tell Captivation to treat the "Item IDs" list as a Whitelist (True) or a Blacklist (False). Default is True (Whitelist).

Features:
- Automatically picks up Items.
- Automatically picks up XP Orbs.
- Customizable Horizontal radius.
- Customizable Vertical radius.
- Customizable Whitelist / Blacklist.
</details>
  
<details><summary><strong>Cropination</strong> - makes Farming easier and the Hoe more useful.</summary>

**Auto Hoe:** It is now really easy to till your land ready for crop growing. Just right click the dirt/grass with your Hoe as normal and Cropination will automagically till the ground for you. Only the areas that are in range of a water source are tilled. If no water source is found, the Hoe will just act as normal.

**Auto Harvest:** When right clicking any plant-able crop with a Hoe, all mature crops will be harvested and replanted.
An optional "Harvest Seeds" option is avaliable to allow you to gather seeds along with the mature crop.  This is turned off by default, meaning that only the crop will be dropped.

Features:
- Automatically till an area of dirt that has a valid water source.
- Automatically harvest mature crops and replant.
- Optionalanly "Harvest Seeds".
</details>

<details><summary><strong>Excavation</strong> - Digs a big hole by breaking connected blocks, of the same type, within the specified range.</summary>

Excavation allows you to dig/mine big holes by breaking a single block. All blocks of the same type will be mined within the specified range. This range is user definable to allow you to fine tune the amount of land you want to harvest.
You are required to hold down a key while mining in order for it to work. Default button is the Grave key - ' **\`** '
There is a Tools list that can be defined so that you can restrict Excavation to only work with the specified tool(s). It is defaulted to an empty list, which allows all tools and items in your hand or just your hand.

If you have Illumination enabled and the 'Auto Illuminate' option is switched on (default is on), then Excavation will tell Illumination to place torches on the lowest level of the hole so you don't have to.

If you have Veination enabled and the 'Mine Ore Veins' option is switched on (default is off), then Excavation will tell Veination to mine the ore veins that are contained within the area being excavated.

**Single Layer Excavation:** This feature allows you to excavate an area 1 level at a time.
It uses the same block limit and radius to determine the area to excavate but will only dig at the height of the block initially destroyed. You are required to hold down a key while mining in order for it to work. Default button is the Backslash key - '**/**'

**Pathanation:** This feature allows you to automatically path an area using the Path block.

When holding the Excavation toggle key (default Grave ' \` ') and right clicking Grass ( or Dirt ) using a shovel, a path will be produced in the direction you are facing. Unlike the vanilla mechanic MinersAdvantage allows you to turn Dirt into Path blocks as well as Grass.
The 'Path Width' and 'Path Length' can be adjusted using the MinersAdvantage configuration.
Width has a minimum of 1 block and maximum of 16 blocks.
Length has a minimum of 3 blocks and maximum of 64 blocks.
The path will be laid up/down hill, depending on the terrain, as long as the next block isn't more than 1 block higher or lower.

Excavation also recognizes the different variations of Stone types (smooth, andesite, diorite, and granite).
This makes it easier to go around your world collecting a particular stone type for you latest, awesome build.  An option is available to disable the variation detection, causing Excavation to harvest all stone types.

_**Small Warning**: Keep an eye on your inventory space, as excavating will fill your inventory quickly..._

Features:
- Customizable Tools list.
- Customizable Blocks list.
- Customizable block limit.
- Customizable block radius.
- Customizable Path width.
- Customizable Path length.
- Optional Stone variation detection.  Default is True.
- Works with Illumination to light the shaft.
- Works with Veination to mine the ore veins that intersect the shaft.
</details>

<details><summary><strong>Illumination</strong> - Automatically lights up the area while you mine.</summary>

Illumination has been designed to allow you to mine away and not worry about lighting the area as you go. It checks the light level at your feet while you are breaking blocks and places a torch if the light level falls below the defined light level.

A default keybinding of 'V' is available which allows Torches to be placed without the need to swap the currently held item.

**NOTE:** *You are required to have torches within your inventory in order for this component to work.*

Features:

- Customizable Lowest Light Level.
- Torch placement keybind.  Default button is 'V'.
</details>

<details><summary><strong>Lumbination</strong> - Chop down entire trees.</summary>

Lumbination allows you to chop down an entire tree just like you would in real life.  It intelligently tries to identify the whole tree based on the size of the trunk and it's branches.
You can define whether or not the whole tree will be chopped down when chopping it higher than the bottom block. The default is no, which will leave any blocks below the chopping point.

Features:
- Customizable Tools list.
- Customizable Wood blocks list.
- Customizable Leaf blocks list.
</details>

<details><summary><strong>Shaftanation</strong> - Dig yourself a mine shaft.</summary>

Shaftanation was created to make strip mining quicker and easier by digging a mine shaft automagically.
Once the shaft is dug, all you need to do is to take a walk and collect the Ores.
By default Shaftanation will dig a mine shaft two blocks high, one block wide and sixteen blocks long. The size of the shaft is fully customizable to suit your needs.
Using the Mod options or the config file you can specify the shaft Height, Width and Length within the default limits.
You are required to hold down a key while mining in order for it to work. Default button is the 'Left Alt' key.

If you have Illumination enabled and the 'Auto Illuminate' option is switched on (default is on), then Shaftanation will tell Illumination to place torches along the length of the shaft so you don't have to.

If you have Veination enabled and the 'Mine Ore Veins' option is switched on (default is off), then Shaftanation will tell Veination to mine the ore veins that intersect the length of the shaft.

_**Small Warning:** Keep an eye on your inventory space, as shaft digging will fill your inventory quickly..._

Features:
- Customizable Shaft Height - Limit of between 2 & 16.
- Customizable Shaft Width - Limit of between 1 & 16.
- Customizable Shaft Length - Limit of between 4 & 128.
- Works with Illumination to light the shaft.
- Works with Veination to mine the ore veins that intersect the shaft.
</details>

<details><summary><strong>Substitution</strong> - Automatically select the correct tool while you mine.</summary>

Substitution will automatically switch to the best tool when you start breaking a block and by default will switch back to the previously held item after you're done.
Only those tools that are in your hotbar inventory slots will be used. The quickest/best tool will be selected based on the block being attacked and the best enchantments.
By default, a Silk Touch tool will be selected for silk touchable blocks, like Ores, Glowstone, Glass etc. If silk touch is unavaliable Fortune will be favoured.
A sword will be selected if you are attacking a mob. By default all passive mobs (Cows, Sheep, Pigs etc) will be ignored by Substitution, unless you disable the feature in the configuration.

Features:
- Optional: Switch back to the previously held item.
- Optional: Favour a SilkTouch tool over any other tool.
- Optional: Ignore other tools when deciding to substitute if the current Tool is valid for the block.
- Optional: Ignore Passive Mobs when deciding to substitute to a weapon.
</details>

<details><summary><strong>Veination</strong> - Mine an entire vein of ore.</summary>

Veination allows you to mine entire veins of ore.
There is no need to hold a button in order for the vein to be mined, it will be done automatically for you.

Features:
- Customizable Tools list.
- Customizable Ore blocks list.
</details>

## Reviews/Spotlights:
<details><summary><em>*None Yet*</em></summary>

</details>

## FAQ:

<details><summary><em>How do I install your mod?</em></summary>
  
- Download and install Minecraft Forge
- Download MinersAdvantage.
- Place the mod jar file into the mods folder of your Minecraft installation.
- Start Minecraft.

**NOTE:** MinersAdvantage must be installed on both Client and Server in order to work correctly.
</details>

<details><summary><em>Can I use your mod within my mod pack?</em></summary>
  
I give my _**full permission**_ to include MinersAdvantage in any mod pack, as long as the following conditions are met:
- Ensure you provide a link to MinerAdvantage.
- Properly credit me as the author - DuelMonster
- You mustn't make any money off of your mod pack.
- Be sure to remove these mods from your pack if I specifically request it.
</details>

<details><summary><em>Where can I find the source code for your mod?</em></summary>
  
The source is avaliable under GNU Lesser General Public License v3.0 and can be found within my GitHub repository.

[**GitHub.com/DuelMonster/MinersAdvantage**](https://github.com/DuelMonster/MinersAdvantage)
</details>

<details><summary><em>How do I compile the Source?</em></summary>
  
[**Compiling MinersAdvantage**](/.github/CONTRIBUTING.md)
</details>

<details><summary><em>How do I report an issue or contribute to the project?</em></summary>
  
[**Contributing MinersAdvantage**](/.github/CONTRIBUTING.md#contributing)
</details>
