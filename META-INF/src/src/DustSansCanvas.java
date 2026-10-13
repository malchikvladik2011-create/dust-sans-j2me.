Java
import javax.microedition.lcdui.*;
import java.util.Random;

public class DustSansCanvas extends Canvas implements Runnable {
    private DustSansMidlet midlet;
    private boolean running = true;

    private static final int STATE_BATTLE_DIALOG = 1;
    private static final int STATE_DODGE_BOX = 2;
    private static final int STATE_PLAYER_TURN = 3;
    private static final int STATE_GAMEOVER = 4;
    private static final int STATE_WIN = 5;
    private int gameState = STATE_BATTLE_DIALOG;

    private int hp = 100;
    private int maxHp = 100;
    private int karma = 0;
    private int healUses = 4;

    private int sansHp = 100;
    private int sansMaxHp = 100;

    private int boxX = 40, boxY = 120, boxW = 160, boxH = 100;
    private int soulX = 120, soulY = 170;
    private int soulVy = 0;
    private boolean blueMode = false;
    private boolean isGrounded = false;

    private boolean keyUp = false, keyDown = false, keyLeft = false, keyRight = false;

    private Random rnd = new Random();
    private int attackTimer = 0;
    private String floatingText = "";
    private int floatingTimer = 0;
    private boolean screenDark = false;

    public DustSansCanvas(DustSansMidlet m) {
        this.midlet = m;
        setFullScreenMode(true);
        new Thread(this).start();
    }

    public void run() {
        while (running) {
            updateGame();
            repaint();
            try { Thread.sleep(30); } catch (InterruptedException e) {}
        }
    }

    private void updateGame() {
        int speed = 3;
        if (keyLeft) soulX -= speed;
        if (keyRight) soulX += speed;

        if (blueMode) {
            soulVy += 1;
            soulY += soulVy;
            if (soulY >= boxY + boxH - 12) {
                soulY = boxY + boxH - 12;
                soulVy = 0;
                isGrounded = true;
            }
        } else {
            if (keyUp) soulY -= speed;
            if (keyDown) soulY += speed;
        }

        if (soulX < boxX + 4) soulX = boxX + 4;
        if (soulX > boxX + boxW - 12) soulX = boxX + boxW - 12;
        if (soulY < boxY + 4) soulY = boxY + 4;
        if (soulY > boxY + boxH - 12) soulY = boxY + boxH - 12;

        if (floatingTimer > 0) {
            floatingTimer--;
            if (floatingTimer == 0) floatingText = "";
        }

        if (gameState == STATE_BATTLE_DIALOG) {
            attackTimer++;
            if (attackTimer > 50) {
                gameState = STATE_DODGE_BOX;
                attackTimer = 0;
                screenDark = (rnd.nextInt(10) < 4);
                if (screenDark) {
                    soulX = boxX + boxW / 2;
                    soulY = boxY + boxH / 2;
                }
            }
        } else if (gameState == STATE_DODGE_BOX) {
            attackTimer++;
            if (karma > 0 && attackTimer % 12 == 0) {
                karma--;
                hp--;
                if (hp <= 0) { hp = 0; gameState = STATE_GAMEOVER; }
            }
            if (attackTimer > 140) {
                gameState = STATE_PLAYER_TURN;
                attackTimer = 0;
            }
        }
    }

    protected void keyPressed(int keyCode) {
        if (keyCode == KEY_NUM4 || keyCode == Canvas.LEFT) keyLeft = true;
        if (keyCode == KEY_NUM6 || keyCode == Canvas.RIGHT) keyRight = true;
        if (keyCode == KEY_NUM8 || keyCode == Canvas.DOWN) { if (!blueMode) keyDown = true; }
        if (keyCode == KEY_NUM2 || keyCode == Canvas.UP) {
            if (blueMode && isGrounded) { soulVy = -9; isGrounded = false; }
            else if (!blueMode) keyUp = true;
        }

        if (keyCode == KEY_POUND || keyCode == '#') {
            if (healUses > 0 && hp < maxHp) {
                hp += 30;
                if (hp > maxHp) hp = maxHp;
                healUses--;
                karma = Math.max(0, karma - 5);
                showFloating("HEAL +30", 0xFF00FF00);
            }
        }

        if (keyCode == KEY_NUM5) {
            blueMode = !blueMode;
            showFloating(blueMode ? "BLUE SOUL" : "RED SOUL", 0xFF00FFFF);
        }

        if (gameState == STATE_PLAYER_TURN && (keyCode == KEY_NUM5 || keyCode == Canvas.FIRE)) {
            int dmg = 12 + rnd.nextInt(15);
            if (rnd.nextInt(100) < 60) {
                showFloating("МИМО", 0xFFFFFFFF);
            } else {
                sansHp -= dmg;
                showFloating("-" + dmg, 0xFFFF0000);
                if (sansHp <= 5) {
                    gameState = STATE_WIN;
                }
            }
            if (gameState != STATE_WIN) {
                gameState = STATE_BATTLE_DIALOG;
                attackTimer = 0;
            }
        }
    }

