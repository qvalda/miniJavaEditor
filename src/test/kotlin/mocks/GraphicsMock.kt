package mocks

import base.BaseTest.Companion.assertFail
import java.awt.*
import java.awt.image.ImageObserver
import java.text.AttributedCharacterIterator

class GraphicsMock : Graphics() {
    override fun create(): Graphics {
        assertFail("Not yet implemented")
    }

    override fun translate(x: Int, y: Int) {
        assertFail("Not yet implemented")
    }

    override fun getColor(): Color {
        assertFail("Not yet implemented")
    }

    override fun setColor(c: Color?) {
        assertFail("Not yet implemented")
    }

    override fun setPaintMode() {
        assertFail("Not yet implemented")
    }

    override fun setXORMode(c1: Color?) {
        assertFail("Not yet implemented")
    }

    override fun getFont(): Font {
        assertFail("Not yet implemented")
    }

    override fun setFont(font: Font?) {
        assertFail("Not yet implemented")
    }

    override fun getFontMetrics(f: Font?): FontMetrics {
        assertFail("Not yet implemented")
    }

    override fun getClipBounds(): Rectangle {
        assertFail("Not yet implemented")
    }

    override fun clipRect(x: Int, y: Int, width: Int, height: Int) {
        assertFail("Not yet implemented")
    }

    override fun setClip(x: Int, y: Int, width: Int, height: Int) {
        assertFail("Not yet implemented")
    }

    override fun setClip(clip: Shape?) {
        assertFail("Not yet implemented")
    }

    override fun getClip(): Shape {
        assertFail("Not yet implemented")
    }

    override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        assertFail("Not yet implemented")
    }

    override fun fillRect(x: Int, y: Int, width: Int, height: Int) {
        assertFail("Not yet implemented")
    }

    override fun clearRect(x: Int, y: Int, width: Int, height: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
        assertFail("Not yet implemented")
    }

    override fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawOval(x: Int, y: Int, width: Int, height: Int) {
        assertFail("Not yet implemented")
    }

    override fun fillOval(x: Int, y: Int, width: Int, height: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
        assertFail("Not yet implemented")
    }

    override fun fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawPolyline(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawPolygon(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) {
        assertFail("Not yet implemented")
    }

    override fun fillPolygon(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawString(str: String, x: Int, y: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawString(iterator: AttributedCharacterIterator?, x: Int, y: Int) {
        assertFail("Not yet implemented")
    }

    override fun drawImage(img: Image?, x: Int, y: Int, observer: ImageObserver?): Boolean {
        assertFail("Not yet implemented")
    }

    override fun drawImage(img: Image?, x: Int, y: Int, width: Int, height: Int, observer: ImageObserver?): Boolean {
        assertFail("Not yet implemented")
    }

    override fun drawImage(img: Image?, x: Int, y: Int, bgcolor: Color?, observer: ImageObserver?): Boolean {
        assertFail("Not yet implemented")
    }

    override fun drawImage(img: Image?, x: Int, y: Int, width: Int, height: Int, bgcolor: Color?, observer: ImageObserver?): Boolean {
        assertFail("Not yet implemented")
    }

    override fun drawImage(img: Image?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, observer: ImageObserver?): Boolean {
        assertFail("Not yet implemented")
    }

    override fun drawImage(img: Image?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, bgcolor: Color?, observer: ImageObserver?): Boolean {
        assertFail("Not yet implemented")
    }

    override fun dispose() {
        assertFail("Not yet implemented")
    }
}