package dev.walsted.wintertodt;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;

import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class GameObjectOverlay extends OverlayPanel  {
    private final WintertodtExPlugin plugin;
    private final WintertodtExConfig wintertodtConfig;

    @Inject
    private GameObjectOverlay(WintertodtExPlugin plugin, WintertodtExConfig wintertodtConfig)
    {
        super(plugin);
        this.plugin = plugin;
        this.wintertodtConfig = wintertodtConfig;
        setPosition(OverlayPosition.DYNAMIC);
        addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Brazier overlay");
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isInWintertodt() || !wintertodtConfig.showOverlay())
        {
            return null;
        }
        var brazier = plugin.getClosets_brazier();
        var root = plugin.getClosets_root();
        if(brazier != null)
            renderGameObjects(graphics, brazier.getClickbox(), new Color(0x00, 0xff, 0x00, 1));
        if(root != null )
            renderGameObjects(graphics, root.getClickbox(), new Color(0x00, 0xff, 0x00, 1));
        return super.render(graphics);
    }

    private void renderGameObjects(Graphics2D graphics, Shape s, Color color)
    {
        if (s != null)
        {
            graphics.setColor(color);
            graphics.draw(s);
            var color2 = new Color((color.getRed() /255), (color.getGreen() /255), (color.getBlue()/255), .5f);
            graphics.setColor(color2);
            graphics.fill(s);
        }

    }
}
