<img src="https://cdn.modrinth.com/data/cached_images/01d4b3f0a8d469b8d7b36030f2039007500b00f4.png" height="64" alt="Multiworld Logo">

# Multiworld ![](http://cf.way2muchnoise.eu/multiworld-mod.svg)

Multiworld 模组 - 增加了创建和传送到多个世界的支持。

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

#### 创建世界
/mv create myid:myvalue NORMAL -g=VOID

- `myid:myvalue` 是你想要创建的世界的唯一标识符。
- `NORMAL` 是维度类型，可以是 `NORMAL`（主世界）、`NETHER`（下界）或 `THE_END`（末地）。
- `-g=GENERATOR` 是可选参数，用于指定生成器类型，例如 `FLAT`（平坦）、`VOID`（虚空）等。
- `-s=SEED` 是可选参数，用于指定世界种子。
- `-r=myid:myvalue` 是可选参数，用于指定一个现有世界的世界规则作为新世界的模板。

这里末尾的 -g 和 -s 参数是可选的，每个参数直接用空格分开，如果同时使用多个参数的指令就像这样

*(`/mv create myid:myvalue NORMAL -g=VOID -r=minecraft:overworld`)*

#### 游戏规则 & 难度
| 指令           | 说明                                         | 示例                                |
|----------------|----------------------------------------------|-------------------------------------|
| /mv gamerule   | 为自定义世界设置游戏规则                     | /mv gamerule doDaylightCycle false  |
| /mv difficulty | 设置当前世界的难度                           | /mv difficulty EASY                 |

## 传送门
最新版本的 Multiworld 引入了传送门功能。
传送门可以通向目标，目标可以是一个世界 *(`myWorld`)*，另一个传送门 *(`p:myOtherPortal`)*，或者是精确坐标 *(`w:myWorld:0,0,0`)*。

要制作传送门，请使用 *`/mv portal wand`* 获得的传送门魔杖。持有魔杖物品时，类似于 WorldEdit，左键和右键点击方块以选择传送门框架的两个角。选中区域将在使用创建传送门指令时被用于生成传送门。

### 传送门指令
| 指令                          | 说明                | 示例                                            |
|-----------------------------|-------------------|-------------------------------------------------|
| /mv portal                  | 查看帮助              |                                                 |
| /mv portal create           | 用魔杖选择的区域创建新传送门    | /mv create myPortal myWorld [isTransparent]     |
| /mv portal wand             | 获得传送门魔杖，用于选择传送门区域 | 选择传送门框架的黑曜石角                        |
| /mv portal list             | 列出所有传送门           |                                                 |
| /mv portal list [name/page] | 列出对应传送门/第x页传送门    |                                                 |
| /mv portal remove           | 移除一个传送门           | /mv portal remove myPortal                      |

当isTransparent为true时，会在门框处生成透明不可见的传送门方块。接触这些方块就可以触发传送

传送本质上就是截断传送门方块的检测，在世界传送门列表里面遍历是否存在对应的传送门

手持传送门探测器时，会高亮此维度的传送门

### 导入世界
你可以使用 `/mv import <路径>` 命令将已有世界导入 Multiworld，例如 `/mv import mcg:void1`。
世界必须位于服务器的 dimensions 文件夹下，例如 `world\dimensions\mcg\void1`。

导入的世界将会是虚空世界。

### 迁移数据
| 指令                               | 说明                  | 示例                                            |
|----------------------------------|---------------------|-------------------------------------------------|
| /mv migrate gamerule [from] [to] | 将第一个世界的游戏规则复制到第二个世界 |   /mv migrate gamerule minecraft:overworld mcg:test  |


### 其他指令
| 指令                  | 说明             | 示例            |
|---------------------|----------------|---------------|
| /setwarp name [pos] | 设置传送点          | /setwarp test |
| /warp name [player] | 传送到目标传送点       | /warp test    |
| /delwarp            | 删除传送点          | /delwarp test |
| /back               | 返回上次传送位置       |               |

需要注意的是，传送点名称是特殊匹配的，如果名字中没有空格，则<name>会直接匹配中间的内容

如果名字中间需要空格，则需要使用引号括起来，例如 `/setwarp "my home" ~ ~ ~`，当然没有空格可以使用引号标明需要的是内部内容也是可以的，例如 `/setwarp "test" ~ ~ ~`，这样设置的传送点名称就是test
同样的，delwarp和warp也使用这种用法

## 许可证与致谢

Multiworld 根据 [LGPL v3](LICENSE) 许可证分发。

注意：Multiworld 使用了 NucleoidMC 的 Fantasy 库用于运行时世界创建，亦为 LGPLv3 许可。

Forge 版本中，引用了 [Fabric Dimensions v1](https://github.com/FabricMC/fabric/blob/1.18/fabric-dimensions-v1/src/main/java/net/fabricmc/fabric/impl/dimension/FabricDimensionInternals.java#L45) 的部分代码，遵循 Apache License v2.0 许可。