package link.botwmcs.fizzy.ui.element.animate;

import org.joml.Matrix3x2fStack;

public final class ElementTransform {
    private float offsetX;
    private float offsetY;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float rotationRad;
    private float pivotX;
    private float pivotY;
    private boolean pivotSet;
    private boolean pivotAbsolute;
    private float colorR = 1.0f;
    private float colorG = 1.0f;
    private float colorB = 1.0f;
    private float colorA = 1.0f;
    private boolean colorSet;

    public ElementTransform copy() {
        ElementTransform copy = new ElementTransform();
        copy.offsetX = this.offsetX;
        copy.offsetY = this.offsetY;
        copy.scaleX = this.scaleX;
        copy.scaleY = this.scaleY;
        copy.rotationRad = this.rotationRad;
        copy.pivotX = this.pivotX;
        copy.pivotY = this.pivotY;
        copy.pivotSet = this.pivotSet;
        copy.pivotAbsolute = this.pivotAbsolute;
        copy.colorR = this.colorR;
        copy.colorG = this.colorG;
        copy.colorB = this.colorB;
        copy.colorA = this.colorA;
        copy.colorSet = this.colorSet;
        return copy;
    }

    public ElementTransform clearOffset() {
        this.offsetX = 0.0f;
        this.offsetY = 0.0f;
        return this;
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public void offset(float dx, float dy) {
        this.offsetX += dx;
        this.offsetY += dy;
    }

    public void setOffset(float x, float y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    public float scaleX() {
        return scaleX;
    }

    public float scaleY() {
        return scaleY;
    }

    public void scale(float sx, float sy) {
        this.scaleX *= sx;
        this.scaleY *= sy;
    }

    public void setScale(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
    }

    public float rotationRad() {
        return rotationRad;
    }

    public void rotateRad(float rad) {
        this.rotationRad += rad;
    }

    public void rotateDeg(float deg) {
        this.rotationRad += (float) Math.toRadians(deg);
    }

    public void pivot(float x, float y) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotSet = true;
        this.pivotAbsolute = false;
    }

    public void pivotAbsolute(float x, float y) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotSet = true;
        this.pivotAbsolute = true;
    }

    public void setColor(float r, float g, float b, float a) {
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
        this.colorSet = true;
    }

    public void multiplyColor(float r, float g, float b, float a) {
        this.colorR *= r;
        this.colorG *= g;
        this.colorB *= b;
        this.colorA *= a;
        this.colorSet = true;
    }

    public boolean hasColor() {
        return colorSet;
    }

    public float colorR() {
        return colorR;
    }

    public float colorG() {
        return colorG;
    }

    public float colorB() {
        return colorB;
    }

    public float colorA() {
        return colorA;
    }

    public boolean applyColor(net.minecraft.client.gui.GuiGraphicsExtractor g) {
        return false;
    }

    public void applyToPose(Matrix3x2fStack pose, int leftPx, int topPx, int widthPx, int heightPx) {
        boolean hasScale = scaleX != 1.0f || scaleY != 1.0f;
        boolean hasRotation = rotationRad != 0.0f;
        if (!hasScale && !hasRotation) {
            return;
        }

        float pivotAbsX;
        float pivotAbsY;
        if (pivotSet) {
            if (pivotAbsolute) {
                pivotAbsX = pivotX;
                pivotAbsY = pivotY;
            } else {
                pivotAbsX = leftPx + pivotX;
                pivotAbsY = topPx + pivotY;
            }
        } else {
            pivotAbsX = leftPx + widthPx * 0.5f;
            pivotAbsY = topPx + heightPx * 0.5f;
        }

        pose.translate(pivotAbsX, pivotAbsY);
        if (hasRotation) {
            pose.rotate(rotationRad);
        }
        if (hasScale) {
            pose.scale(scaleX, scaleY);
        }
        pose.translate(-pivotAbsX, -pivotAbsY);
    }
}
