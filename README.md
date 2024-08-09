# Construction Wand for Polymer

This is an unofficial port of [ConstructionWand](https://github.com/Theta-Dev/ConstructionWand) by Theta-Dev for Polymer on Minecraft 1.21.  
It can be installed to any Fabric server, and players will be able to use it without installing it to their clients, though the experience will be a lot better if they do.  

Due to being a Polymer port, certain features had to be altered:
- The wand menu was converted to an inventory menu, though players who also have the client installed will see the usual screen instead.
- The wand usage statistic is now tied to the item and can be reset at any time.
- Adding cores to the wand is no longer done at the crafting table, instead there is now an additional button in the wand menu.
- owo-lib is no longer needed. I might port these changes to the main branch down the line as well to minimize dependencies.

## Downloads
[![https://modrinth.com/mod/fabric-api](https://img.shields.io/badge/Requires_Fabric_API-white?style=for-the-badge&logo=modrinth&logoColor=black)](https://modrinth.com/mod/fabric-api)
  
[Modrinth](https://modrinth.com/mod/construction-wand-polymer)  
[CurseForge]()

## License
Credits go to [Theta-Dev](https://github.com/Theta-Dev/) for creating the original mod.

This project makes use of the following third-party libraries:

- [Polymer](https://github.com/Patbox/polymer) (LGPL-3.0 License)
- [forge-config-api-port](https://modrinth.com/mod/forge-config-api-port) (MPLv2 License), to keep config compatibility between both loaders (for now)

Just like the original release, this mod is licensed under the MIT license!
