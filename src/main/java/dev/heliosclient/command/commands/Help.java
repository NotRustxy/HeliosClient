package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.heliosclient.command.Command;
import dev.heliosclient.command.CommandManager;
import dev.heliosclient.command.ModuleArgumentType;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

public class Help extends Command
{
	// TODO (ElBe): Add optional "command" argument to show specific help and help about arguments

    public Help() 
    {
		super("help", "Gives you a list of all of the commands", "c", "commands", "h");
	}

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) 
	{
		builder.then(argument("module", new ModuleArgumentType()).executes(context -> {
			ClientPlayerEntity player = mc.player;
			assert player != null;
			
			Module_ module = context.getArgument("module", Module_.class);

			ChatUtils.sendMsg(ColorUtils.bold + ColorUtils.yellow + module.name);
			ChatUtils.sendMsg(module.description);
			ChatUtils.sendMsg("");
			
			for (Setting setting : module.settings) {
				ChatUtils.sendMsg(ColorUtils.aqua + setting.name + ColorUtils.gray + ": " + setting.description);
			}

			return SINGLE_SUCCESS;
		}));
        builder.executes(context -> 
		{
			ChatUtils.sendMsg(ColorUtils.bold + ColorUtils.yellow + "Commands:");

			for (Command cmd : CommandManager.get().getAll()) {
				List<String> aliases = new ArrayList<>();

				for (String alias : cmd.getAliases()) aliases.add(alias);
				aliases.add(0, ColorUtils.bold + ColorUtils.aqua + cmd.getName());

				ChatUtils.sendMsg(ColorUtils.aqua + String.join(ColorUtils.reset + ", ", aliases) + ColorUtils.gray + ": " + cmd.getDescription());
			}
			return SINGLE_SUCCESS;
		});
        
    }
    
}
