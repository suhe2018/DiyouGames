#!/usr/bin/env python3
"""
Render pixel-art style game UI mockups for DiyouGames.
Generates 5 screens: MainMenu, CharacterSelect, Battle, Result, Settings.
"""

from PIL import Image, ImageDraw, ImageFont
import math, os

# ─── Font helpers ─────────────────────────────────────────────────────────────
_FONT_ZH = "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc"   # CJK support
_FONT_EN = "/usr/share/fonts/truetype/dejavu/DejaVuSansMono-Bold.ttf"

def _font(size, cjk=True):
    path = _FONT_ZH if cjk else _FONT_EN
    try:
        return ImageFont.truetype(path, size)
    except:
        return ImageFont.load_default()

# ─── Palette ─────────────────────────────────────────────────────────────────
BG_DARK    = (13, 8, 32)
BG_MID     = (26, 16, 64)
SURFACE    = (37, 21, 80)
SURF_VAR   = (48, 26, 94)
PRIMARY    = (61, 26, 110)
PRIMARY_V  = (106, 47, 160)
GOLD       = (255, 215, 0)
GOLD_DIM   = (200, 160, 0)
SECONDARY  = (200, 120, 32)
P1_COLOR   = (64, 128, 255)
P2_COLOR   = (255, 64, 64)
HP_HIGH    = (50, 200, 80)
HP_MID     = (220, 180, 20)
HP_LOW     = (220, 50, 50)
ENERGY_C   = (80, 100, 220)
ENERGY_F   = (255, 220, 50)
WHITE      = (255, 255, 255)
GREY       = (180, 160, 220)
HINT       = (120, 88, 144)
RED        = (200, 48, 48)
GREEN      = (32, 120, 64)
BLUE       = (48, 96, 128)
DARK_RED   = (96, 0, 0)
STAR_Y     = (255, 240, 60)

W, H = 480, 270   # game virtual resolution
SCALE = 3         # output scale factor

def make_canvas():
    img = Image.new("RGB", (W * SCALE, H * SCALE), BG_DARK)
    d   = ImageDraw.Draw(img)
    return img, d

def px(x, y, w=1, h=1, color=WHITE, d=None):
    """Draw a pixel-art rectangle in scaled coordinates."""
    d.rectangle([x*SCALE, y*SCALE, (x+w)*SCALE-1, (y+h)*SCALE-1], fill=color)

def text_px(d, txt, x, y, size=8, color=WHITE, mono=True):
    """Draw text with CJK support."""
    font = _font(size * SCALE)
    d.text((x*SCALE, y*SCALE), txt, fill=color, font=font)

def text_center(d, txt, cy, size=8, color=WHITE):
    """Center text horizontally with CJK support."""
    font = _font(size * SCALE)
    bbox = d.textbbox((0,0), txt, font=font)
    tw = bbox[2] - bbox[0]
    x = (W * SCALE - tw) // 2
    d.text((x, cy*SCALE), txt, fill=color, font=font)

def draw_stars(d):
    positions = [(40,20),(80,35),(150,15),(220,40),(300,10),(370,30),(420,50),(100,55),(250,60),(460,20)]
    for (sx, sy) in positions:
        px(sx, sy, 2, 2, (255,255,255,180), d)

def draw_mountains(d):
    # far mountains
    pts_far = [(0,180),(60,130),(130,160),(200,100),(260,140),(340,90),(400,130),(480,120),(480,210),(0,210)]
    d.polygon([(x*SCALE, y*SCALE) for (x,y) in pts_far], fill=(40,15,60))
    # near mountains
    pts_near = [(0,200),(80,165),(160,185),(240,150),(320,170),(400,155),(480,165),(480,210),(0,210)]
    d.polygon([(x*SCALE, y*SCALE) for (x,y) in pts_near], fill=(55,25,75))

def draw_sky(d):
    for y in range(H):
        ratio = y / H
        r = int(20 + (100-20)*ratio * 0.7)
        g = int(10 + (40-10)*ratio * 0.7)
        b = int(40 + (100-40)*ratio * 0.7)
        d.rectangle([0, y*SCALE, W*SCALE, (y+1)*SCALE], fill=(r,g,b))