    protected void keyReleased(int keyCode) {
        if (keyCode == KEY_NUM4 || keyCode == Canvas.LEFT) keyLeft = false;
        if (keyCode == KEY_NUM6 || keyCode == Canvas.RIGHT) keyRight = false;
        if (keyCode == KEY_NUM8 || keyCode == Canvas.DOWN) keyDown = false;
        if (keyCode == KEY_NUM2 || keyCode == Canvas.UP) keyUp = false;
    }

    private void showFloating(String text, int color) {
        floatingText = text;
        floatingTimer = 25;
    }

    protected void paint(Graphics g) {
        g.setColor(screenDark && gameState == STATE_DODGE_BOX ? 0x050505 : 0x111122);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(0xFFFFFF);
        g.fillRect(100, 35, 40, 40);
        g.setColor(0x00AAFF);
        g.fillRect(108, 45, 10, 8);

        g.setColor(0x555555);
        g.fillRect(70, 85, 100, 6);
        g.setColor(0x00FF00);
        int sansBarW = (int)((double)sansHp / sansMaxHp * 100);
        g.fillRect(70, 85, Math.max(0, sansBarW), 6);
        g.drawString("SANS: " + sansHp, 70, 70, Graphics.LEFT | Graphics.TOP);

        g.setColor(screenDark ? 0x550000 : 0xFFFFFF);
        g.drawRect(boxX, boxY, boxW, boxH);

        if (gameState == STATE_DODGE_BOX) {
            g.setColor(0xFFA500);
            g.fillRect(boxX + 20 + (attackTimer * 2) % 120, boxY + 25, 6, 50);

            g.setColor(0xFFFFFF);
            g.fillRect(boxX + boxW - 30 - (attackTimer * 2) % 100, boxY + 40, 6, 50);

            g.setColor(0xAAAAAA);
            g.fillRect(boxX + 15, boxY + 10, 24, 16);
            g.setColor(0x00FFFF);
            g.fillRect(boxX + 39, boxY + 16, attackTimer * 3, 4);
        }

        g.setColor(blueMode ? 0x0000FF : 0xFFFF00);
        g.fillRect(soulX, soulY, 8, 8);

        g.setColor(0xFFFFFF);
        g.drawString("HP: " + hp + (karma > 0 ? " (KR:" + karma + ")" : ""), 10, 280, Graphics.LEFT | Graphics.TOP);
        g.drawString("HEAL(#): " + healUses, 130, 280, Graphics.LEFT | Graphics.TOP);

        if (gameState == STATE_BATTLE_DIALOG) {
            g.drawString("* Dust Sans watches you closely.", boxX + 5, boxY + 15, Graphics.LEFT | Graphics.TOP);
        } else if (gameState == STATE_PLAYER_TURN) {
            g.drawString("YOUR TURN: Press 5 to Attack", boxX + 5, boxY + 15, Graphics.LEFT | Graphics.TOP);
        } else if (gameState == STATE_GAMEOVER) {
            g.setColor(0xFF0000);
            g.drawString("YOU DIED", 95, 150, Graphics.LEFT | Graphics.TOP);
        } else if (gameState == STATE_WIN) {
            g.setColor(0x00FF00);
            g.drawString("* Sans runs away in fear!", 35, 150, Graphics.LEFT | Graphics.TOP);
        }

        if (floatingTimer > 0) {
            g.setColor(0xFFFF00);
            g.drawString(floatingText, soulX - 5, soulY - 18, Graphics.LEFT | Graphics.TOP);
        }
    }
}
