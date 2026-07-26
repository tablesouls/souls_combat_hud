package net.tablesouls.souls_combat_hud.util;

public class ColorHelper {

    public record RGB(float r, float g, float b) {
        private static final float EPSILON = 0.001f;

        public static RGB fromPackedInt(int colorInt) {
            float r = ((colorInt >> 16) & 0xFF) / 255.0f;
            float g = ((colorInt >> 8) & 0xFF) / 255.0f;
            float b = (colorInt & 0xFF) / 255.0f;
            return new RGB(r, g, b);
        }

        public static RGB from255(int r, int g, int b) {
            return new RGB(r / 255.0f, g / 255.0f, b / 255.0f);
        }

        public static RGB fromNormalized(float r, float g, float b) {
            return new RGB(r, g, b);
        }

        public boolean matchesNormalized(float r, float g, float b) {
            return Math.abs(this.r - r) < EPSILON
                    && Math.abs(this.g - g) < EPSILON
                    && Math.abs(this.b - b) < EPSILON;
        }

        public boolean matches255(int r, int g, int b) {
            return matchesNormalized(r / 255.0f, g / 255.0f, b / 255.0f);
        }

        public boolean matchesPackedInt(int colorInt) {
            return matchesNormalized(
                    ((colorInt >> 16) & 0xFF) / 255.0f,
                    ((colorInt >> 8) & 0xFF) / 255.0f,
                    (colorInt & 0xFF) / 255.0f
            );
        }

        public int toPackedInt() {
            int ri = Math.round(r * 255.0f) & 0xFF;
            int gi = Math.round(g * 255.0f) & 0xFF;
            int bi = Math.round(b * 255.0f) & 0xFF;
            return (ri << 16) | (gi << 8) | bi;
        }
    }
}