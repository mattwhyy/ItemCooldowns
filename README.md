[Modrinth](https://modrinth.com/plugin/itemcooldowns) | [Discord](https://discordapp.com/users/555629040455909406) | [GitHub](https://github.com/mattwhyy/ItemCooldowns)
# ItemCooldowns
**ItemCooldowns** is a lightweight plugin that allows you to easily set the cooldown of **ANY** item!

**The plugin automatically detects the type of item and applies cooldowns based on its behavior.**

Whether it's a Totem of Undying, Trident, Bow, Crossbow, Shield, or even melee weapons, the plugin ensures that cooldowns are applied at the right time, only after the item is **actually used!**
## Configuration
```
cooldowns:
  # Example config
  # diamond_sword: 10
  # ender_pearl: 5
```
## Commands
```/setcooldown <item> <seconds>``` -
Allows you to set the cooldown easily in-game.

**Required permission:** 
```itemcooldowns.set```
- description: Allows setting item cooldowns
- default: op

```/getcooldown <item>``` -
Check the current cooldown of an item.

**Required permission:** 
```itemcooldowns.get```
- description: Allows checking an item's cooldown
- default: op

```/resetcooldown <item> OR <all>``` -
Allows you to reset the cooldown of an item, or all cooldowns.

**Required permission:** 
```itemcooldowns.reset```
- description: Allows resetting all cooldowns
- default: op
## Setup
Put the ```jar``` file into your server's **plugin folder**.

**Restart** (or reload) the server!
