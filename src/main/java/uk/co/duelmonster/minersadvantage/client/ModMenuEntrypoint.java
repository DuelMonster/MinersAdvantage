package uk.co.duelmonster.minersadvantage.client;

//? if fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuEntrypoint implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MinersAdvantageConfigScreen::create;
    }
}
//?} else {
/*
// This class is Fabric-only.
*/ //?}