def bar(d, x, y, w, h, fill, bg=(20,10,30), border=GREY):
    d.rectangle([x*SCALE, y*SCALE, (x+w)*SCALE, (y+h)*SCALE], fill=bg)
    d.rectangle([x*SCALE, y*SCALE, (x+fill)*SCALE, (y+h)*SCALE], fill=HP_HIGH if fill > w*0.5 else (HP_MID if fill > w*0.25 else HP_LOW))
    d.rectangle([x*SCALE, y*SCALE, (x+w)*SCALE, (y+h)*SCALE], outline=border, width=1)

def btn(d, x, y, w, h, label, bg=PRIMARY, border=PRIMARY_V, txtcolor=WHITE, sz=7):
    d.rectangle([x*SCALE, y*SCALE, (x+w)*SCALE, (y+h)*SCALE], fill=bg)
    d.rectangle([x*SCALE, y*SCALE, (x+w)*SCALE, (y+h)*SCALE], outline=border, width=SCALE)
    font = _font(sz * SCALE)
    bbox = d.textbbox((0,0), label, font=font)
    tw = (bbox[2]-bbox[0]) // SCALE
    cx = x + (w - tw) // 2
    text_px(d, label, cx, y+2, sz, txtcolor)

def char_sprite(d, cx, by, body_c, armor_c, skin_c, hair_c, eye_c, weapon_c, flip=False):
    """Draw a tiny pixel character."""
    p = 2  # pixel size
    def r(rx, ry, rw, rh, c):
        ox = cx + rx if not flip else cx - rx - rw
        d.rectangle([(ox*SCALE), ((by+ry)*SCALE), ((ox+rw)*SCALE)-1, ((by+ry+rh)*SCALE)-1], fill=c)

    r(-3, -30, 6, 3,  hair_c)   # hair
    r(-3, -27, 6, 8,  skin_c)   # head
    r(-2, -24, 2, 2,  eye_c)    # eye L
    r(1,  -24, 2, 2,  eye_c)    # eye R
    r(-5, -19, 10,10, armor_c)  # torso
    r(-5, -10, 10,1,  (GOLD_DIM))  # belt
    r(-4, -9,  4, 9,  body_c)   # left leg
    r(1,  -9,  4, 9,  body_c)   # right leg
    r(-7, -19, 3, 7,  skin_c)   # left arm
    r(4,  -19, 3, 7,  skin_c)   # right arm
    r(7,  -22, 2, 12, weapon_c) # weapon

# ─── SCREEN 1: Main Menu ─────────────────────────────────────────────────────
def screen_main_menu():
    img, d = make_canvas()
    draw_sky(d)
    draw_mountains(d)
    draw_stars(d)

    # Ground
    d.rectangle([0, 200*SCALE, W*SCALE, H*SCALE], fill=(80,50,120))
    d.rectangle([0, 200*SCALE, W*SCALE, 204*SCALE], fill=(120,80,160))

    # Title shadow
    text_center(d, "像素决斗", 52, 20, (80,0,120))
    text_center(d, "像素决斗", 50, 20, GOLD)
    text_center(d, "PIXEL  DUEL", 75, 9, SECONDARY)

    # Decorative swords
    for i in range(8):
        px(i*2 + 100, 90, 1, 1, GOLD if i%2==0 else WHITE, d)

    # Buttons
    btn(d, 180, 110, 120, 22, "  ⚔ 单人 VS 电脑  ", bg=PRIMARY, border=PRIMARY_V, sz=7)
    btn(d, 180, 140, 120, 22, "  ⚔ 本地双人对战  ", bg=PRIMARY, border=PRIMARY_V, sz=7)
    btn(d, 200, 170, 80,  18, "  ⚙  设置       ", bg=SURF_VAR, border=HINT, sz=7)

    # Version
    text_px(d, "v1.0.0", 430, 260, 6, HINT)

    # Pixel border decoration
    for i in range(0, W, 16):
        px(i, 0, 8, 2, GOLD_DIM, d)
        px(i, H-2, 8, 2, GOLD_DIM, d)

    return img

