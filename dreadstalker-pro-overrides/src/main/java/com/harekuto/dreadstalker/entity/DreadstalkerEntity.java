package com.harekuto.dreadstalker.entity;

import com.harekuto.dreadstalker.config.DreadstalkerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DreadstalkerEntity extends Monster {
    public static final int STATE_DORMANT = 0;
    public static final int STATE_STALKING = 1;
    public static final int STATE_HUNTING = 2;
    public static final int STATE_RAGE = 3;

    private static final EntityDataAccessor<Boolean> DATA_OBSERVED =
            SynchedEntityData.defineId(DreadstalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(DreadstalkerEntity.class, EntityDataSerializers.INT);

    private int teleportCooldown = 140;
    private int ambientCooldown = 90;
    private int unseenTicks;
    private int observedTicks;
    private int rageTicks;
    private boolean wasObserved;

    public DreadstalkerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 28;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_OBSERVED, false);
        entityData.define(DATA_STATE, STATE_DORMANT);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 82.0D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.15D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.FOLLOW_RANGE, 52.0D)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.64D);
    }

    public static boolean checkSpawnRules(EntityType<DreadstalkerEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason == MobSpawnType.NATURAL) {
            if (!DreadstalkerConfig.COMMON.naturalSpawning.get()) return false;
            int chance = DreadstalkerConfig.COMMON.naturalSpawnChance.get();
            if (chance > 1 && random.nextInt(chance) != 0) return false;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL
                && pos.getY() < level.getSeaLevel() + 24
                && level.getMaxLocalRawBrightness(pos) <= DreadstalkerConfig.COMMON.maximumSpawnLight.get()
                && Monster.isDarkEnoughToSpawn(level, pos, random)
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new DreadAttackGoal(this, 1.20D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.64D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 36.0F, 1.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;

        if (teleportCooldown > 0) teleportCooldown--;
        if (ambientCooldown > 0) ambientCooldown--;
        if (rageTicks > 0) rageTicks--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            setObserved(false);
            setHorrorState(STATE_DORMANT);
            unseenTicks = 0;
            observedTicks = 0;
            wasObserved = false;
            restoreMovementSpeed(0.29D);
            return;
        }

        double distance = distanceTo(target);
        boolean observed = target instanceof Player player && isPlayerLookingAtMe(player);
        setObserved(observed);

        boolean frozen = observed
                && rageTicks <= 0
                && DreadstalkerConfig.COMMON.freezeWhenObserved.get()
                && distance <= DreadstalkerConfig.COMMON.freezeDistance.get();

        if (frozen) {
            observedTicks++;
            unseenTicks = 0;
            setHorrorState(STATE_STALKING);
            restoreMovementSpeed(0.29D);
            getNavigation().stop();
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x * 0.025D, motion.y, motion.z * 0.025D);
            getLookControl().setLookAt(target, 38.0F, 12.0F);
        } else {
            observedTicks = 0;
            unseenTicks++;
            updateHuntState(distance);
        }

        if (DreadstalkerConfig.COMMON.teleporting.get() && !frozen) {
            if (wasObserved && !observed && teleportCooldown == 0 && distance > 6.0D && distance < 30.0D) {
                if (teleportBehindTarget(target)) {
                    teleportCooldown = 210 + random.nextInt(150);
                    unseenTicks = 0;
                }
            } else if (teleportCooldown == 0 && unseenTicks > 60 && distance > 11.0D && distance < 45.0D) {
                if (teleportNearTarget(target)) {
                    teleportCooldown = 230 + random.nextInt(180);
                    unseenTicks = 0;
                }
            }
        }

        if (DreadstalkerConfig.COMMON.darknessEffects.get() && !frozen && distance < 17.0D && tickCount % 100 == 0) {
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 65, 0, true, false, false), this);
        }

        if (ambientCooldown == 0 && distance < 32.0D) {
            SoundEvent sound = random.nextBoolean() ? SoundEvents.WARDEN_HEARTBEAT : SoundEvents.SCULK_SHRIEKER_SHRIEK;
            float volume = (float) Mth.clamp(0.32D + (32.0D - distance) / 48.0D, 0.32D, 0.78D);
            playSound(sound, volume, 0.34F + random.nextFloat() * 0.16F);
            ambientCooldown = 150 + random.nextInt(240);
        }

        wasObserved = observed;
    }

    private void updateHuntState(double distance) {
        double speed;
        if (rageTicks > 0 || distance < 4.5D) {
            setHorrorState(STATE_RAGE);
            speed = 0.44D;
        } else if (distance < 13.0D) {
            setHorrorState(STATE_HUNTING);
            speed = 0.39D;
        } else {
            setHorrorState(STATE_STALKING);
            speed = distance > 28.0D ? 0.33D : 0.35D;
        }
        restoreMovementSpeed(speed);
    }

    private void restoreMovementSpeed(double speed) {
        AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && Math.abs(attribute.getBaseValue() - speed) > 0.0001D) {
            attribute.setBaseValue(speed);
        }
    }

    private boolean isPlayerLookingAtMe(Player player) {
        if (player.isSpectator()) return false;
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 toMe = new Vec3(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
        double distance = toMe.length();
        if (distance < 0.001D) return true;
        toMe = toMe.normalize();
        double angularAllowance = Mth.clamp(0.11D / Math.max(distance, 1.0D), 0.0022D, 0.055D);
        double threshold = 1.0D - angularAllowance;
        return look.dot(toMe) > threshold && player.hasLineOfSight(this);
    }

    private boolean teleportBehindTarget(LivingEntity target) {
        Vec3 look = target.getViewVector(1.0F).normalize();
        Vec3 side = new Vec3(-look.z, 0.0D, look.x);
        for (int i = 0; i < 8; i++) {
            double back = 6.0D + random.nextDouble() * 4.0D;
            double strafe = (random.nextDouble() - 0.5D) * 5.0D;
            double x = target.getX() - look.x * back + side.x * strafe;
            double z = target.getZ() - look.z * back + side.z * strafe;
            double y = target.getY() + random.nextInt(3) - 1;
            if (teleportWithEffects(x, y, z)) return true;
        }
        return false;
    }

    private boolean teleportNearTarget(LivingEntity target) {
        for (int i = 0; i < 12; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 9.0D + random.nextDouble() * 8.0D;
            double x = target.getX() + Math.cos(angle) * radius;
            double z = target.getZ() + Math.sin(angle) * radius;
            double y = target.getY() + random.nextInt(5) - 2;
            if (teleportWithEffects(x, y, z)) return true;
        }
        return false;
    }

    private boolean teleportWithEffects(double x, double y, double z) {
        if (!(level() instanceof ServerLevel server)) return false;
        double oldX = getX();
        double oldY = getY() + getBbHeight() * 0.55D;
        double oldZ = getZ();
        if (!randomTeleport(x, y, z, true)) return false;

        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, oldX, oldY, oldZ, 20, 0.35D, 0.8D, 0.35D, 0.02D);
        server.sendParticles(ParticleTypes.SMOKE, oldX, oldY, oldZ, 14, 0.28D, 0.65D, 0.28D, 0.015D);
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + getBbHeight() * 0.55D, getZ(), 24, 0.4D, 0.9D, 0.4D, 0.02D);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 0.22F, 0.30F + random.nextFloat() * 0.08F);
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (hit && entity instanceof LivingEntity living) {
            rageTicks = Math.max(rageTicks, 55);
            setHorrorState(STATE_RAGE);
            if (DreadstalkerConfig.COMMON.darknessEffects.get()) {
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 45, 0, true, false, false), this);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24, 0, true, false, false), this);
            }
            playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 0.72F, 0.62F + random.nextFloat() * 0.1F);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && isAlive()) {
            rageTicks = Math.max(rageTicks, 45);
            setHorrorState(STATE_RAGE);
            teleportCooldown = Math.min(teleportCooldown, 55);
        }
        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DreadTeleportCooldown", teleportCooldown);
        tag.putInt("DreadAmbientCooldown", ambientCooldown);
        tag.putInt("DreadRageTicks", rageTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DreadTeleportCooldown")) teleportCooldown = Mth.clamp(tag.getInt("DreadTeleportCooldown"), 0, 1200);
        if (tag.contains("DreadAmbientCooldown")) ambientCooldown = Mth.clamp(tag.getInt("DreadAmbientCooldown"), 0, 1200);
        if (tag.contains("DreadRageTicks")) rageTicks = Mth.clamp(tag.getInt("DreadRageTicks"), 0, 400);
    }

    private void setObserved(boolean observed) {
        if (entityData.get(DATA_OBSERVED) != observed) entityData.set(DATA_OBSERVED, observed);
    }

    public boolean isObserved() {
        return entityData.get(DATA_OBSERVED);
    }

    private void setHorrorState(int state) {
        int safe = Mth.clamp(state, STATE_DORMANT, STATE_RAGE);
        if (entityData.get(DATA_STATE) != safe) entityData.set(DATA_STATE, safe);
    }

    public int getHorrorState() {
        return entityData.get(DATA_STATE);
    }

    public float getThreatLevel() {
        return switch (getHorrorState()) {
            case STATE_RAGE -> 1.0F;
            case STATE_HUNTING -> 0.76F;
            case STATE_STALKING -> 0.46F;
            default -> 0.12F;
        };
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.74F;
    }

    private static final class DreadAttackGoal extends MeleeAttackGoal {
        private final DreadstalkerEntity stalker;

        private DreadAttackGoal(DreadstalkerEntity stalker, double speed, boolean longMemory) {
            super(stalker, speed, longMemory);
            this.stalker = stalker;
        }

        private boolean canMove() {
            return !stalker.isObserved() || stalker.rageTicks > 0 || !DreadstalkerConfig.COMMON.freezeWhenObserved.get();
        }

        @Override
        public boolean canUse() {
            return canMove() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return canMove() && super.canContinueToUse();
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return 7.0D + target.getBbWidth() * target.getBbWidth();
        }
    }
}
