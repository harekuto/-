#!/usr/bin/env bash
set -euo pipefail

ROOT="$PWD/dreadstalker"
rm -rf "$ROOT"
mkdir -p "$ROOT/src/main/java/dev/harekuto/dreadstalker/client/model"
mkdir -p "$ROOT/src/main/java/dev/harekuto/dreadstalker/client/render"
mkdir -p "$ROOT/src/main/java/dev/harekuto/dreadstalker/client"
mkdir -p "$ROOT/src/main/java/dev/harekuto/dreadstalker/entity"
mkdir -p "$ROOT/src/main/java/dev/harekuto/dreadstalker/network"
mkdir -p "$ROOT/src/main/java/dev/harekuto/dreadstalker/registry"
mkdir -p "$ROOT/src/main/java/dev/harekuto/dreadstalker/world"
mkdir -p "$ROOT/src/main/resources/META-INF"
mkdir -p "$ROOT/src/main/resources/assets/dreadstalker/lang"
mkdir -p "$ROOT/src/main/resources/assets/dreadstalker/models/item"
mkdir -p "$ROOT/src/main/resources/assets/dreadstalker/textures/entity"
mkdir -p "$ROOT/src/main/resources/assets/dreadstalker/textures/gui"
mkdir -p "$ROOT/src/main/resources/assets/dreadstalker/sounds"
mkdir -p "$ROOT/src/main/resources/data/dreadstalker/forge/biome_modifier"
mkdir -p "$ROOT/src/main/resources/data/dreadstalker/loot_tables/entities"

cat > "$ROOT/settings.gradle" <<'EOF'
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = 'https://maven.minecraftforge.net/' }
    }
}
rootProject.name = 'dreadstalker'
EOF

cat > "$ROOT/gradle.properties" <<'EOF'
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false
minecraft_version=1.20.1
forge_version=47.4.10
mod_id=dreadstalker
mod_name=Dreadstalker
mod_version=1.0.0
mod_group_id=dev.harekuto.dreadstalker
EOF

cat > "$ROOT/build.gradle" <<'EOF'
plugins {
    id 'eclipse'
    id 'idea'
    id 'maven-publish'
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
}

group = mod_group_id
version = mod_version
base { archivesName = "${mod_id}-${minecraft_version}-forge" }

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

minecraft {
    mappings channel: 'official', version: minecraft_version
    copyIdeResources = true
    runs {
        server {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'info'
            mods { dreadstalker { source sourceSets.main } }
        }
        client {
            workingDirectory project.file('run-client')
            property 'forge.logging.console.level', 'info'
            mods { dreadstalker { source sourceSets.main } }
        }
    }
}

repositories { mavenCentral() }

dependencies {
    minecraft "net.minecraftforge:forge:${minecraft_version}-${forge_version}"
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}

tasks.named('jar', Jar).configure {
    manifest {
        attributes([
            'Specification-Title': mod_id,
            'Specification-Vendor': 'Harekuto',
            'Specification-Version': '1',
            'Implementation-Title': project.name,
            'Implementation-Version': project.jar.archiveVersion,
            'Implementation-Vendor': 'Harekuto'
        ])
    }
    finalizedBy 'reobfJar'
}
EOF

cat > "$ROOT/src/main/resources/META-INF/mods.toml" <<'EOF'
modLoader="javafml"
loaderVersion="[47,)"
license="All Rights Reserved"

[[mods]]
modId="dreadstalker"
version="1.0.0"
displayName="Dreadstalker"
authors="Harekuto"
description='''
A psychological horror creature mod centered around the Dreadstalker: a tall voxel predator that watches, freezes under observation, stalks from darkness, crawls through caves, triggers atmospheric events, and performs synchronized visual/audio jumpscares.
'''

[[dependencies.dreadstalker]]
modId="forge"
mandatory=true
versionRange="[47,)"
ordering="NONE"
side="BOTH"

[[dependencies.dreadstalker]]
modId="minecraft"
mandatory=true
versionRange="[1.20.1,1.21)"
ordering="NONE"
side="BOTH"
EOF

cat > "$ROOT/src/main/resources/pack.mcmeta" <<'EOF'
{"pack":{"description":"Dreadstalker resources","pack_format":15}}
EOF

cat > "$ROOT/src/main/resources/assets/dreadstalker/lang/en_us.json" <<'EOF'
{
  "entity.dreadstalker.dreadstalker": "Dreadstalker",
  "item.dreadstalker.dreadstalker_spawn_egg": "Dreadstalker Spawn Egg"
}
EOF
cat > "$ROOT/src/main/resources/assets/dreadstalker/lang/ru_ru.json" <<'EOF'
{
  "entity.dreadstalker.dreadstalker": "Дредсталкер",
  "item.dreadstalker.dreadstalker_spawn_egg": "Яйцо призыва Дредсталкера"
}
EOF

cat > "$ROOT/src/main/resources/assets/dreadstalker/models/item/dreadstalker_spawn_egg.json" <<'EOF'
{"parent":"minecraft:item/template_spawn_egg"}
EOF

cat > "$ROOT/src/main/resources/data/dreadstalker/forge/biome_modifier/add_dreadstalker.json" <<'EOF'
{
  "type": "forge:add_spawns",
  "biomes": "#minecraft:is_overworld",
  "spawners": {
    "type": "dreadstalker:dreadstalker",
    "weight": 2,
    "minCount": 1,
    "maxCount": 1
  }
}
EOF

cat > "$ROOT/src/main/resources/data/dreadstalker/loot_tables/entities/dreadstalker.json" <<'EOF'
{
  "type": "minecraft:entity",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {"type":"minecraft:item","name":"minecraft:bone"},
        {"type":"minecraft:item","name":"minecraft:redstone","weight":1}
      ],
      "functions": [
        {"function":"minecraft:set_count","count":{"min":1.0,"max":3.0}}
      ]
    }
  ]
}
EOF