# ─── SCREEN 2: Character Select ──────────────────────────────────────────────
def screen_char_select():
    img, d = make_canvas()
    d.rectangle([0,0,W*SCALE,H*SCALE], fill=BG_DARK)

    # Grid bg
    for i in range(0, W, 32):
        d.rectangle([i*SCALE, 0, (i+1)*SCALE, H*SCALE], fill=(20,12,40))
    for j in range(0, H, 32):
        d.rectangle([0, j*SCALE, W*SCALE, (j+1)*SCALE], fill=(20,12,40))

    text_center(d, "选择角色", 4, 10, GOLD)

    # P1 Card
    d.rectangle([8*SCALE, 20*SCALE, 228*SCALE, 250*SCALE], fill=SURFACE)
    d.rectangle([8*SCALE, 20*SCALE, 228*SCALE, 250*SCALE], outline=PRIMARY_V, width=SCALE)
    text_px(d, "玩家 1  PLAYER 1", 12, 22, 7, P1_COLOR)

    # P1 character sprite - Warrior (blue armor)
    char_sprite(d, 80, 155, (80,120,160),(160,160,180),(220,180,140),(80,50,30),(50,100,200),(200,200,220))
    # Arrow buttons
    btn(d, 12,  80, 20, 30, "◀", bg=SURF_VAR, border=HINT, sz=8)
    btn(d, 100, 80, 20, 30, "▶", bg=SURF_VAR, border=HINT, sz=8)

    text_px(d, "战士  Warrior",    15, 175, 7, WHITE)
    text_px(d, "厚甲重剑，冲锋在前",   15, 187, 6, GREY)
    text_px(d, "武器: 长剑 Sword",  15, 200, 6, SECONDARY)
    # Stats
    text_px(d, "HP",  15, 212, 6, GREY)
    bar(d, 35, 213, 80, 5, 70)
    text_px(d, "SPD", 15, 221, 6, GREY)
    bar(d, 35, 222, 80, 5, 45, bg=(20,10,30), border=GREY)
    d.rectangle([35*SCALE, 222*SCALE, (35+45)*SCALE, (222+5)*SCALE], fill=P1_COLOR)
    text_px(d, "DEF", 15, 230, 6, GREY)
    bar(d, 35, 231, 80, 5, 40, bg=(20,10,30), border=GREY)
    d.rectangle([35*SCALE, 231*SCALE, (35+40)*SCALE, (231+5)*SCALE], fill=SECONDARY)
    text_px(d, "绝招: 猛力斩",      15, 242, 6, GOLD)

    # VS divider
    text_center(d, "VS", 125, 14, SECONDARY)

    # P2 Card (Ninja - dark)
    d.rectangle([252*SCALE, 20*SCALE, 472*SCALE, 250*SCALE], fill=SURFACE)
    d.rectangle([252*SCALE, 20*SCALE, 472*SCALE, 250*SCALE], outline=(100,30,30), width=SCALE)
    text_px(d, "AI 对手  ENEMY", 256, 22, 7, P2_COLOR)

    char_sprite(d, 324, 155, (30,30,40),(50,50,60),(200,160,120),(20,20,20),(220,30,30),(180,180,60), flip=True)
    btn(d, 256, 80, 20, 30, "◀", bg=SURF_VAR, border=HINT, sz=8)
    btn(d, 344, 80, 20, 30, "▶", bg=SURF_VAR, border=HINT, sz=8)

    text_px(d, "忍者  Ninja",      258, 175, 7, WHITE)
    text_px(d, "身法迅捷，出刀如风",   258, 187, 6, GREY)
    text_px(d, "武器: 双刃 Daggers",258, 200, 6, SECONDARY)
    text_px(d, "HP",  258, 212, 6, GREY)
    bar(d, 278, 213, 80, 5, 40)
    d.rectangle([278*SCALE, 213*SCALE, (278+40)*SCALE, (213+5)*SCALE], fill=HP_LOW)
    text_px(d, "SPD", 258, 221, 6, GREY)
    bar(d, 278, 222, 80, 5, 75, bg=(20,10,30), border=GREY)
    d.rectangle([278*SCALE, 222*SCALE, (278+75)*SCALE, (222+5)*SCALE], fill=HP_HIGH)
    text_px(d, "DEF", 258, 230, 6, GREY)
    bar(d, 278, 231, 80, 5, 10, bg=(20,10,30), border=GREY)
    d.rectangle([278*SCALE, 231*SCALE, (278+10)*SCALE, (231+5)*SCALE], fill=HP_LOW)
    text_px(d, "绝招: 分身术",     258, 242, 6, GOLD)

    # Difficulty row
    text_px(d, "AI 难度:", 140, 256, 6, GREY)
    btn(d, 195, 254, 28, 14, "简单", bg=GREEN,    border=(20,80,40), sz=6)
    btn(d, 226, 254, 28, 14, "普通", bg=SECONDARY,border=(140,80,20), sz=6)
    btn(d, 257, 254, 28, 14, "困难", bg=(100,20,20), border=RED, sz=6)
    btn(d, 288, 254, 28, 14, "专家", bg=DARK_RED, border=RED, sz=6)

    # Fight button
    btn(d, 145, 257, 140, 0, "", bg=(0,0,0), sz=1)  # clear
    btn(d, 155, 256, 170, 12, "  ⚔  开始决斗！  ", bg=SECONDARY, border=GOLD, sz=7)

    return img

