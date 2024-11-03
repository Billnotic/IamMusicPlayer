package dev.felnull.imp.blockentity;

import dev.felnull.imp.block.IMPBlocks;
import dev.felnull.imp.inventory.MusicManagerMenu;
import dev.felnull.imp.music.resource.AuthorityInfo;
import dev.felnull.imp.music.resource.ImageInfo;
import dev.felnull.imp.music.resource.MusicSource;
import dev.felnull.imp.server.music.MusicManager;
import dev.felnull.otyacraftengine.server.level.TagSerializable;
import dev.felnull.otyacraftengine.util.OENbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MusicManagerBlockEntity extends IMPBaseEntityBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    protected final Map<UUID, CompoundTag> playerData = new HashMap<>();

    public MusicManagerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(IMPBlockEntities.MUSIC_MANAGER.get(), blockPos, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return IMPBlocks.MUSIC_MANAGER.get().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new MusicManagerMenu(i, inventory, getBlockPos(), this);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, MusicManagerBlockEntity blockEntity) {
        //if ()

        if (!level.isClientSide()) {
            blockEntity.playerData.forEach((n, m) -> {
                var monst = m.getString("Monitor");
                if (!monst.isEmpty()) {
                    var type = MonitorType.getByName(monst);
                    if ((blockEntity.isPowered() && type == MonitorType.OFF) || (!blockEntity.isPowered() && type != MonitorType.OFF))
                        m.putString("Monitor", MonitorType.getDefault(blockEntity, n).getName());
                    if (type == MonitorType.OFF) {
                        m.remove("SelectedPlayList");
                        m.remove("SelectedMusic");
                    }
                    var mm = MusicManager.getInstance();
                    if (m.contains("SelectedPlayList") && (mm.getSaveData(level.getServer()).getPlayLists().get(m.getUUID("SelectedPlayList")) == null || !mm.getSaveData(level.getServer()).getPlayLists().get(m.getUUID("SelectedPlayList")).getAuthority().getAuthorityType(n).isMoreReadOnly())) {
                        m.remove("SelectedPlayList");
                        m.remove("SelectedMusic");
                    } else if (m.contains("SelectedMusic") && !mm.getSaveData(level.getServer()).getMusics().containsKey(m.getUUID("SelectedMusic"))) {
                        m.remove("SelectedMusic");
                    }

                    if (m.contains("SelectedPlayer")) {
                        boolean rmFlg = !m.contains("SelectedPlayList");
                        boolean rmFlg2 = false;
                        if (m.contains("SelectedPlayList")) {
                            var pl = mm.getSaveData(level.getServer()).getPlayLists().get(m.getUUID("SelectedPlayList"));
                            if (pl != null) {
                                rmFlg2 = !pl.getAuthority().getPlayersAuthority().containsKey(m.getUUID("SelectedPlayer"));
                            } else {
                                rmFlg2 = true;
                            }
                        }
                        if (rmFlg || rmFlg2)
                            m.remove("SelectedPlayer");
                    }

                    if (type != null && type.isNeedSelectPlayList() && !m.contains("SelectedPlayList")) {
                        m.putString("Monitor", MonitorType.PLAY_LIST.getName());
                    }
                    if (type != null && type.isNeedSelectMusic() && !m.contains("SelectedMusic")) {
                        m.putString("Monitor", MonitorType.PLAY_LIST.getName());
                    }
                }
            });
            blockEntity.setChanged();
        }

        blockEntity.baseAfterTick();
    }

    private void updateMonitor(ServerPlayer player, MonitorType newM, MonitorType oldM) {
        var tag = getPlayerData(player);

        boolean keepFlg = (oldM == MonitorType.ADD_MUSIC && newM == MonitorType.SEARCH_MUSIC) || (oldM == MonitorType.SEARCH_MUSIC && newM == MonitorType.ADD_MUSIC);
        boolean keepFlg2 = (oldM == MonitorType.ADD_MUSIC && newM == MonitorType.UPLOAD_MUSIC) || (oldM == MonitorType.UPLOAD_MUSIC && newM == MonitorType.ADD_MUSIC);
        boolean keepFlg3 = oldM != null && newM != null && oldM.isKeepPlayListData() && newM.isKeepPlayListData();
        boolean keepFlg4 = oldM != null && newM != null && oldM.isKeepMusicData() && newM.isKeepMusicData();

        if (!keepFlg && !keepFlg2 && !keepFlg3 && !keepFlg4) {
            tag.remove("Image");
            tag.remove("ImageURL");
            tag.remove("CreateName");
            tag.remove("Publishing");
            tag.remove("InitialAuthority");
            tag.remove("InvitePlayerName");
            tag.remove("InvitePlayers");
            tag.remove("MusicLoaderType");
            tag.remove("MusicSourceName");
            tag.remove("MusicSource");
            tag.remove("MusicAuthor");
            tag.remove("ImportIdentifier");
            tag.remove("ImportPlayListName");
            tag.remove("ImportPlayListAuthor");
            tag.remove("ImportPlayListMusicCount");
        }

        tag.remove("SelectedPlayer");

        tag.remove("MusicSearchName");

        var mm = MusicManager.getInstance();

        var pl = getSelectedPlayList(player);
        if (oldM == MonitorType.DETAIL_PLAY_LIST && newM == MonitorType.EDIT_PLAY_LIST && pl != null) {
            var pls = mm.getSaveData(level.getServer()).getPlayLists().get(pl);
            if (pls != null) {
                setImage(player, pls.getImage());
                setCreateName(player, pls.getName());
                setInitialAuthority(player, pls.getAuthority().getInitialAuthority() == AuthorityInfo.AuthorityType.MEMBER ? "member" : "read_only");
                setPublishing(player, pls.getAuthority().isPublic() ? "public" : "private");
                setInvitePlayers(player, pls.getAuthority().getRawAuthority().entrySet().stream().filter(n -> n.getValue().isInvitation()).map(Map.Entry::getKey).toList());
            }
        }

        var m = getSelectedMusic(player);
        if (oldM == MonitorType.DETAIL_MUSIC && newM == MonitorType.EDIT_MUSIC && m != null) {
            var pls = mm.getSaveData(level.getServer()).getPlayLists().get(pl);
            if (pls != null && pls.getMusicList().contains(m)) {
                var ms = mm.getSaveData(level.getServer()).getMusics().get(m);
                if (ms != null) {
                    setImage(player, ms.getImage());
                    setCreateName(player, ms.getName());
                    setMusicAuthor(player, ms.getAuthor());
                    setMusicSource(player, ms.getSource());
                    setMusicLoaderType(player, ms.getSource().getLoaderType());
                }
            }
        }
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        OENbtUtils.readUUIDTagMap(tag, "PlayerData", playerData);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        OENbtUtils.writeUUIDTagMap(tag, "PlayerData", playerData);
    }

    @Override
    public void saveToUpdateTag(CompoundTag tag) {
        super.saveToUpdateTag(tag);
        OENbtUtils.writeUUIDTagMap(tag, "SyncPlayerData", playerData);
    }

    @Override
    public void loadToUpdateTag(CompoundTag tag) {
        super.loadToUpdateTag(tag);
        OENbtUtils.readUUIDTagMap(tag, "SyncPlayerData", playerData);
    }

    @Nullable
    public UUID getSelectedMusic(@NotNull ServerPlayer player) {
        var tag = getPlayerData(player);
        if (tag.contains("SelectedMusic"))
            return tag.getUUID("SelectedMusic");
        return null;
    }

    @Nullable
    public UUID getSelectedPlayer(@NotNull ServerPlayer player) {
        var tag = getPlayerData(player);
        if (tag.contains("SelectedPlayer"))
            return tag.getUUID("SelectedPlayer");
        return null;
    }

    @Nullable
    public UUID getSelectedPlayList(@NotNull ServerPlayer player) {
        var tag = getPlayerData(player);
        if (tag.contains("SelectedPlayList"))
            return tag.getUUID("SelectedPlayList");
        return null;
    }

    public CompoundTag getPlayerData(Player player) {
        var id = player.getGameProfile().getId();
        if (!playerData.containsKey(id))
            playerData.put(id, new CompoundTag());
        return playerData.get(id);
    }

    @Nullable
    public UUID getSelectedPlayer(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            return null;

        if (!tag.contains("SelectedPlayer"))
            return null;
        return tag.getUUID("SelectedPlayer");
    }

    @Nullable
    public UUID getSelectedMusic(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            return null;

        if (!tag.contains("SelectedMusic"))
            return null;
        return tag.getUUID("SelectedMusic");
    }

    public UUID getSelectedPlayList(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            return null;

        if (!tag.contains("SelectedPlayList"))
            return null;
        return tag.getUUID("SelectedPlayList");
    }

    public MusicSource getMusicSource(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            return null;

        if (tag.contains("MusicSource"))
            return TagSerializable.loadSavedTag(tag.getCompound("MusicSource"), new MusicSource());
        return MusicSource.EMPTY;
    }

    public void setMusicSource(ServerPlayer player, MusicSource source) {
        getPlayerData(player).put("MusicSource", source.createSavedTag());
        setChanged();
    }

    public int getImportPlayListMusicCount(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getInt("ImportPlayListMusicCount");
    }

    public void setImportPlayListMusicCount(@NotNull ServerPlayer player, int num) {
        getPlayerData(player).putInt("ImportPlayListMusicCount", num);
        setChanged();
    }

    @NotNull
    public String getImportPlayListAuthor(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("ImportPlayListAuthor");
    }

    public void setImportPlayListAuthor(@NotNull ServerPlayer player, @NotNull String name) {
        getPlayerData(player).putString("ImportPlayListAuthor", name);
        setChanged();
    }

    @NotNull
    public String getImportPlayListName(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("ImportPlayListName");
    }

    public void setImportPlayListName(@NotNull ServerPlayer player, @NotNull String name) {
        getPlayerData(player).putString("ImportPlayListName", name);
        setChanged();
    }

    @NotNull
    public String getImportIdentifier(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("ImportIdentifier");
    }

    public void setImportIdentifier(@NotNull ServerPlayer player, @NotNull String identifier) {
        getPlayerData(player).putString("ImportIdentifier", identifier);
        setChanged();
    }

    public String getMusicSourceName(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("MusicSourceName");
    }

    public void setMusicSourceName(ServerPlayer player, String name) {
        getPlayerData(player).putString("MusicSourceName", name);
        setChanged();
    }

    public String getMusicAuthor(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("MusicAuthor");
    }

    public void setMusicAuthor(ServerPlayer player, String name) {
        getPlayerData(player).putString("MusicAuthor", name);
        setChanged();
    }

    public String getMusicSearchName(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("MusicSearchName");
    }

    public void setMusicSearchName(ServerPlayer player, String name) {
        getPlayerData(player).putString("MusicSearchName", name);
        setChanged();
    }

    public String getMusicLoaderType(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("MusicLoaderType");
    }

    public void setMusicLoaderType(ServerPlayer player, String name) {
        getPlayerData(player).putString("MusicLoaderType", name);
        setChanged();
    }

    public void setSelectedPlayer(@NotNull ServerPlayer player, @Nullable UUID selectedPlayer) {
        if (selectedPlayer != null) {
            getPlayerData(player).putUUID("SelectedPlayer", selectedPlayer);
        } else {
            getPlayerData(player).remove("SelectedPlayer");
        }
        setChanged();
    }

    public void setSelectedMusic(@NotNull ServerPlayer player, @Nullable UUID selectedMusic) {
        var type = getMonitor(player);
        if (type != null && type.isNeedSelectMusic()) {
            if (getPlayerData(player).contains("SelectedMusic")) {
                var old = getPlayerData(player).getUUID("SelectedMusic");
                if (selectedMusic == null || !selectedMusic.equals(old))
                    setMonitor(player, MonitorType.PLAY_LIST);
            }
        }
        if (selectedMusic != null) {
            getPlayerData(player).putUUID("SelectedMusic", selectedMusic);
        } else {
            getPlayerData(player).remove("SelectedMusic");
        }
        setChanged();
    }

    public void setSelectedPlayList(@NotNull ServerPlayer player, @Nullable UUID selectedPlayList) {
        var type = getMonitor(player);
        if (type != null && type.isNeedSelectPlayList()) {
            if (getPlayerData(player).contains("SelectedPlayList")) {
                var old = getPlayerData(player).getUUID("SelectedPlayList");
                if (selectedPlayList == null || !selectedPlayList.equals(old))
                    setMonitor(player, MonitorType.PLAY_LIST);
            }
        }
        if (selectedPlayList != null) {
            getPlayerData(player).putUUID("SelectedPlayList", selectedPlayList);
        } else {
            getPlayerData(player).remove("SelectedPlayList");
        }
        setChanged();
    }


    public List<UUID> getInvitePlayers(Player player) {
        List<UUID> pls = new ArrayList<>();

        var tag = getPlayerData(player);
        if (tag == null)
            return pls;

        OENbtUtils.readUUIDList(tag, "InvitePlayers", pls);
        return pls;
    }

    public void setInvitePlayers(ServerPlayer player, List<UUID> players) {
        var tag = getPlayerData(player);
        tag.remove("InvitePlayers");
        OENbtUtils.writeUUIDList(tag, "InvitePlayers", players);
        setChanged();
    }

    public String getInvitePlayerName(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("InvitePlayerName");
    }

    public void setInvitePlayerName(ServerPlayer player, String name) {
        getPlayerData(player).putString("InvitePlayerName", name);
        setChanged();
    }

    public void setImage(ServerPlayer player, ImageInfo image) {
        getPlayerData(player).put("Image", image.createSavedTag());
        setChanged();
    }

    public ImageInfo getImage(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        if (tag.getCompound("Image").isEmpty())
            return ImageInfo.EMPTY;
        return TagSerializable.loadSavedTag(tag.getCompound("Image"), new ImageInfo());
    }

    public String getInitialAuthority(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("InitialAuthority");
    }

    public void setInitialAuthority(ServerPlayer player, String initialAuthority) {
        getPlayerData(player).putString("InitialAuthority", initialAuthority);
        setChanged();
    }

    public String getPublishing(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("Publishing");
    }

    public void setPublishing(ServerPlayer player, String publishing) {
        getPlayerData(player).putString("Publishing", publishing);
        setChanged();
    }

    public String getCreateName(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("CreateName");
    }

    public void setCreateName(ServerPlayer player, String name) {
        getPlayerData(player).putString("CreateName", name);
        setChanged();
    }

    public void setImageURL(ServerPlayer player, String url) {
        getPlayerData(player).putString("ImageURL", url);
        setChanged();
    }

    public String getImageURL(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        return tag.getString("ImageURL");
    }


    public MonitorType getMonitor(ServerPlayer player) {
        return MonitorType.getByNameOrDefault(getPlayerData(player).getString("Monitor"), this, player.getGameProfile().getId());
    }

    public void setMonitor(ServerPlayer player, MonitorType type) {
        var oldM = MonitorType.getByNameOrDefault(getPlayerData(player).getString("Monitor"), this, player.getGameProfile().getId());
        if (oldM != type)
            updateMonitor(player, type, oldM);
        getPlayerData(player).putString("Monitor", type.getName());
        setChanged();
    }

    public MonitorType getMonitor(Player player) {
        var tag = getPlayerData(player);
        if (tag == null)
            tag = new CompoundTag();

        var name = tag.getString("Monitor");
        return MonitorType.getByNameOrDefault(name, this, player.getGameProfile().getId());
    }

    @Override
    public CompoundTag onInstruction(ServerPlayer player, String name, CompoundTag data) {
        if ("set_monitor".equals(name)) {
            var mn = data.getString("type");
            if (!mn.isEmpty())
                setMonitor(player, MonitorType.getByName(mn));
            return null;
        } else if ("add_playlist".equals(name)) {
            if (data.contains("playlist")) {
                var pl = data.getUUID("playlist");
                MusicManager.getInstance().addPlayListToPlayer(level.getServer(), pl, player);
            }
            return data;
        } else if ("set_image_url".equals(name)) {
            var url = data.getString("url");
            setImageURL(player, url);
            return null;
        } else if ("set_image".equals(name)) {
            var image = TagSerializable.loadSavedTag(data.getCompound("image"), new ImageInfo());
            setImage(player, image);
            return null;
        } else if ("set_create_name".equals(name)) {
            var cname = data.getString("name");
            setCreateName(player, cname);
            return null;
        } else if ("set_publishing".equals(name)) {
            var pub = data.getString("publishing");
            setPublishing(player, pub);
            return null;
        } else if ("set_initial_authority".equals(name)) {
            var ina = data.getString("initial_authority");
            setInitialAuthority(player, ina);
            return null;
        } else if ("set_invite_player_name".equals(name)) {
            var pname = data.getString("name");
            setInvitePlayerName(player, pname);
            return null;
        } else if ("set_invite_players".equals(name)) {
            List<UUID> pls = new ArrayList<>();
            OENbtUtils.readUUIDList(data, "players", pls);
            setInvitePlayers(player, pls);
            return null;
        } else if ("set_selected_playlist".equals(name)) {
            if (data.contains("playlist")) {
                var id = data.getUUID("playlist");
                setSelectedPlayList(player, id);
            } else {
                setSelectedPlayList(player, null);
            }
            return data;
        } else if ("set_music_loader_type".equals(name)) {
            var lname = data.getString("name");
            setMusicLoaderType(player, lname);
            return null;
        } else if ("set_music_source_name".equals(name)) {
            var mname = data.getString("name");
            setMusicSourceName(player, mname);
            return null;
        } else if ("set_music_source".equals(name)) {
            var ms = TagSerializable.loadSavedTag(data.getCompound("MusicSource"), new MusicSource());
            setMusicSource(player, ms);
            return null;
        } else if ("set_music_search_name".equals(name)) {
            var sname = data.getString("name");
            setMusicSearchName(player, sname);
            return null;
        } else if ("set_music_author".equals(name)) {
            var author = data.getString("author");
            setMusicAuthor(player, author);
            return null;
        } else if ("set_import_identifier".equals(name)) {
            setImportIdentifier(player, data.getString("id"));
            return null;
        } else if ("set_import_playlist_name".equals(name)) {
            setImportPlayListName(player, data.getString("name"));
            return null;
        } else if ("set_import_playlist_author".equals(name)) {
            setImportPlayListAuthor(player, data.getString("author"));
            return null;
        } else if ("set_import_playlist_music_count".equals(name)) {
            setImportPlayListMusicCount(player, data.getInt("count"));
            return null;
        } else if ("set_selected_music".equals(name)) {
            if (data.contains("music")) {
                var id = data.getUUID("music");
                setSelectedMusic(player, id);
            } else {
                setSelectedMusic(player, null);
            }
            return null;
        } else if ("set_selected_player".equals(name)) {
            if (data.contains("player")) {
                var id = data.getUUID("player");
                setSelectedPlayer(player, id);
            } else {
                setSelectedPlayer(player, null);
            }
            return null;
        }
        return super.onInstruction(player, name, data);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    public static enum MonitorType {
        OFF("off", false),
        TEST("test", false),
        PLAY_LIST("play_list", false),
        ADD_PLAY_LIST("add_play_list", false),
        ADD_ONLINE_PLAY_LIST("add_online_play_list", false),
        EDIT_PLAY_LIST("edit_play_list", true),
        DETAIL_PLAY_LIST("detail_play_list", true),
        IMPORT_PLAY_LIST_SELECT("import_play_list_select", false),
        CREATE_PLAY_LIST("create_play_list", false),
        DELETE_PLAY_LIST("delete_play_list", true),
        ADD_MUSIC("add_music", true),
        SEARCH_MUSIC("search_music", true),
        UPLOAD_MUSIC("upload_music", true),
        DETAIL_MUSIC("detail_music", true),
        EDIT_MUSIC("edit_music", true),
        DELETE_MUSIC("delete_music", true),
        IMPORT_YOUTUBE_PLAY_LIST("import_youtube_play_list", false),
        IMPORT_MUSICS_SELECT("import_musics_select", true),
        IMPORT_YOUTUBE_PLAY_LIST_MUSICS("import_youtube_play_list_musics", true),
        AUTHORITY("authority", true);
        private final String name;
        private final boolean needSelectPlayList;

        private MonitorType(String name, boolean needSelectPlayList) {
            this.name = name;
            this.needSelectPlayList = needSelectPlayList;
        }

        public String getName() {
            return name;
        }

        public static MonitorType getByNameOrDefault(String name, MusicManagerBlockEntity blockEntity, UUID player) {
            for (MonitorType value : values()) {
                if (value.getName().equals(name))
                    return value;
            }
            return getDefault(blockEntity, player);
        }


        public static MonitorType getByName(String name) {
            for (MonitorType value : values()) {
                if (value.getName().equals(name))
                    return value;
            }
            return MonitorType.OFF;
        }

        public boolean isNeedSelectPlayList() {
            return needSelectPlayList;
        }

        public boolean isNeedSelectMusic() {
            return this == DETAIL_MUSIC || this == DELETE_MUSIC || this == EDIT_MUSIC;
        }

        public boolean isKeepPlayListData() {
            return this == CREATE_PLAY_LIST || this == IMPORT_PLAY_LIST_SELECT || this == IMPORT_YOUTUBE_PLAY_LIST;
        }

        public boolean isKeepMusicData() {
            return this == ADD_MUSIC || this == IMPORT_MUSICS_SELECT || this == IMPORT_YOUTUBE_PLAY_LIST_MUSICS;
        }

        public static MonitorType getDefault(MusicManagerBlockEntity blockEntity, UUID player) {
            return blockEntity.isPowered() ? PLAY_LIST : OFF;
        }
    }
}
