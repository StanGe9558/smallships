package com.talhanation.smallships.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

public class MathUtil {
    public static double normalizedDotProduct(Vector3d v1, Vector3d v2) {
        return v1.dotProduct(v2) / v1.length() * v2.length();
    }

    public static float getPitch(Vector3d motion) {
        double y = motion.y;
        return (float)Math.toDegrees(Math.atan2(y, Math.sqrt(motion.x * motion.x + motion.z * motion.z)));
    }

    public static float getYaw(Vector3d motion) {
        return (float)Math.toDegrees(Math.atan2(-motion.x, motion.z));
    }

    public static float lerpAngle(float perc, float start, float end) {
        return start + perc * MathHelper.wrapDegrees(end - start);
    }

    public static float lerpAngle180(float perc, float start, float end) {
        if (degreesDifferenceAbs(start, end) > 90.0D)
            end += 180.0F;
        return start + perc * MathHelper.wrapDegrees(end - start);
    }

    public static double lerpAngle180(double perc, double start, double end) {
        if (degreesDifferenceAbs(start, end) > 90.0D)
            end += 180.0D;
        return start + perc * MathHelper.wrapDegrees(end - start);
    }

    public static double lerpAngle(double perc, double start, double end) {
        return start + perc * MathHelper.wrapDegrees(end - start);
    }

    public static double degreesDifferenceAbs(double p_203301_0_, double p_203301_1_) {
        return Math.abs(wrapSubtractDegrees(p_203301_0_, p_203301_1_));
    }

    public static double wrapSubtractDegrees(double p_203302_0_, double p_203302_1_) {
        return MathHelper.wrapDegrees(p_203302_1_ - p_203302_0_);
    }

    public static Vector3d rotationToVector(double yaw, double pitch) {
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(pitch);
        double xzLen = Math.cos(pitch);
        double x = -xzLen * Math.sin(yaw);
        double y = Math.sin(pitch);
        double z = xzLen * Math.cos(-yaw);
        return new Vector3d(x, y, z);
    }

    public static Vector3d rotationToVector(double yaw, double pitch, double size) {
        Vector3d vec = rotationToVector(yaw, pitch);
        return vec.scale(size / vec.length());
    }

    public static EulerAngles toEulerAngles(Quaternion q) {
        EulerAngles angles = new EulerAngles();
        double sinr_cosp = (2.0F * (q.getW() * q.getZ() + q.getX() * q.getY()));
        double cosr_cosp = (1.0F - 2.0F * (q.getZ() * q.getZ() + q.getX() * q.getX()));
        angles.roll = Math.toDegrees(Math.atan2(sinr_cosp, cosr_cosp));
        double sinp = (2.0F * (q.getW() * q.getX() - q.getY() * q.getZ()));
        if (Math.abs(sinp) >= 0.98D) {
            angles.pitch = -Math.toDegrees(Math.signum(sinp) * Math.PI / 2.0D);
        } else {
            angles.pitch = -Math.toDegrees(Math.asin(sinp));
        }
        double siny_cosp = (2.0F * (q.getW() * q.getY() + q.getZ() * q.getX()));
        double cosy_cosp = (1.0F - 2.0F * (q.getX() * q.getX() + q.getY() * q.getY()));
        angles.yaw = Math.toDegrees(Math.atan2(siny_cosp, cosy_cosp));
        return angles;
    }

    public static float fastInvSqrt(float number) {
        float f = 0.5F * number;
        int i = Float.floatToIntBits(number);
        i = 1597463007 - (i >> 1);
        number = Float.intBitsToFloat(i);
        return number * (1.5F - f * number * number);
    }

    public static Quaternion normalizeQuaternion(Quaternion q) {
        float f = q.getX() * q.getX() + q.getY() * q.getY() + q.getZ() * q.getZ() + q.getW() * q.getW();
        float x = q.getX();
        float y = q.getY();
        float z = q.getZ();
        float w = q.getW();
        if (f > 1.0E-6F) {
            float f1 = fastInvSqrt(f);
            x *= f1;
            y *= f1;
            z *= f1;
            w *= f1;
            return new Quaternion(x, y, z, w);
        }
        return new Quaternion(0.0F, 0.0F, 0.0F, 0.0F);
    }

    public static Quaternion toQuaternion(double yaw, double pitch, double roll) {
        yaw = Math.toRadians(yaw);
        pitch = -Math.toRadians(pitch);
        roll = Math.toRadians(roll);
        double cy = Math.cos(yaw * 0.5D);
        double sy = Math.sin(yaw * 0.5D);
        double cp = Math.cos(pitch * 0.5D);
        double sp = Math.sin(pitch * 0.5D);
        double cr = Math.cos(roll * 0.5D);
        double sr = Math.sin(roll * 0.5D);
        float w = (float)(cr * cp * cy + sr * sp * sy);
        float z = (float)(sr * cp * cy - cr * sp * sy);
        float x = (float)(cr * sp * cy + sr * cp * sy);
        float y = (float)(cr * cp * sy - sr * sp * cy);
        return new Quaternion(x, y, z, w);
    }

    public static Quaternion lerpQ(float perc, Quaternion start, Quaternion end) {
        start = normalizeQuaternion(start);
        end = normalizeQuaternion(end);
        double dot = (start.getX() * end.getX() + start.getY() * end.getY() + start.getZ() * end.getZ() + start.getW() * end.getW());
        if (dot < 0.0D) {
            end = new Quaternion(-end.getX(), -end.getY(), -end.getZ(), -end.getW());
            dot = -dot;
        }
        double DOT_THRESHOLD = 0.9995D;
        if (dot > DOT_THRESHOLD) {
            Quaternion quaternion1 = new Quaternion(start.getX() * (1.0F - perc) + end.getX() * perc, start.getY() * (1.0F - perc) + end.getY() * perc, start.getZ() * (1.0F - perc) + end.getZ() * perc, start.getW() * (1.0F - perc) + end.getW() * perc);
            return normalizeQuaternion(quaternion1);
        }
        double theta_0 = Math.acos(dot);
        double theta = theta_0 * perc;
        double sin_theta = Math.sin(theta);
        double sin_theta_0 = Math.sin(theta_0);
        float s0 = (float)(Math.cos(theta) - dot * sin_theta / sin_theta_0);
        float s1 = (float)(sin_theta / sin_theta_0);
        Quaternion quaternion = new Quaternion(start.getX() * s0 + end.getX() * s1, start.getY() * s0 + end.getY() * s1, start.getZ() * s0 + end.getZ() * s1, start.getW() * s0 + end.getW() * s1);
        return normalizeQuaternion(quaternion);
    }

    public static class EulerAngles {
        public double pitch;

        public double yaw;

        public double roll;

        public EulerAngles() {}

        public EulerAngles(EulerAngles a) {
            this.pitch = a.pitch;
            this.yaw = a.yaw;
            this.roll = a.roll;
        }

        public EulerAngles copy() {
            return new EulerAngles(this);
        }

        public String toString() {
            return "EulerAngles{pitch=" + this.pitch + ", yaw=" + this.yaw + ", roll=" + this.roll + '}';
        }
    }
}
