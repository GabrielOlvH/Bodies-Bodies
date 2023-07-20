package me.steven.bodiesbodies.screen;

import me.steven.bodiesbodies.BodiesBodies;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.tools.Tool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathHistoryScreen extends Screen {

    protected int x;
    protected int y;
    protected int backgroundWidth;
    protected int backgroundHeight;
    public static final Identifier DEATH_HISTORY_BACKGROUND = new Identifier("bodiesbodies","textures/gui/death_history_background.png");
    private final List<DeathData> deaths;

    public DeathHistoryScreen(Text title, List<DeathData> deaths) {
        super(title);
        this.deaths = deaths;
    }

    private void showDeath(DeathData data, int index) {

        int yPos = this.y + this.backgroundHeight / 2 - (deaths.size() / 2 - index) * 23;
        int xPos = this.x + 5;
        this.addDrawable((context, mouseX, mouseY, delta) -> context.drawTexture(DEATH_HISTORY_BACKGROUND, xPos, yPos, 0f, 226f, 166, 23, 256, 256));

        TextWidget txt = new TextWidget(Text.literal("ID " + data.id()), textRenderer);
        txt.setPosition(xPos + 3, yPos + 7);
        this.addDrawable(txt);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Inventory"), button -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(data.id());
                    buf.writeString("vanilla");
                    ClientPlayNetworking.send(BodiesBodies.OPEN_DEAD_BODY_INV, buf);
                })
                .position(xPos + 166 - 63, yPos + 2)
                .size(60, 18)
                .tooltip(Tooltip.of(Text.literal("Click to open inventory")))
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Teleport"), button -> {
                    ChatScreen screen = new ChatScreen("/execute in " + data.dimension() + " run tp @p " + data.pos().getX() + " " + data.pos().getY() + " " + data.pos().getZ());
                    MinecraftClient.getInstance().setScreen(screen);
                })
                .position(xPos + 52, yPos + 2)
                .size(50, 18)
                .tooltip(Tooltip.of(Text.literal("Teleport to death location: \nX: " + data.pos().getX() + " Y: " + data.pos().getY() + " Z: " + data.pos().getZ() + " (" + data.dimension() + ")")))
                .build());
    }

    @Override
    protected void init() {
        super.init();
        this.backgroundWidth = 175;
        this.backgroundHeight = 223;
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
        for (int i = 0; i < deaths.size(); i++) {
            DeathData death = deaths.get(i);
            showDeath(death, i);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawText(this.textRenderer, Text.literal("Death History"), this.x + 8, this.y + 6, 4210752, false);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context) {
        context.drawTexture(DEATH_HISTORY_BACKGROUND, x, y, 0, 0.0F, 0.0F, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