# ─── SCREEN 3: Battle ────────────────────────────────────────────────────────
def screen_battle():
    img, d = make_canvas()
    draw_sky(d)
    draw_mountains(d)
    draw_stars(d)

    gY = int(H * 0.75)
    d.rectangle([0, gY*SCALE, W*SCALE, H*SCALE], fill=(80,50,120))
    d.rectangle([0, gY*SCALE, W*SCALE, (gY+4)*SCALE], fill=(120,80,160))
    for i in range(0, W, 32):
        d.rectangle([i*SCALE, (gY+6)*SCALE, (i+1)*SCALE, H*SCALE], fill=(70,45,110))

    # Draw fighters
    # P1 - Warrior (left)
    char_sprite(d, 100, gY, (80,120,160),(160,160,180),(220,180,140),(80,50,30),(50,100,200),(200,200,220))
    # P2 - Ninja (right, flipped)
    char_sprite(d, 380, gY, (30,30,40),(50,50,60),(200,160,120),(20,20,20),(220,30,30),(180,180,60), flip=True)

    # Shadows
    d.ellipse([(82*SCALE, (gY-2)*SCALE), (118*SCALE, (gY+2)*SCALE)], fill=(0,0,0,120))
    d.ellipse([(362*SCALE,(gY-2)*SCALE), (398*SCALE, (gY+2)*SCALE)], fill=(0,0,0,120))

    # HUD: P1 HP bar
    text_px(d, "战士", 8, 4, 7, P1_COLOR)
    bar(d, 8, 15, 180, 10, 145)
    # Energy bar P1
    d.rectangle([8*SCALE, 28*SCALE, 188*SCALE, 33*SCALE], fill=(20,20,40))
    d.rectangle([8*SCALE, 28*SCALE, (8+120)*SCALE, 33*SCALE], fill=ENERGY_C)

    # HUD: P2 HP bar (right side)
    text_px(d, "忍者", 400, 4, 7, P2_COLOR)
    d.rectangle([292*SCALE, 15*SCALE, 472*SCALE, 25*SCALE], fill=(20,10,30))
    d.rectangle([292*SCALE, 15*SCALE, (292+60)*SCALE, 25*SCALE], fill=HP_LOW)   # low HP for drama
    d.rectangle([292*SCALE, 15*SCALE, 472*SCALE, 25*SCALE], outline=GREY, width=1)
    # Energy P2
    d.rectangle([292*SCALE, 28*SCALE, 472*SCALE, 33*SCALE], fill=(20,20,40))
    d.rectangle([(472-180)*SCALE, 28*SCALE, 472*SCALE, 33*SCALE], fill=ENERGY_F)  # full energy!

    # Timer box (center)
    d.rectangle([215*SCALE, 5*SCALE, 265*SCALE, 35*SCALE], fill=(20,10,40))
    d.rectangle([215*SCALE, 5*SCALE, 265*SCALE, 35*SCALE], outline=PRIMARY_V, width=SCALE)
    text_center(d, "42", 10, 14, WHITE)
    text_px(d, "R2", 228, 5, 6, (180,150,220))

    # Round wins dots
    for i in range(3):
        c = P1_COLOR if i == 0 else (60,50,80)
        d.rectangle([(220+i*14)*SCALE, 33*SCALE, (226+i*14)*SCALE, 37*SCALE], fill=c)

    # Hit effect sparkle
    for i in range(8):
        angle = i * 45 * math.pi / 180
        hx = 280 + int(math.cos(angle)*12)
        hy = gY - 20 + int(math.sin(angle)*8)
        px(hx, hy, 2, 2, STAR_Y, d)
    text_px(d, "-25", 268, gY-35, 8, STAR_Y)

    # Control buttons P1 (bottom left)
    btn(d, 8,   220, 30, 20, "◀",  bg=SURF_VAR, border=HINT, sz=9)
    btn(d, 42,  220, 30, 20, "跳", bg=(32,100,50), border=(20,60,30), sz=7)
    btn(d, 76,  220, 30, 20, "▶",  bg=SURF_VAR, border=HINT, sz=9)
    btn(d, 8,   196, 28, 20, "绝招",bg=(128,96,0), border=GOLD, sz=6)
    btn(d, 40,  196, 28, 20, "防御",bg=BLUE, border=(30,60,80), sz=6)
    btn(d, 72,  196, 36, 24, "攻击",bg=RED, border=(150,20,20), sz=8)

    # Pause button
    btn(d, 223, 5,  34, 0, "", bg=(0,0,0), sz=1)
    btn(d, 222, 37, 36, 12, " ⏸ ",bg=SURF_VAR, border=HINT, sz=7)

    return img

