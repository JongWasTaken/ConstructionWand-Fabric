package pw.smto.constructionwand.basics;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigClient
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue OPT_KEY;
    public static final ForgeConfigSpec.BooleanValue SHIFTOPT_MODE;
    public static final ForgeConfigSpec.BooleanValue SHIFTOPT_GUI;

    static {
        BUILDER.comment("This is the Client config for ConstructionWand.",
                "If you're not familiar with Forge's new split client/server config, let me explain:",
                "Client config is stored in the /config folder and only contains client specific settings like graphics and keybinds.",
                "Mod behavior is configured in the Server config, which is world-specific and thus located",
                "in the /saves/myworld/serverconfig folder. If you want to change the serverconfig for all",
                "new worlds, copy the config files in the /defaultconfigs folder.");

        BUILDER.push("keys");
        BUILDER.comment("Default key code of OPTKEY (Default: Left Control). Look up key codes under https://www.glfw.org/docs/3.3/group__keys.html");
        OPT_KEY = BUILDER.defineInRange("OptKey", 341, 0, 350);
        BUILDER.comment("Press SNEAK+OPTKEY instead of SNEAK for changing wand mode/direction lock");
        SHIFTOPT_MODE = BUILDER.define("ShiftOpt", false);
        BUILDER.comment("Press SNEAK+OPTKEY instead of SNEAK for opening wand GUI");
        SHIFTOPT_GUI = BUILDER.define("ShiftOptGUI", false);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
