package net.morimori.imp.sound;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.morimori.imp.client.handler.RenderHandler;
import net.morimori.imp.util.SoundHelper;

public class ClientSoundPlayer {
	public static ClientSoundPlayer INSTANS;

	private Map<String, SoundRinger> ringSounds = new HashMap<String, SoundRinger>();
	public Map<String, INewSoundPlayer> playdSounds = new HashMap<String, INewSoundPlayer>();

	private static Minecraft mc = Minecraft.getInstance();

	public void tick() {

		try {
			ringtick();
			playedtick();
		} catch (Exception e) {
			RenderHandler.expations.put("Exception : " + e, 100);
		}
	}

	public SoundRinger getRingSound(String key) {
		if (ringSounds.containsKey(key)) {
			return ringSounds.get(key);
		}
		return null;
	}

	public void addRingSound(String key, SoundRinger sr) {
		if (!ringSounds.containsKey(key)) {
			ringSounds.put(key, sr);
		}
	}

	public void removeRingSound(String key) {
		if (ringSounds.containsKey(key)) {
			ringSounds.get(key).stopRing();
			remringmaps.add(key);
		}
	}

	private Set<String> remringmaps = new HashSet<String>();

	private void ringtick() {

		if (mc.player == null) {
			ringSounds.clear();
		}

		remringmaps.forEach(n -> ringSounds.remove(n));
		remringmaps.clear();

		for (Entry<String, SoundRinger> rs : ringSounds.entrySet()) {
			SoundRinger sr = rs.getValue();

			if (sr.isFinish()) {
				removeRingSound(rs.getKey());
			}

			if (!sr.isRing() && !sr.isFinish())
				sr.startRing();

		}
	}

	private Set<String> removespke = new HashSet<String>();

	private void playedtick() {
		if (mc.player == null) {
			playdSounds.clear();
		}

		removespke.forEach(n -> playdSounds.remove(n));
		removespke.clear();

		for (Entry<String, INewSoundPlayer> rs : playdSounds.entrySet()) {

			if (!ringSounds.containsKey(rs.getKey()) && rs.getValue().isPlayed()) {

				SoundRinger sr = null;

				if (rs.getValue().getSound().type == PlayDatasTypes.FILE) {
					sr = new SoundRinger(rs.getValue().getSound().path);
				} else if (rs.getValue().getSound().type == PlayDatasTypes.WORLD) {
					sr = new WorldSoundRinger(rs.getValue().getSound().wsk);
				} else if (rs.getValue().getSound().type == PlayDatasTypes.URL_STREAM) {
					sr = new StreamSoundRinger(rs.getValue().getSound().url);
				}
				sr.setPotision(rs.getValue().getPosition());
				addRingSound(rs.getKey(), sr);
			}

			if (ringSounds.containsKey(rs.getKey()) && rs.getValue().isPlayed()) {

				float vold = SoundHelper.getVolumeFromSoundPos(rs.getValue().getSoundPos(),
						rs.getValue().getVolume() * SoundHelper.getOptionVolume());

				getRingSound(rs.getKey()).setVolume(vold);

			}

			if (ringSounds.containsKey(rs.getKey()) && !rs.getValue().isPlayed()
					|| ringSounds.containsKey(rs.getKey()) && !rs.getValue().canExistence()) {
				removeRingSound(rs.getKey());
			}

			if (!rs.getValue().canExistence()) {
				removespke.add(rs.getKey());
			}
		}

	}
}