# ─── SCREEN 4: Result ────────────────────────────────────────────────────────
def screen_result():
    img, d = make_canvas()
    d.rectangle([0,0,W*SCALE,H*SCALE], fill=BG_DARK)

    # Stars background
    for i in range(20):
        sx = (i*23 + 7) % W
        sy = (i*17 + 5) % 120
        px(sx, sy, 2, 2, (100+i*5,80+i*3,30+i*8), d)

    # Victory banner
    d.rectangle([60*SCALE, 30*SCALE, 420*SCALE, 80*SCALE], fill=(40,20,80))
    d.rectangle([60*SCALE, 30*SCALE, 420*SCALE, 80*SCALE], outline=GOLD, width=2*SCALE)
    text_center(d, "战士  胜利！", 35, 18, GOLD)
    text_center(d, "P1 WINS", 60, 9, GREY)

    # Score
    text_px(d, "2", 195, 90, 24, P1_COLOR)
    text_center(d, ":", 90, 24, WHITE)
    text_px(d, "1", 263, 90, 24, P2_COLOR)

    # Stats card
    d.rectangle([50*SCALE, 140*SCALE, 430*SCALE, 220*SCALE], fill=SURFACE)
    d.rectangle([50*SCALE, 140*SCALE, 430*SCALE, 220*SCALE], outline=PRIMARY_V, width=SCALE)

    # P1 stats
    text_px(d, "战士",       60, 145, 7, P1_COLOR)
    text_px(d, "回合胜利: 2", 60, 158, 6, WHITE)
    text_px(d, "总伤害: 320", 60, 168, 6, WHITE)
    text_px(d, "绝招使用: 2", 60, 178, 6, WHITE)
    text_px(d, "精准命中率:",  60, 188, 6, GREY)
    bar(d, 60, 198, 100, 6, 72)

    # P2 stats
    text_px(d, "忍者",       260, 145, 7, P2_COLOR)
    text_px(d, "回合胜利: 1", 260, 158, 6, WHITE)
    text_px(d, "总伤害: 198", 260, 168, 6, WHITE)
    text_px(d, "绝招使用: 1", 260, 178, 6, WHITE)
    text_px(d, "精准命中率:",  260, 188, 6, GREY)
    bar(d, 260, 198, 100, 6, 55)

    # Divider
    d.rectangle([235*SCALE, 140*SCALE, 237*SCALE, 220*SCALE], fill=SURF_VAR)

    # Buttons
    btn(d, 150, 228, 180, 20, "  ⚔  再来一局  ", bg=SECONDARY, border=GOLD, sz=8)
    btn(d, 160, 252, 160, 16, "  主菜单       ", bg=PRIMARY,   border=PRIMARY_V, sz=7)

    return img