cat > "$ROOT/src/main/resources/assets/dreadstalker/sounds.json" <<'EOF'
{
  "ambient_whisper": {"sounds":[{"name":"dreadstalker:ambient_whisper","stream":false}]},
  "breath": {"sounds":[{"name":"dreadstalker:breath","stream":false}]},
  "roar": {"sounds":[{"name":"dreadstalker:roar","stream":false}]},
  "jumpscare": {"sounds":[{"name":"dreadstalker:jumpscare","stream":false}]},
  "step": {"sounds":[{"name":"dreadstalker:step","stream":false}]},
  "scrape": {"sounds":[{"name":"dreadstalker:scrape","stream":false}]},
  "death": {"sounds":[{"name":"dreadstalker:death","stream":false}]}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/DreadstalkerMod.java" <<'EOF'
package dev.harekuto.dreadstalker;

import dev.harekuto.dreadstalker.entity.DreadstalkerEntity;
import dev.harekuto.dreadstalker.network.ModNetwork;
import dev.harekuto.dreadstalker.registry.ModEntities;
import dev.harekuto.dreadstalker.registry.ModItems;
import dev.harekuto.dreadstalker.registry.ModSounds;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DreadstalkerMod.MODID)
public final class DreadstalkerMod {
    public static final String MODID = "dreadstalker";

    public DreadstalkerMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.REGISTER.register(modBus);
        ModItems.REGISTER.register(modBus);
        ModSounds.REGISTER.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::attributes);
        modBus.addListener(this::creativeTabs);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            SpawnPlacements.register(ModEntities.DREADSTALKER.get(), SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DreadstalkerEntity::checkSpawnRules);
        });
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DREADSTALKER.get(), DreadstalkerEntity.createAttributes().build());
    }

    private void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.DREADSTALKER_SPAWN_EGG);
        }
    }
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/registry/ModEntities.java" <<'EOF'
package dev.harekuto.dreadstalker.registry;

import dev.harekuto.dreadstalker.DreadstalkerMod;
import dev.harekuto.dreadstalker.entity.DreadstalkerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DreadstalkerMod.MODID);
    public static final RegistryObject<EntityType<DreadstalkerEntity>> DREADSTALKER = REGISTER.register("dreadstalker",
            () -> EntityType.Builder.of(DreadstalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.95F, 3.15F)
                    .clientTrackingRange(12)
                    .updateInterval(2)
                    .build("dreadstalker"));
    private ModEntities() {}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/registry/ModItems.java" <<'EOF'
package dev.harekuto.dreadstalker.registry;

import dev.harekuto.dreadstalker.DreadstalkerMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, DreadstalkerMod.MODID);
    public static final RegistryObject<Item> DREADSTALKER_SPAWN_EGG = REGISTER.register("dreadstalker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DREADSTALKER, 0x120d0e, 0xd11119, new Item.Properties()));
    private ModItems() {}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/registry/ModSounds.java" <<'EOF'
package dev.harekuto.dreadstalker.registry;

import dev.harekuto.dreadstalker.DreadstalkerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DreadstalkerMod.MODID);
    public static final RegistryObject<SoundEvent> AMBIENT_WHISPER = sound("ambient_whisper");
    public static final RegistryObject<SoundEvent> BREATH = sound("breath");
    public static final RegistryObject<SoundEvent> ROAR = sound("roar");
    public static final RegistryObject<SoundEvent> JUMPSCARE = sound("jumpscare");
    public static final RegistryObject<SoundEvent> STEP = sound("step");
    public static final RegistryObject<SoundEvent> SCRAPE = sound("scrape");
    public static final RegistryObject<SoundEvent> DEATH = sound("death");

    private static RegistryObject<SoundEvent> sound(String name) {
        ResourceLocation id = new ResourceLocation(DreadstalkerMod.MODID, name);
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
    private ModSounds() {}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/network/ScarePacket.java" <<'EOF'
package dev.harekuto.dreadstalker.network;

import dev.harekuto.dreadstalker.client.ClientScareState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ScarePacket {
    public final int type;
    public final int duration;
    public final float strength;
    public final long seed;

    public ScarePacket(int type, int duration, float strength, long seed) {
        this.type = type;
        this.duration = duration;
        this.strength = strength;
        this.seed = seed;
    }

    public static void encode(ScarePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.type);
        buf.writeVarInt(msg.duration);
        buf.writeFloat(msg.strength);
        buf.writeLong(msg.seed);
    }

    public static ScarePacket decode(FriendlyByteBuf buf) {
        return new ScarePacket(buf.readVarInt(), buf.readVarInt(), buf.readFloat(), buf.readLong());
    }

    public static void handle(ScarePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientScareState.trigger(msg.type, msg.duration, msg.strength, msg.seed)));
        context.setPacketHandled(true);
    }
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/network/ModNetwork.java" <<'EOF'
package dev.harekuto.dreadstalker.network;

import dev.harekuto.dreadstalker.DreadstalkerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DreadstalkerMod.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static int id;
    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(id++, ScarePacket.class, ScarePacket::encode, ScarePacket::decode, ScarePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void scare(ServerPlayer player, int type, int duration, float strength) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ScarePacket(type, duration, strength, player.getRandom().nextLong()));
    }
    private ModNetwork() {}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/entity/DreadstalkerEntity.java" <<'EOF'
package dev.harekuto.dreadstalker.entity;

import dev.harekuto.dreadstalker.network.ModNetwork;
import dev.harekuto.dreadstalker.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

