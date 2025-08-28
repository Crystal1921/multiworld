<img src="https://cdn.modrinth.com/data/cached_images/01d4b3f0a8d469b8d7b36030f2039007500b00f4.png" height="64" alt="Multiworld Logo">

# Multiworld ![](http://cf.way2muchnoise.eu/multiworld-mod.svg) ![](http://cf.way2muchnoise.eu/versions/multiworld-mod.svg)

Multiworld 模组 - 增加了创建和传送到多个世界的支持。

<a href="https://modrinth.com/mod/multiworld/versions?l=fabric"><img src="https://cdn.modrinth.com/data/cached_images/1b54a3f3b03745c57beaa1ab11d9d86b9222a41a.png" width="160"></a>
<a href="https://modrinth.com/mod/multiworld/versions?l=neoforge"><img src="https://cdn.modrinth.com/data/cached_images/a073c4dc33587010c5b7f0386d3df9e1b0eee3ed.png" width="160"></a>

## 指令用法:
| 指令         | 说明                                         | 示例 |
|--------------|----------------------------------------------|------|
| /mv          | 查看帮助                                     |      |
| /mv list     | 列出所有世界                                 |      |
| /mv tp       | 传送到某个世界                               | /mv tp minecraft:overworld |
| /mv spawn    | 传送到当前世界的出生点                       |      |
| /mv setspawn | 设置当前世界的出生点                         |      |
| /mv create   | 创建一个新世界                               | /mv create myLovelyWorld NORMAL -g=FLAT -s=1234 |
| /mv delete   | 删除一个世界（仅限控制台）                   | /mv delete myWorld |

#### 游戏规则 & 难度
| 指令           | 说明                                         | 示例                                |
|----------------|----------------------------------------------|-------------------------------------|
| /mv gamerule   | 为自定义世界设置游戏规则                     | /mv gamerule doDaylightCycle false  |
| /mv difficulty | 设置当前世界的难度                           | /mv difficulty EASY                 |

## 传送门 <img src="https://static.wikia.nocookie.net/minecraft_gamepedia/images/0/03/Nether_portal_%28animated%29.png/revision/latest?cb=20191114182303" width="128" float="right" align="right">
最新版本的 Multiworld 引入了传送门功能。
传送门可以通向目标，目标可以是一个世界 *(`myWorld`)*，另一个传送门 *(`p:myOtherPortal`)*，或者是精确坐标 *(`w:myWorld:0,0,0`)*。

要制作传送门，请使用 *`/mv portal wand`* 获得的传送门魔杖。持有魔杖物品时，类似于 WorldEdit，左键和右键点击方块以选择传送门框架的两个角。选中区域将在使用创建传送门指令时被用于生成传送门。

### 传送门指令
| 指令              | 说明                                       | 示例                                            |
|-------------------|--------------------------------------------|-------------------------------------------------|
| /mv portal        | 查看帮助                                   |                                                 |
| /mv portal create | 用魔杖选择的区域创建新传送门               | /mv create myPortal myWorld [isTransparent]     |
| /mv portal wand   | 获得传送门魔杖，用于选择传送门区域         | 选择传送门框架的黑曜石角                        |
| /mv portal info   | 列出所有传送门                             |                                                 |
| /mv portal remove | 移除一个传送门                             | /mv portal remove myPortal                      |

### 导入世界
你可以使用 `/mv import <路径>` 命令将已有世界导入 Multiworld，例如 `/mv import mcg:void1`。
世界必须位于服务器的 dimensions 文件夹下，例如 `world\dimensions\mcg\void1`。

导入的世界将会是虚空世界。

## 权限

Multiworld 支持 LuckPerms 或 CyberPerms。
拥有 `multiworld.admin` 权限或被 `/op` 授予管理员即可访问所有指令。

| 指令 | 权限 |
|------|------|
| /mv  | multiworld.cmd |
| /mv tp | multiworld.tp |
| /mv spawn | multiworld.spawn |
| /mv setspawn | multiworld.setspawn |
| /mv create <id> <dim> [-g=GENERATOR -s=SEED] | multiworld.create |
| /mv gamerule | multiworld.gamerule |
等等……

## 即将推出

- 自定义生成器支持

## 许可证与致谢

Multiworld 根据 [LGPL v3](LICENSE) 许可证分发。

注意：Multiworld 使用了 NucleoidMC 的 Fantasy 库用于运行时世界创建，亦为 LGPLv3 许可。

Forge 版本中，引用了 [Fabric Dimensions v1](https://github.com/FabricMC/fabric/blob/1.18/fabric-dimensions-v1/src/main/java/net/fabricmc/fabric/impl/dimension/FabricDimensionInternals.java#L45) 的部分代码，遵循 Apache License v2.0 许可。