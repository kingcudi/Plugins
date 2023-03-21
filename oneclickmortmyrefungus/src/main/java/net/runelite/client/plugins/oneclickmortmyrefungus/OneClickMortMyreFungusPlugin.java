package net.runelite.client.plugins.oneclickmortmyrefungus;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "One Click Mort Myre Fungus",
        description = "POH Fairy ring/butler method only.",
        tags = {"one", "click","mort","myre","fungus","mushrooms"},
        enabledByDefault = false
)
public class OneClickMortMyreFungusPlugin extends Plugin {
    @Inject
    private Client client;

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if(client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        if(!(isInPOH() || isInSwamp())) return;
        String text;
        {
            text =  "<col=00ff00>One Click Mort Myre Fungus";
        }

        client.insertMenuItem(
                text,
                "",
                MenuAction.UNKNOWN.getId(),
                0,
                0,
                0,
                true);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Mort Myre Fungus")) {
            handleClick(event);
        }
    }

    private void handleClick(MenuOptionClicked event) {
        if (isInSwamp()) {
            if (getEmptySlots() == 0) {
                event.setMenuEntry(teleportToHouseMES());
                return;
            }
            if (gatherFungusMES() != null) {
                event.setMenuEntry(gatherFungusMES());
                return;
            }
            if (shouldCastBloom()) {
                event.setMenuEntry(castBloomMES());
                return;
            }
            walktoCastingTile();
            return;
        }

        if (isInPOH())
        {
            if (client.getWidget(219, 1) != null && client.getWidget(219, 1).getChild(1).getText().contains("Take to bank")) {
                event.setMenuEntry(sendToBankMES());
                return;
            }

            if (client.getWidget(231, 5) != null) {
                event.setMenuEntry(clickContinueMES());
                return;
            }

            if (client.getWidget(370, 20) != null && client.getWidget(370, 20).getChild(3) != null) {
                event.setMenuEntry(callButlerMES());
                return;
            }
            if (getEmptySlots()==0)
            {
                if (client.getWidget(116,8)!=null)
                {
                    event.setMenuEntry(houseOptionsMES());
                    return;
                }
            }
            if (client.getBoostedSkillLevel(Skill.PRAYER) < 40) {
                event.setMenuEntry(drinkFromPoolMES());
                return;
            }
            event.setMenuEntry(useFairyRingMES());
        }
    }

    private void walkTile(WorldPoint worldpoint) {
        int x = worldpoint.getX() - client.getBaseX();
        int y = worldpoint.getY() - client.getBaseY();
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    private void walktoCastingTile() {
        WorldPoint worldpoint = new WorldPoint(3472,3419,0);
        walkTile(worldpoint);
    }

    private MenuEntry castBloomMES() {
        List<Integer> itemIDs =Arrays.asList(ItemID.SILVER_SICKLE_B,ItemID.BLISTERWOOD_FLAIL,ItemID.IVANDIS_FLAIL);
        for (Integer itemID : itemIDs )
        {
            if (getInventoryItem(itemID)!=null)
            {
                Widget sickle = getInventoryItem(itemID);
                return createMenuEntry(4, MenuAction.CC_OP, sickle.getIndex(), WidgetInfo.INVENTORY.getId(), false);
            }
        }
        return null;
    }

    private MenuEntry gatherFungusMES() {
        WorldPoint worldpoint = new WorldPoint(3473,3420,0); //checks if mushrooms exist on NW log as this automatically paths optimally and saves 1t!!!
        GameObject fungiOnLog =new GameObjectQuery()
                .idEquals(3509)
                .result(client)
                .stream()
                .filter(gameObject -> gameObject.getWorldLocation().distanceTo(worldpoint)==0)
                .findAny().orElse(null);

        if (fungiOnLog==null){
            fungiOnLog = getGameObject(3509);
        }

        if (fungiOnLog==null) return null;
        return createMenuEntry(fungiOnLog.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(fungiOnLog).getX(), getLocation(fungiOnLog).getY(), false);
    }

    private MenuEntry drinkFromPoolMES() {
        GameObject pool = null;
        List<Integer> Pools = Arrays.asList(ObjectID.POOL_OF_REJUVENATION,ObjectID.FANCY_POOL_OF_REJUVENATION,ObjectID.ORNATE_POOL_OF_REJUVENATION);
        for (Integer poolID : Pools)
        {
            if (getGameObject(poolID)!=null)
            {
                pool = getGameObject(poolID);
            }
        }
        if (pool == null) return null;
        return createMenuEntry(pool.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(pool).getX(),getLocation(pool).getY(), false);
    }

    private MenuEntry useFairyRingMES() {
        GameObject fairyRing = getGameObject(29228);
        if (getGameObject(29229)!=null) //if tree fairy ring combo is present
        {
            fairyRing = getGameObject(29229);
            return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_FOURTH_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
        }
        return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_THIRD_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
    }

    private MenuEntry teleportToHouseMES() {
        if (getInventoryItem(ItemID.CONSTRUCT_CAPE)!=null)
        {
            return createMenuEntry(ItemID.CONSTRUCT_CAPE, MenuAction.ITEM_FOURTH_OPTION, getInventoryItem(ItemID.CONSTRUCT_CAPE).getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (getInventoryItem(ItemID.CONSTRUCT_CAPET)!=null)
        {
            return createMenuEntry(ItemID.CONSTRUCT_CAPET, MenuAction.ITEM_FOURTH_OPTION, getInventoryItem(ItemID.CONSTRUCT_CAPET).getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(), false);
    }

    private MenuEntry houseOptionsMES() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, 7602250, false);
    }

    private MenuEntry callButlerMES() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, 24248339, false);
    }

    private MenuEntry sendToBankMES() {
        return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, 1, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
    }

    private MenuEntry clickContinueMES() {
        return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, -1, 15138821, false);
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getLocation(TileObject tileObject)
    {
        if (tileObject instanceof GameObject)
        {

            return ((GameObject) tileObject).getSceneMinLocation();
        }
        else
        {
            return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
        }
    }

    private Widget getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getWidgetItem(inventoryWidget,id);
        }
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
        }
        return null;
    }

    private Widget getWidgetItem(Widget widget,int id) {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
            }
        }
        return null;
    }

    private int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }
        return -1;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private boolean isInSwamp() {
        return getGameObject(3508)!=null; //checks for rotting log
    }

    private boolean isInPOH() {
        return getGameObject(4525)!=null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }

    private boolean shouldCastBloom() {
        //in correct spot, inventory isn't full, no mushrooms gatherable
        return
        (client.getLocalPlayer().getWorldLocation().equals(new WorldPoint(3472, 3419, 0)))
                && getEmptySlots()>0
                && getGameObject(3509)==null;
    }

}