public final class DreadstalkerEntity extends Monster {
    private static final EntityDataAccessor<Boolean> CRAWLING = SynchedEntityData.defineId(DreadstalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SCARE_TICKS = SynchedEntityData.defineId(DreadstalkerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICKS = SynchedEntityData.defineId(DreadstalkerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> EVENT_SPAWN = SynchedEntityData.defineId(DreadstalkerEntity.class, EntityDataSerializers.BOOLEAN);

    private int unseenTicks;
    private int stareTicks;
    private int teleportCooldown = 100;
    private int scareCooldown = 160;
    private int lifeWithoutTarget;

    public DreadstalkerEntity(EntityType<? extends DreadstalkerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 18;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 52.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 52.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D)
                .add(Attributes.ARMOR, 5.0D);
    }

    public static boolean checkSpawnRules(EntityType<DreadstalkerEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || pos.getY() > 58) return false;
        if (level.getBrightness(LightLayer.BLOCK, pos) > 4) return false;
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CRAWLING, false);
        this.entityData.define(SCARE_TICKS, 0);
        this.entityData.define(ATTACK_TICKS, 0);
        this.entityData.define(EVENT_SPAWN, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.72D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 36.0F, 0.15F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> player != null && !player.isCreative() && !player.isSpectator()));
    }

    @Override
    public void tick() {
        super.tick();
        if (getAttackTicks() > 0 && !level().isClientSide) setAttackTicks(getAttackTicks() - 1);
        if (getScareTicks() > 0 && !level().isClientSide) setScareTicks(getScareTicks() - 1);

        if (level().isClientSide) {
            if ((getScareTicks() > 0 || isCrawling()) && random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.SMOKE, getRandomX(0.7), getY() + 1.0 + random.nextDouble() * 1.7,
                        getRandomZ(0.7), 0.0, 0.02, 0.0);
            }
            return;
        }
        serverHorrorTick();
    }

    private void serverHorrorTick() {
        if (teleportCooldown > 0) teleportCooldown--;
        if (scareCooldown > 0) scareCooldown--;

        Player target = getTarget() instanceof Player p ? p : null;
        if (target == null || !target.isAlive() || target.isCreative() || target.isSpectator()) {
            lifeWithoutTarget++;
            unseenTicks = 0;
            stareTicks = 0;
            setCrawling(false);
            if (isEventSpawn() && lifeWithoutTarget > 20 * 25) discard();
            return;
        }
        lifeWithoutTarget = 0;

        double distance = distanceTo(target);
        boolean watched = distance < 28.0D && isPlayerLookingAtMe(target);
        setCrawling(!watched && target.getY() < 42.0D && distance < 12.0D);

        if (getScareTicks() > 0) {
            getNavigation().stop();
            getLookControl().setLookAt(target, 180.0F, 180.0F);
            setDeltaMovement(getDeltaMovement().multiply(0.12D, 1.0D, 0.12D));
            return;
        }

        if (watched) {
            stareTicks++;
            unseenTicks = Math.max(0, unseenTicks - 3);
            getNavigation().stop();
            setDeltaMovement(getDeltaMovement().multiply(0.08D, 1.0D, 0.08D));
            getLookControl().setLookAt(target, 180.0F, 180.0F);
            if (stareTicks == 30 && target instanceof ServerPlayer sp) {
                ModNetwork.scare(sp, 0, 18, 0.28F);
                level().playSound(null, target.blockPosition(), ModSounds.AMBIENT_WHISPER.get(), SoundSource.HOSTILE, 0.65F, 0.72F);
            }
        } else {
            stareTicks = 0;
            unseenTicks++;
            if (distance > 2.4D) {
                double speed = isCrawling() ? 1.42D : (distance > 9.0D ? 1.34D : 1.20D);
                getNavigation().moveTo(target, speed);
            }

            if (teleportCooldown <= 0 && unseenTicks > 100 && distance > 6.0D && random.nextInt(35) == 0) {
                teleportBehind(target, 5.5D + random.nextDouble() * 3.0D);
                teleportCooldown = 120 + random.nextInt(100);
            }

            if (scareCooldown <= 0 && unseenTicks > 70 && distance < 7.5D && target instanceof ServerPlayer sp && random.nextInt(28) == 0) {
                triggerJumpscare(sp);
            }
        }
    }

    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toMe = new Vec3(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
        double distance = toMe.length();
        if (distance < 0.001D) return true;
        double dot = view.dot(toMe.normalize());
        double threshold = distance < 8.0D ? 0.91D : 0.965D;
        return dot > threshold && player.hasLineOfSight(this);
    }

    private void triggerJumpscare(ServerPlayer player) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 front = player.position().add(look.scale(1.65D));
        Vec3 safe = findSafePosition((ServerLevel) level(), front.x, front.z, Mth.floor(player.getY()));
        if (safe != null && safe.distanceTo(player.position()) < 4.5D) {
            teleportTo(safe.x, safe.y, safe.z);
        }
        setScareTicks(28);
        unseenTicks = 0;
        scareCooldown = 20 * 24;
        getNavigation().stop();
        getLookControl().setLookAt(player, 180.0F, 180.0F);
        ModNetwork.scare(player, 1, 18, 1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 48, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, 1, false, false, false));
        level().playSound(null, player.blockPosition(), ModSounds.JUMPSCARE.get(), SoundSource.HOSTILE, 1.65F, 0.82F + random.nextFloat() * 0.08F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.5D, getZ(), 18, 0.35D, 0.8D, 0.35D, 0.02D);
        }
    }

    private boolean teleportBehind(Player player, double distance) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 side = new Vec3(-look.z, 0.0D, look.x).scale((random.nextBoolean() ? 1 : -1) * (1.0D + random.nextDouble() * 2.0D));
        Vec3 desired = player.position().subtract(look.scale(distance)).add(side);
        Vec3 safe = findSafePosition((ServerLevel) level(), desired.x, desired.z, Mth.floor(player.getY()));
        if (safe == null) return false;
        teleportTo(safe.x, safe.y, safe.z);
        level().playSound(null, blockPosition(), ModSounds.SCRAPE.get(), SoundSource.HOSTILE, 0.45F, 0.62F + random.nextFloat() * 0.16F);
        return true;
    }

    @Nullable
    public static Vec3 findSafePosition(ServerLevel level, double x, double z, int aroundY) {
        int ix = Mth.floor(x);
        int iz = Mth.floor(z);
        for (int offset = 4; offset >= -7; offset--) {
            BlockPos feet = new BlockPos(ix, aroundY + offset, iz);
            BlockPos floor = feet.below();
            BlockState floorState = level.getBlockState(floor);
            if (floorState.getCollisionShape(level, floor).isEmpty()) continue;
            boolean clear = true;
            for (int y = 0; y <= 3; y++) {
                if (!level.getBlockState(feet.above(y)).getCollisionShape(level, feet.above(y)).isEmpty()) {
                    clear = false;
                    break;
                }
            }
            if (clear) return new Vec3(ix + 0.5D, feet.getY(), iz + 0.5D);
        }
        return null;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        setAttackTicks(12);
        level().playSound(null, blockPosition(), ModSounds.ROAR.get(), SoundSource.HOSTILE, 0.75F, 0.85F + random.nextFloat() * 0.15F);
        return super.doHurtTarget(target);
    }

    public boolean isCrawling() { return entityData.get(CRAWLING); }
    public void setCrawling(boolean value) { entityData.set(CRAWLING, value); }
    public int getScareTicks() { return entityData.get(SCARE_TICKS); }
    public void setScareTicks(int value) { entityData.set(SCARE_TICKS, value); }
    public int getAttackTicks() { return entityData.get(ATTACK_TICKS); }
    public void setAttackTicks(int value) { entityData.set(ATTACK_TICKS, value); }
    public boolean isEventSpawn() { return entityData.get(EVENT_SPAWN); }
    public void setEventSpawn(boolean value) { entityData.set(EVENT_SPAWN, value); }

    @Override
    protected SoundEvent getAmbientSound() { return ModSounds.BREATH.get(); }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.SCRAPE.get(); }
    @Override
    protected SoundEvent getDeathSound() { return ModSounds.DEATH.get(); }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(ModSounds.STEP.get(), 0.35F, 0.76F + random.nextFloat() * 0.12F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("EventSpawn", isEventSpawn());
        tag.putInt("ScareCooldown", scareCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setEventSpawn(tag.getBoolean("EventSpawn"));
        scareCooldown = tag.getInt("ScareCooldown");
    }
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/world/DreadEventManager.java" <<'EOF'
package dev.harekuto.dreadstalker.world;

import dev.harekuto.dreadstalker.DreadstalkerMod;
import dev.harekuto.dreadstalker.entity.DreadstalkerEntity;
import dev.harekuto.dreadstalker.network.ModNetwork;
import dev.harekuto.dreadstalker.registry.ModEntities;
import dev.harekuto.dreadstalker.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DreadstalkerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DreadEventManager {
    private static final String COOLDOWN = "dreadstalker_event_cooldown";
    private static final String CHAIN = "dreadstalker_approach_chain";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0 || player.isCreative() || player.isSpectator()) return;
        if (!(player.level() instanceof ServerLevel level) || level.getDifficulty().getId() == 0) return;

        CompoundTag data = player.getPersistentData();
        int chain = data.getInt(CHAIN);
        if (chain > 0) {
            runApproachChain(level, player, chain);
            if (chain >= 5) data.remove(CHAIN); else data.putInt(CHAIN, chain + 1);
            return;
        }

        int cooldown = data.getInt(COOLDOWN);
        if (cooldown > 0) {
            data.putInt(COOLDOWN, Math.max(0, cooldown - 20));
            return;
        }

        int blockLight = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
        boolean exposedToSky = level.canSeeSky(player.blockPosition().above());
        if (blockLight > 7 || exposedToSky || player.getY() > 62.0D) return;
        if (player.getRandom().nextInt(9) != 0) return;

        int eventType = player.getRandom().nextInt(5);
        switch (eventType) {
            case 0 -> whisper(level, player);
            case 1 -> distantScrape(level, player);
            case 2 -> darknessPulse(level, player);
            case 3 -> data.putInt(CHAIN, 1);
            default -> spawnWatcher(level, player, 15.0D, 25.0D);
        }
        data.putInt(COOLDOWN, 20 * (35 + player.getRandom().nextInt(55)));
    }

    private static void whisper(ServerLevel level, ServerPlayer player) {
        Vec3 p = offsetBehind(player, 4.0D + player.getRandom().nextDouble() * 4.0D, 2.0D);
        level.playSound(null, p.x, p.y + 0.5D, p.z, ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.8F, 0.62F + player.getRandom().nextFloat() * 0.2F);
        ModNetwork.scare(player, 0, 20, 0.24F);
    }

    private static void distantScrape(ServerLevel level, ServerPlayer player) {
        Vec3 p = offsetBehind(player, 7.0D + player.getRandom().nextDouble() * 7.0D, 6.0D);
        level.playSound(null, p.x, p.y, p.z, ModSounds.SCRAPE.get(), SoundSource.AMBIENT, 0.75F, 0.55F + player.getRandom().nextFloat() * 0.18F);
        level.playSound(null, p.x + 1.5D, p.y, p.z - 1.5D, ModSounds.STEP.get(), SoundSource.AMBIENT, 0.45F, 0.8F);
    }

    private static void darknessPulse(ServerLevel level, ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 55, 0, false, false, false));
        level.playSound(null, player.blockPosition(), ModSounds.BREATH.get(), SoundSource.AMBIENT, 0.55F, 0.58F);
        ModNetwork.scare(player, 0, 30, 0.42F);
    }

    private static void runApproachChain(ServerLevel level, ServerPlayer player, int stage) {
        double distance = 10.0D - stage * 1.7D;
        Vec3 p = offsetBehind(player, Math.max(2.0D, distance), 0.8D);
        level.playSound(null, p.x, p.y, p.z, stage % 2 == 0 ? ModSounds.SCRAPE.get() : ModSounds.STEP.get(),
                SoundSource.AMBIENT, 0.48F + stage * 0.08F, 0.72F - stage * 0.03F);
        if (stage == 3) {
            level.playSound(null, p.x, p.y + 1.0D, p.z, ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.65F, 0.57F);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 28, 0, false, false, false));
            ModNetwork.scare(player, 0, 24, 0.35F);
        }
        if (stage == 5) {
            spawnWatcher(level, player, 8.0D, 12.0D);
        }
    }

    private static Vec3 offsetBehind(ServerPlayer player, double back, double sideScale) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 side = new Vec3(-look.z, 0.0D, look.x).scale((player.getRandom().nextBoolean() ? 1.0D : -1.0D) * sideScale);
        return player.position().subtract(look.scale(back)).add(side);
    }

    private static void spawnWatcher(ServerLevel level, ServerPlayer player, double minDistance, double maxDistance) {
        if (!level.getEntitiesOfClass(DreadstalkerEntity.class, player.getBoundingBox().inflate(42.0D)).isEmpty()) return;
        for (int tries = 0; tries < 14; tries++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = minDistance + player.getRandom().nextDouble() * (maxDistance - minDistance);
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            Vec3 pos = DreadstalkerEntity.findSafePosition(level, x, z, Mth.floor(player.getY()));
            if (pos == null) continue;
            DreadstalkerEntity mob = ModEntities.DREADSTALKER.get().create(level);
            if (mob == null) return;
            mob.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180.0F, 0.0F);
            mob.setEventSpawn(true);
            mob.setPersistenceRequired();
            mob.setTarget(player);
            level.addFreshEntity(mob);
            level.playSound(null, mob.blockPosition(), ModSounds.BREATH.get(), SoundSource.HOSTILE, 0.3F, 0.55F);
            return;
        }
    }
    private DreadEventManager() {}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/client/ClientScareState.java" <<'EOF'