# ─── SCREEN 5: Settings ──────────────────────────────────────────────────────
def screen_settings():
    img, d = make_canvas()
    d.rectangle([0,0,W*SCALE,H*SCALE], fill=BG_DARK)

    text_center(d, "游戏设置", 8, 12, GOLD)

    # Settings card
    d.rectangle([60*SCALE, 35*SCALE, 420*SCALE, 240*SCALE], fill=SURFACE)
    d.rectangle([60*SCALE, 35*SCALE, 420*SCALE, 240*SCALE], outline=PRIMARY_V, width=SCALE)

    rows = [
        ("开启音效",   True,  75),
        ("音效音量",   None,  95,  0.8),
        ("开启背景音乐",True, 115),
        ("音乐音量",   None, 135,  0.6),
        ("震动反馈",   True, 155),
    ]
    for row in rows:
        label = row[0]; y = row[2]
        text_px(d, label, 72, y, 7, WHITE)
        if row[1] is True:
            # Toggle switch ON
            d.rounded_rectangle([340*SCALE, y*SCALE, 380*SCALE, (y+12)*SCALE], radius=6*SCALE, fill=HP_HIGH)
            d.ellipse([(368*SCALE,(y+2)*SCALE),(378*SCALE,(y+10)*SCALE)], fill=WHITE)
        elif row[1] is None:
            # Seek bar
            vol = row[3]
            d.rectangle([160*SCALE, (y+4)*SCALE, 380*SCALE, (y+8)*SCALE], fill=(30,20,60))
            d.rectangle([160*SCALE, (y+4)*SCALE, int((160+220*vol)*SCALE), (y+8)*SCALE], fill=PRIMARY_V)
            d.ellipse([int((160+220*vol-5)*SCALE),(y+1)*SCALE, int((160+220*vol+5)*SCALE),(y+11)*SCALE], fill=GREY)

    # Back button
    btn(d, 190, 248, 100, 18, "  返回  ", bg=PRIMARY, border=PRIMARY_V, sz=8)

    return img

# ─── Composite all 5 screens ─────────────────────────────────────────────────
def main():
    screens = [
        ("01_main_menu",    screen_main_menu()),
        ("02_char_select",  screen_char_select()),
        ("03_battle",       screen_battle()),
        ("04_result",       screen_result()),
        ("05_settings",     screen_settings()),
    ]

    out_dir = "/home/user/DiyouGames/screenshots"
    os.makedirs(out_dir, exist_ok=True)

    paths = []
    for name, img in screens:
        p = f"{out_dir}/{name}.png"
        img.save(p)
        paths.append(p)
        print(f"  ✓ {p}  ({img.width}x{img.height})")

    # Also make a combined preview sheet (2x3 grid)
    cols, rows = 3, 2
    pad = 12
    thumb_w = W * SCALE // 2
    thumb_h = H * SCALE // 2
    sheet_w = cols * thumb_w + (cols+1)*pad
    sheet_h = rows * thumb_h + (rows+1)*pad
    sheet = Image.new("RGB", (sheet_w, sheet_h), (10,5,20))
    for i, (_, img) in enumerate(screens):
        thumb = img.resize((thumb_w, thumb_h), Image.NEAREST)
        col = i % cols
        row = i // cols
        x = pad + col*(thumb_w+pad)
        y = pad + row*(thumb_h+pad)
        sheet.paste(thumb, (x, y))
    sheet_path = f"{out_dir}/preview_all_screens.png"
    sheet.save(sheet_path)
    print(f"\n  ✓ Preview sheet: {sheet_path}  ({sheet_w}x{sheet_h})")
    return sheet_path

if __name__ == "__main__":
    main()
