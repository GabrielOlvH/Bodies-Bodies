package me.steven.bodiesbodies.screen;

import me.steven.bodiesbodies.BodiesBodies;
import me.steven.bodiesbodies.data.DeadBodyData;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.entity.DeadBodyEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class VanillaDeadBodyInventoryScreen extends HandledScreen<VanillaDeadBodyInventoryScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("bodiesbodies", "textures/gui/dead_body_inventory.png");
    private final DeathData deathData;

    public VanillaDeadBodyInventoryScreen(VanillaDeadBodyInventoryScreenHandler handler, PlayerInventory inventory, Text title, DeathData deathData) {
        super(handler, inventory, title);
        this.backgroundWidth = 175;
        this.backgroundHeight = 223;
        this.playerInventoryTitleY = 130;
        this.deathData = deathData;
    }

    @Override
    protected void init() {
        super.init();
        TexturedButtonWidget btn = new TexturedButtonWidget(this.x + this.backgroundWidth - 5 - 20, this.y + 5, 20, 18, 178, 0, 19, BACKGROUND_TEXTURE, (button) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(deathData.id());
            ClientPlayNetworking.send(BodiesBodies.TRANSFER_ALL_ITEMS_PACKET, buf);
        });
        btn.setTooltip(Tooltip.of(Text.literal("Transfer all items")));
        this.addDrawableChild(btn);

        List<DeadBodyData> savedData = deathData.savedData();
        for (int i = 0; i < savedData.size(); i++) {
            DeadBodyData data = savedData.get(i);
            TexturedButtonWidget dataBtn = new TexturedButtonWidget(this.x + this.backgroundWidth + 2, this.y + i * 22, 65, 20, 178, 38, 21, BACKGROUND_TEXTURE, (button) -> {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(deathData.id());
                buf.writeString(data.getId());
                ClientPlayNetworking.send(BodiesBodies.OPEN_DEAD_BODY_INV, buf);
            }) {
                @Override
                public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                    super.renderButton(context, mouseX, mouseY, delta);
                    MutableText txt = Text.translatable("bodiesbodies.data." + data.getId());
                    context.drawText(textRenderer, txt, this.getX() + this.getWidth() / 2 - textRenderer.getWidth(txt)/2, this.getY() + this.getHeight()/2 - textRenderer.fontHeight/2, -1, false);
                }
            };
            dataBtn.setTooltip(Tooltip.of(Text.translatable("bodiesbodies.data." + data.getId())));
            this.addDrawableChild(dataBtn);
        }
    }

    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}