package dev.harekuto.dreadstalker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.harekuto.dreadstalker.DreadstalkerMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Random;

public final class ClientScareState {
    private static final ResourceLocation JUMPSCARE = new ResourceLocation(DreadstalkerMod.MODID, "textures/gui/jumpscare.png");
    private static int ticks;
    private static int maxTicks;
    private static int type;
    private static float strength;
    private static long seed;

    public static void trigger(int newType, int duration, float newStrength, long newSeed) {
        type = newType;
        ticks = Math.max(ticks, duration);
        maxTicks = Math.max(1, duration);
        strength = Math.max(strength, newStrength);
        seed = newSeed;
    }

    public static void tick() {
        if (ticks > 0) ticks--;
        else strength = 0.0F;
    }

    public static boolean active() { return ticks > 0; }
    public static float intensity(float partialTick) {
        if (ticks <= 0) return 0.0F;
        float life = (ticks - partialTick) / (float) maxTicks;
        float pulse = 0.72F + 0.28F * Mth.sin((maxTicks - ticks + partialTick) * 2.45F);
        return Mth.clamp(life * pulse * strength, 0.0F, 1.0F);
    }

    public static void render(GuiGraphics graphics, float partialTick, int width, int height) {
        if (!active()) return;
        float a = intensity(partialTick);
        Random r = new Random(seed + ticks * 73428767L);

        if (type == 1) {
            graphics.fill(0, 0, width, height, ((int)(150 * a) << 24));
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Math.min(1.0F, 0.55F + a));
            float sx = (width + 28.0F) / 256.0F;
            float sy = (height + 28.0F) / 256.0F;
            int jitterX = r.nextInt(15) - 7;
            int jitterY = r.nextInt(15) - 7;
            graphics.pose().pushPose();
            graphics.pose().translate(jitterX - 14.0F, jitterY - 14.0F, 200.0F);
            graphics.pose().scale(sx, sy, 1.0F);
            graphics.blit(JUMPSCARE, 0, 0, 0, 0.0F, 0.0F, 256, 256, 256, 256);
            graphics.pose().popPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            for (int i = 0; i < 10; i++) {
                int y = r.nextInt(Math.max(1, height));
                int h = 1 + r.nextInt(3);
                int alpha = 25 + r.nextInt(60);
                graphics.fill(0, y, width, y + h, (alpha << 24) | 0x7a0000);
            }
        } else {
            int alpha = (int)(95 * a);
            graphics.fill(0, 0, width, height, (alpha << 24) | 0x100000);
            for (int i = 0; i < 6; i++) {
                int x = r.nextInt(Math.max(1, width));
                int w = 2 + r.nextInt(8);
                graphics.fill(x, 0, Math.min(width, x + w), height, ((20 + r.nextInt(35)) << 24) | 0x3b0000);
            }
        }
    }
    private ClientScareState() {}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/client/ClientModEvents.java" <<'EOF'
package dev.harekuto.dreadstalker.client;

import dev.harekuto.dreadstalker.DreadstalkerMod;
import dev.harekuto.dreadstalker.client.model.DreadstalkerModel;
import dev.harekuto.dreadstalker.client.render.DreadstalkerRenderer;
import dev.harekuto.dreadstalker.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DreadstalkerMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DreadstalkerModel.LAYER_LOCATION, DreadstalkerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DREADSTALKER.get(), DreadstalkerRenderer::new);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("dread_jumpscare", (gui, graphics, partialTick, width, height) ->
                ClientScareState.render(graphics, partialTick, width, height));
    }

    @Mod.EventBusSubscriber(modid = DreadstalkerMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class RuntimeEvents {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) ClientScareState.tick();
        }

        @SubscribeEvent
        public static void camera(ViewportEvent.ComputeCameraAngles event) {
            if (!ClientScareState.active()) return;
            float intensity = ClientScareState.intensity((float) event.getPartialTick());
            double t = System.nanoTime() * 0.000000028D;
            event.setYaw(event.getYaw() + (float)Math.sin(t * 17.0D) * 1.8F * intensity);
            event.setPitch(event.getPitch() + (float)Math.cos(t * 23.0D) * 1.3F * intensity);
            event.setRoll(event.getRoll() + (float)Math.sin(t * 29.0D) * 2.4F * intensity);
        }
    }
    private ClientModEvents() {}
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/client/model/DreadstalkerModel.java" <<'EOF'
package dev.harekuto.dreadstalker.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.harekuto.dreadstalker.DreadstalkerMod;
import dev.harekuto.dreadstalker.entity.DreadstalkerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class DreadstalkerModel<T extends DreadstalkerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DreadstalkerMod.MODID, "dreadstalker"), "main");
    private final ModelPart root;
    private final ModelPart pelvis;
    private final ModelPart spine;
    private final ModelPart chest;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftShin;
    private final ModelPart rightShin;

    public DreadstalkerModel(ModelPart root) {
        this.root = root;
        this.pelvis = root.getChild("pelvis");
        this.spine = pelvis.getChild("spine");
        this.chest = spine.getChild("chest");
        this.neck = chest.getChild("neck");
        this.head = neck.getChild("head");
        this.jaw = head.getChild("jaw");
        this.leftArm = chest.getChild("left_arm");
        this.rightArm = chest.getChild("right_arm");
        this.leftForearm = leftArm.getChild("left_forearm");
        this.rightForearm = rightArm.getChild("right_forearm");
        this.leftLeg = pelvis.getChild("left_leg");
        this.rightLeg = pelvis.getChild("right_leg");
        this.leftShin = leftLeg.getChild("left_shin");
        this.rightShin = rightLeg.getChild("right_shin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition pelvis = root.addOrReplaceChild("pelvis",
                CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, -4.0F, -2.8F, 8.0F, 8.0F, 5.6F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition spine = pelvis.addOrReplaceChild("spine",
                CubeListBuilder.create().texOffs(30, 48).addBox(-2.7F, -11.0F, -2.0F, 5.4F, 11.0F, 4.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition chest = spine.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(0, 64).addBox(-5.3F, -9.0F, -3.2F, 10.6F, 9.0F, 6.4F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, -10.0F, 0.0F));

        for (int i = 0; i < 5; i++) {
            float y = -1.5F - i * 1.45F;
            float spread = 4.3F - i * 0.35F;
            chest.addOrReplaceChild("rib_l_" + i,
                    CubeListBuilder.create().texOffs(112, 16).addBox(-spread, y, -3.75F, spread - 0.7F, 0.65F, 0.75F), PartPose.ZERO);
            chest.addOrReplaceChild("rib_r_" + i,
                    CubeListBuilder.create().texOffs(112, 16).addBox(0.7F, y, -3.75F, spread - 0.7F, 0.65F, 0.75F), PartPose.ZERO);
        }
        chest.addOrReplaceChild("sternum", CubeListBuilder.create().texOffs(112,16).addBox(-0.65F,-7.4F,-4.0F,1.3F,7.6F,0.9F), PartPose.ZERO);
        for (int i = 0; i < 4; i++) {
            chest.addOrReplaceChild("spike_" + i, CubeListBuilder.create().texOffs(58,64).addBox(-0.8F,-1.0F,-0.8F,1.6F,5.0F,1.6F),
                    PartPose.offsetAndRotation((i-1.5F)*2.2F,-7.5F+i*1.1F,2.6F,-0.95F,0.0F,(i-1.5F)*0.12F));
        }

        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(52,48).addBox(-2.2F,-6.0F,-2.2F,4.4F,6.0F,4.4F), PartPose.offset(0.0F,-8.0F,0.4F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0,0).addBox(-6.0F,-11.5F,-6.0F,12.0F,12.0F,12.0F,new CubeDeformation(0.25F)), PartPose.offset(0.0F,-5.0F,-0.6F));
        head.addOrReplaceChild("eye_left", CubeListBuilder.create().texOffs(112,0).addBox(-4.1F,-7.6F,-6.45F,2.6F,2.2F,0.7F), PartPose.ZERO);
        head.addOrReplaceChild("eye_right", CubeListBuilder.create().texOffs(112,0).addBox(1.5F,-7.6F,-6.45F,2.6F,2.2F,0.7F), PartPose.ZERO);
        head.addOrReplaceChild("mouth_void", CubeListBuilder.create().texOffs(96,0).addBox(-4.6F,-4.7F,-6.55F,9.2F,5.2F,0.65F), PartPose.ZERO);
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(76,36).addBox(-5.0F,-0.2F,-6.0F,10.0F,3.1F,8.0F), PartPose.offset(0.0F,-0.2F,0.0F));
        for (int i = 0; i < 6; i++) {
            float x = -3.9F + i * 1.55F;
            head.addOrReplaceChild("top_tooth_"+i, CubeListBuilder.create().texOffs(112,16).addBox(-0.45F,0.0F,-0.45F,0.9F,2.4F,0.9F), PartPose.offsetAndRotation(x,-4.4F,-6.65F,0.12F,0.0F,(i%2==0?-0.12F:0.12F)));
            jaw.addOrReplaceChild("bottom_tooth_"+i, CubeListBuilder.create().texOffs(112,16).addBox(-0.45F,-2.2F,-0.45F,0.9F,2.2F,0.9F), PartPose.offsetAndRotation(x,0.0F,-6.1F,-0.10F,0.0F,(i%2==0?0.12F:-0.12F)));
        }

        PartDefinition leftArm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(72,48).addBox(-2.0F,-1.5F,-2.0F,4.0F,13.0F,4.0F), PartPose.offsetAndRotation(6.0F,-6.5F,0.0F,0.10F,0.0F,-0.14F));
        PartDefinition rightArm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(72,48).mirror().addBox(-2.0F,-1.5F,-2.0F,4.0F,13.0F,4.0F), PartPose.offsetAndRotation(-6.0F,-6.5F,0.0F,0.10F,0.0F,0.14F));
        PartDefinition lf = leftArm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(88,48).addBox(-1.7F,0.0F,-1.7F,3.4F,14.0F,3.4F), PartPose.offsetAndRotation(0.0F,10.6F,0.0F,-0.15F,0.0F,0.08F));
        PartDefinition rf = rightArm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(88,48).mirror().addBox(-1.7F,0.0F,-1.7F,3.4F,14.0F,3.4F), PartPose.offsetAndRotation(0.0F,10.6F,0.0F,-0.15F,0.0F,-0.08F));
        addHand(lf, "l", false);
        addHand(rf, "r", true);

        PartDefinition leftLeg = pelvis.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0,84).addBox(-2.2F,0.0F,-2.2F,4.4F,14.0F,4.4F), PartPose.offsetAndRotation(2.35F,2.0F,0.0F,0.05F,0.0F,-0.03F));
        PartDefinition rightLeg = pelvis.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0,84).mirror().addBox(-2.2F,0.0F,-2.2F,4.4F,14.0F,4.4F), PartPose.offsetAndRotation(-2.35F,2.0F,0.0F,0.05F,0.0F,0.03F));
        PartDefinition ls = leftLeg.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(22,84).addBox(-1.8F,0.0F,-1.8F,3.6F,13.0F,3.6F), PartPose.offsetAndRotation(0.0F,13.0F,0.0F,-0.05F,0.0F,0.0F));
        PartDefinition rs = rightLeg.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(22,84).mirror().addBox(-1.8F,0.0F,-1.8F,3.6F,13.0F,3.6F), PartPose.offsetAndRotation(0.0F,13.0F,0.0F,-0.05F,0.0F,0.0F));
        addFoot(ls, "left_foot");
        addFoot(rs, "right_foot");
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addHand(PartDefinition forearm, String side, boolean mirror) {
        PartDefinition hand = forearm.addOrReplaceChild(side+"_hand", CubeListBuilder.create().texOffs(48,84).addBox(-2.8F,0.0F,-2.1F,5.6F,3.5F,4.2F), PartPose.offset(0.0F,13.0F,0.0F));
        for (int i=0;i<4;i++) {
            float x=-2.0F+i*1.35F;
            PartDefinition finger=hand.addOrReplaceChild(side+"_finger_"+i, CubeListBuilder.create().texOffs(70,84).addBox(-0.45F,0.0F,-0.55F,0.9F,5.2F,1.1F), PartPose.offsetAndRotation(x,2.8F,-0.4F,0.10F+(i%2)*0.08F,0.0F,(i-1.5F)*0.08F));
            finger.addOrReplaceChild(side+"_claw_"+i, CubeListBuilder.create().texOffs(112,16).addBox(-0.38F,0.0F,-0.42F,0.76F,3.0F,0.84F), PartPose.offsetAndRotation(0.0F,4.8F,0.0F,0.30F,0.0F,0.0F));
        }
    }

    private static void addFoot(PartDefinition shin, String name) {
        PartDefinition foot = shin.addOrReplaceChild(name, CubeListBuilder.create().texOffs(82,84).addBox(-2.4F,-1.2F,-5.0F,4.8F,3.0F,7.0F), PartPose.offset(0.0F,12.8F,-0.3F));
        for(int i=0;i<3;i++) foot.addOrReplaceChild(name+"_talon_"+i, CubeListBuilder.create().texOffs(112,16).addBox(-0.35F,-0.3F,-3.0F,0.7F,0.8F,3.2F), PartPose.offset(-1.4F+i*1.4F,0.2F,-4.6F));
    }

    @Override
    public ModelPart root() { return root; }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float walk = limbSwing * 0.62F;
        float amount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float breath = Mth.sin(ageInTicks * 0.085F);
        pelvis.y += breath * 0.28F;
        spine.xRot = -0.08F + breath * 0.025F;
        chest.xRot = 0.10F - breath * 0.045F;
        neck.yRot = Mth.sin(ageInTicks * 0.037F) * 0.09F;
        head.zRot = Mth.sin(ageInTicks * 0.052F) * 0.045F;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.72F;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.55F;
        jaw.xRot = 0.12F + Mth.max(0.0F, Mth.sin(ageInTicks * 0.11F)) * 0.08F;

        leftLeg.xRot = Mth.cos(walk) * 0.92F * amount;
        rightLeg.xRot = Mth.cos(walk + Mth.PI) * 0.92F * amount;
        leftShin.xRot = Mth.max(0.0F, Mth.sin(walk + 0.6F)) * 0.55F * amount;
        rightShin.xRot = Mth.max(0.0F, Mth.sin(walk + Mth.PI + 0.6F)) * 0.55F * amount;
        leftArm.xRot = -0.18F + Mth.cos(walk + Mth.PI) * 0.55F * amount;
        rightArm.xRot = -0.18F + Mth.cos(walk) * 0.55F * amount;
        leftForearm.xRot = -0.22F + Mth.sin(walk) * 0.16F * amount;
        rightForearm.xRot = -0.22F + Mth.sin(walk + Mth.PI) * 0.16F * amount;

        if (entity.isCrawling()) {
            pelvis.xRot = 0.42F;
            spine.xRot = 0.78F;
            chest.xRot = 0.72F;
            neck.xRot = -0.52F;
            head.xRot += -0.35F;
            leftArm.zRot = -0.72F;
            rightArm.zRot = 0.72F;
            leftArm.xRot = -1.05F + Mth.cos(walk) * 0.45F;
            rightArm.xRot = -1.05F + Mth.cos(walk + Mth.PI) * 0.45F;
            leftLeg.xRot = 0.85F + Mth.cos(walk + Mth.PI) * 0.34F;
            rightLeg.xRot = 0.85F + Mth.cos(walk) * 0.34F;
            jaw.xRot = 0.42F + Mth.sin(ageInTicks * 0.22F) * 0.08F;
        }

        if (entity.getAttackTicks() > 0) {
            float p = 1.0F - entity.getAttackTicks() / 12.0F;
            float swing = Mth.sin(p * Mth.PI);
            rightArm.xRot = -1.65F + swing * 1.3F;
            rightArm.zRot = 0.55F - swing * 0.95F;
            rightForearm.xRot = -0.9F;
            chest.yRot = -0.45F * swing;
            jaw.xRot = 0.72F;
        }

        if (entity.getScareTicks() > 0) {
            float pulse = Mth.sin((28 - entity.getScareTicks()) * 1.9F);
            chest.xRot = -0.18F;
            neck.xRot = -0.38F;
            head.xRot = -0.24F + pulse * 0.09F;
            head.zRot = pulse * 0.06F;
            jaw.xRot = 1.05F + pulse * 0.07F;
            leftArm.xRot = -1.0F;
            rightArm.xRot = -1.0F;
            leftArm.zRot = -0.95F;
            rightArm.zRot = 0.95F;
            leftForearm.xRot = -0.65F;
            rightForearm.xRot = -0.65F;
        }
    }
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/client/render/DreadstalkerRenderer.java" <<'EOF'
package dev.harekuto.dreadstalker.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.harekuto.dreadstalker.DreadstalkerMod;
import dev.harekuto.dreadstalker.client.model.DreadstalkerModel;
import dev.harekuto.dreadstalker.entity.DreadstalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class DreadstalkerRenderer extends MobRenderer<DreadstalkerEntity, DreadstalkerModel<DreadstalkerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DreadstalkerMod.MODID, "textures/entity/dreadstalker.png");

    public DreadstalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadstalkerModel<>(context.bakeLayer(DreadstalkerModel.LAYER_LOCATION)), 0.72F);
        addLayer(new DreadstalkerEyesLayer(this));
    }

    @Override
    protected void scale(DreadstalkerEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.04F, 1.04F, 1.04F);
    }

    @Override
    public ResourceLocation getTextureLocation(DreadstalkerEntity entity) { return TEXTURE; }
}
EOF

cat > "$ROOT/src/main/java/dev/harekuto/dreadstalker/client/render/DreadstalkerEyesLayer.java" <<'EOF'
package dev.harekuto.dreadstalker.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.harekuto.dreadstalker.DreadstalkerMod;
import dev.harekuto.dreadstalker.client.model.DreadstalkerModel;
import dev.harekuto.dreadstalker.entity.DreadstalkerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;

public final class DreadstalkerEyesLayer extends RenderLayer<DreadstalkerEntity, DreadstalkerModel<DreadstalkerEntity>> {
    private static final ResourceLocation EYES = new ResourceLocation(DreadstalkerMod.MODID, "textures/entity/dreadstalker_eyes.png");

    public DreadstalkerEyesLayer(RenderLayerParent<DreadstalkerEntity, DreadstalkerModel<DreadstalkerEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, DreadstalkerEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        VertexConsumer vc = buffer.getBuffer(RenderType.eyes(EYES));
        getParentModel().renderToBuffer(poseStack, vc, 15728640,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
EOF

cat > "$ROOT/generate_assets.py" <<'PY'
from pathlib import Path
import math, random, struct, zlib, wave
root = Path(__file__).parent / 'src/main/resources/assets/dreadstalker'

def png(path,w,h,pixels):
    raw=b''.join(b'\x00'+bytes(pixels[y*w*4:(y+1)*w*4]) for y in range(h))
    def chunk(t,d):
        return struct.pack('>I',len(d))+t+d+struct.pack('>I',zlib.crc32(t+d)&0xffffffff)
    data=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b'')
    path.write_bytes(data)

def body_texture():
    w=h=128; rnd=random.Random(1337); p=[0]*(w*h*4)
    for y in range(h):
        for x in range(w):
            n=rnd.randint(-13,13); crack=(x*7+y*11+rnd.randint(0,27))%43==0
            base=30 + int(10*math.sin(x*.31)+8*math.sin(y*.23))+n
            r=max(10,min(78,base+18+(25 if crack else 0)))
            g=max(5,min(42,base//2))
            b=max(6,min(38,base//2+2))
            i=(y*w+x)*4; p[i:i+4]=[r,g,b,255]
    # emissive eye UV region
    for y in range(0,16):
        for x in range(108,128):
            d=abs(x-118)+abs(y-7); glow=max(0,255-d*18)
            i=(y*w+x)*4; p[i:i+4]=[255,max(20,glow//4),max(8,glow//12),255]
    # bone/teeth region
    for y in range(16,32):
        for x in range(108,128):
            i=(y*w+x)*4; p[i:i+4]=[210+rnd.randint(0,35),190+rnd.randint(0,25),155+rnd.randint(0,20),255]
    # mouth/gore region
    for y in range(0,24):
        for x in range(92,108):
            i=(y*w+x)*4; p[i:i+4]=[55+rnd.randint(0,35),2+rnd.randint(0,8),5+rnd.randint(0,10),255]
    png(root/'textures/entity/dreadstalker.png',w,h,p)
    e=[0]*(w*h*4)
    for y in range(0,16):
        for x in range(108,128):
            i=(y*w+x)*4; e[i:i+4]=[255,20,8,255]
    png(root/'textures/entity/dreadstalker_eyes.png',w,h,e)

def jumpscare():
    w=h=256; rnd=random.Random(666); p=[0]*(w*h*4)
    for y in range(h):
        for x in range(w):
            dx=(x-128)/112; dy=(y-126)/126; inside=dx*dx+dy*dy<1
            i=(y*w+x)*4
            if not inside: p[i:i+4]=[0,0,0,255]; continue
            noise=rnd.randint(0,24); fiss=((x*13+y*7+rnd.randint(0,31))%67==0)
            p[i:i+4]=[28+noise+(45 if fiss else 0),7+noise//4,8+noise//5,255]
    # eye cavities + red cores
    for cx in (82,174):
        for y in range(68,111):
            for x in range(cx-28,cx+29):
                d=((x-cx)/28)**2+((y-88)/20)**2
                if d<1:
                    i=(y*w+x)*4; glow=max(0,int(255*(1-d)))
                    p[i:i+4]=[120+glow//2,4+glow//12,5,255]
    # vertical mouth
    for y in range(112,240):
        half=int(18+27*math.sin((y-112)/128*math.pi))
        for x in range(128-half,129+half):
            i=(y*w+x)*4; p[i:i+4]=[14,0,2,255]
    # teeth
    for row,y0,dirn in [(0,120,1),(1,214,-1)]:
        for k in range(8):
            cx=96+k*9
            length=18+(k%3)*5
            for yy in range(length):
                y=y0+yy*dirn
                width=max(1,4-yy//6)
                for x in range(cx-width,cx+width+1):
                    if 0<=x<w and 0<=y<h:
                        i=(y*w+x)*4; p[i:i+4]=[232,214,173,255]
    # blood streaks
    for _ in range(45):
        x=rnd.randrange(55,202); y0=rnd.randrange(105,220); length=rnd.randrange(4,36)
        for y in range(y0,min(h,y0+length)):
            i=(y*w+x)*4; p[i:i+4]=[115+rnd.randrange(80),0,5,230]
    png(root/'textures/gui/jumpscare.png',w,h,p)

def wav(name,duration,kind,seed):
    sr=44100; rnd=random.Random(seed); n=int(sr*duration); out=[]; low=0.0
    for i in range(n):
        t=i/sr; env=min(1,t*8)*min(1,(duration-t)*5)
        noise=rnd.uniform(-1,1); low=low*0.985+noise*0.015
        if kind=='whisper': v=(low*0.55+math.sin(2*math.pi*(71+7*math.sin(t*2))*t)*0.11)*env
        elif kind=='breath': v=(low*0.8+math.sin(2*math.pi*43*t)*0.08)*(0.55+0.45*math.sin(math.pi*t/duration))*env
        elif kind=='roar': v=(math.sin(2*math.pi*(48+18*math.sin(t*6))*t)*0.42+low*0.64+math.sin(2*math.pi*93*t)*0.14)*env
        elif kind=='jump': v=(low*0.8+math.sin(2*math.pi*(55+140*t)*t)*0.45+math.sin(2*math.pi*27*t)*0.25)*env
        elif kind=='step': v=(low*math.exp(-t*14)+math.sin(2*math.pi*58*t)*math.exp(-t*11))*0.72
        elif kind=='scrape': v=(low*0.75+math.sin(2*math.pi*(110+40*math.sin(t*8))*t)*0.22)*env
        else: v=(low*0.7+math.sin(2*math.pi*34*t)*0.2)*env
        out.append(max(-1,min(1,v)))
    path=root/'sounds'/f'{name}.wav'
    with wave.open(str(path),'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(sr)
        f.writeframes(b''.join(struct.pack('<h',int(v*30000)) for v in out))

body_texture(); jumpscare()
wav('ambient_whisper',2.4,'whisper',1)
wav('breath',2.8,'breath',2)
wav('roar',1.9,'roar',3)
wav('jumpscare',1.15,'jump',4)
wav('step',0.45,'step',5)
wav('scrape',1.65,'scrape',6)
wav('death',2.3,'roar',7)
PY

cd "$ROOT"
python3 generate_assets.py
if ! command -v ffmpeg >/dev/null 2>&1; then
  sudo apt-get update -qq
  sudo apt-get install -y -qq ffmpeg
fi
for f in src/main/resources/assets/dreadstalker/sounds/*.wav; do
  ffmpeg -y -loglevel error -i "$f" -c:a libvorbis -q:a 4 "${f%.wav}.ogg"
  rm "$f"
done
rm generate_assets.py

printf 'Generated Dreadstalker project at %s\n' "$ROOT"
find src/main -type f | sort
