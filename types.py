"""Contains various data types used throughout Pyre"""

import math


class Coordinate(tuple):
    """Represents a 2D integer Cartesian coordinate"""

    def __new__(cls, x, y):
        return tuple.__new__(cls, (x, y))

    @classmethod
    def from_tuple(cls, value):
        """Constructs a Coordinate from a tuple"""
        return Coordinate(value[0], value[1])

    @property
    def x(self):
        """Returns the x component of the Coordinate"""
        return self[0]

    @property
    def y(self):
        """Returns the y component of the Coordinate"""
        return self[1]

    def direction_to(self, other):
        """Returns a cardinal direction from this Coordinate to another"""
        return Coordinate(max(-1, min(other[0] - self[0], 1)),
                          max(-1, min(other[1] - self[1], 1)))

    def directions_to(self, other):
        """Returns the cardinal directions from this Coordinate to another"""
        dx, dy = self.direction_to(other)
        if dx == 0 and dy == 0:
            return [Coordinate(dx, dy)]
        elif dx == 0 and dy != 0:
            return [Coordinate(dx, dy) for dx in [0, -1, 1]]
        elif dx != 0 and dy == 0:
            return [Coordinate(dx, dy) for dy in [0, -1, 1]]
        else:
            return [Coordinate(dx, dy), Coordinate(dx, 0), Coordinate(0, dy)]

    def manhattan_distance(self, other):
        """Returns the L_1 distance between this Coordinate to another"""
        return abs(self[0] - other[0]) + abs(self[1] - other[1])

    def euclidean_distance(self, other):
        """Returns the L_2 distance between this Coordinate to another"""
        return math.hypot(self[0] - other[0], self[1] - other[1])

    def chebyshev_distance(self, other):
        """Returns the L_inf distance between this Coordinate to another"""
        return max(abs(self[0] - other[0]), abs(self[1] - other[1]))

    def dot(self, other):
        """Returns the dot product of two Coordinates"""
        return self[0] * other[0] + self[1] * other[1]

    def is_orthognal(self):
        """Returns true if one and only one component is zero"""
        return (self[0] == 0) ^ (self[1] == 0)

    def __abs__(self):
        return math.hypot(self[0], self[1])

    def __add__(self, other):
        return Coordinate(self[0] + other[0], self[1] + other[1])

    def __radd__(self, other):
        return Coordinate(other[0] + self[0], other[1] + self[1])

    def __sub__(self, other):
        return Coordinate(self[0] - other[0], self[1] - other[1])

    def __rsub__(self, other):
        return Coordinate(other[0] - self[0], other[1] - self[1])


class Direction(object):
    """A collection of Coordinate representing cardinal directions"""

    North = Coordinate(0, -1)
    East = Coordinate(1, 0)
    South = Coordinate(0, 1)
    West = Coordinate(-1, 0)
    Northeast = Coordinate(1, -1)
    Southeast = Coordinate(1, 1)
    Southwest = Coordinate(-1, 1)
    Northwest = Coordinate(-1, -1)
    Origin = Coordinate(0, 0)

    @staticmethod
    def key_to_dir(key):
        """Translates a key character to a cardinal direction Coordinate"""

        key = key.lower()
        delta = None

        if key == 'k' or key == '8':
            delta = Direction.North
        elif key == 'l' or key == '6':
            delta = Direction.East
        elif key == 'j' or key == '2':
            delta = Direction.South
        elif key == 'h' or key == '4':
            delta = Direction.West
        elif key == 'u' or key == '9':
            delta = Direction.Northeast
        elif key == 'n' or key == '3':
            delta = Direction.Southeast
        elif key == 'b' or key == '1':
            delta = Direction.Southwest
        elif key == 'y' or key == '7':
            delta = Direction.Northwest
        elif key == '.' or key == '5':
            delta = Direction.Origin

        return delta


class Color(object):
    """A collection of ANSI escape codes for colors"""

    Aqua = 51
    Beige = 230
    Black = 16
    Blue = 21
    Brown = 130
    Chartreuse = 118
    Coral = 209
    Crimson = 161
    Cyan = 51
    Darkblue = 18
    Darkgray = 145
    Darkgreen = 22
    Darkmagenta = 90
    Darkorange = 208
    Darkred = 88
    Darkturquoise = 44
    Darkviolet = 92
    Darkpink = 198
    Dimgray = 59
    Forestgreen = 28
    Fuchsia = 201
    Gold = 220
    Gray = 244
    Green = 28
    Hotpink = 205
    Indigo = 54
    Lavender = 189
    Lawngreen = 82
    Lightblue = 152
    Lightcoral = 210
    Lightcyan = 195
    Lightgreen = 120
    Lightgray = 252
    Lightpink = 217
    Lightsalmon = 216
    Lime = 46
    Limegreen = 77
    Magenta = 201
    Mediumblue = 20
    Mediumorchid = 134
    Midnightblue = 17
    Olive = 100
    Oliverab = 64
    Orange = 214
    Orangered = 202
    Orchid = 170
    Peachpuff = 223
    Pink = 218
    Plum = 182
    Purple = 90
    Red = 196
    Salmon = 209
    Seagreen = 29
    Silver = 145
    Skyblue = 116
    Slategray = 66
    Smoke = 240
    Tan = 180
    Teal = 30
    Turquoise = 80
    Violet = 213
    White = 231
    Whitesmoke = 255
    Yellow = 226

    @classmethod
    def by_name(cls, name):
        """Returns the ansi code given by name"""
        name = name.lower()
        for attr in dir(cls):
            if attr.lower() == name:
                return getattr(cls, attr)

    @classmethod
    def try_by_name(cls, name):
        """Attempts to return the code by name, otherwise parses an int"""

        color = cls.by_name(name)
        if not color:
            color = int(name)
        return color

    _SYS_RGB = [(0, 0, 0),
                (0xAA, 0, 0),
                (0, 0xAA, 0),
                (0xAA, 0x55, 0),
                (0, 0, 0xAA),
                (0xAA, 0, 0xAA),
                (0, 0xAA, 0xAA),
                (0xAA, 0xAA, 0xAA),
                (0x55, 0x55, 0x55),
                (0xFF, 0x55, 0xFF),
                (0x55, 0xFF, 0x55),
                (0xFF, 0xFF, 0x55),
                (0x55, 0x55, 0xFF),
                (0xFF, 0x55, 0xFF),
                (0x55, 0xFF, 0xFF),
                (0xFF, 0xFF, 0xFF)]
    _CUBE_RGB_VALS = [0, 95, 135, 175, 215, 255]

    @classmethod
    def to_rgb(cls, code):
        """Converts an ansi 256 color code to an rgb int 3-triple"""
        assert code is None or 0 <= code < 256

        if code is None:
            return (0, 0, 0, 0)
        elif code < 16:
            return cls._SYS_RGB[code]
        elif code < 16 + 6 ** 3:
            red, green, blue = _to_cube(code)
            return tuple(cls._CUBE_RGB_VALS[x] for x in [red, green, blue])
        else:
            gray = 8 + 10 * (code - 16 - 6 ** 3)
            return gray, gray, gray

    @classmethod
    def to_ansi(cls, char, color, bg_color=None):
        """Uses ansi escape sequences to colorize a character"""
        fg_ansi = '38;5;{}'.format(color)
        bg_ansi = ';48;5;{}'.format(bg_color) if bg_color is not None else ''
        return '\x1b[{}{}m{}\x1b[0m'.format(fg_ansi, bg_ansi, char)


def _to_cube(code):
    assert 0 <= code < 256

    red, code = divmod(code - 16, 36)
    green, blue = divmod(code, 6)
    return red, green, blue


def _by_cube(red, green, blue):
    assert 0 <= red < 6
    assert 0 <= green < 6
    assert 0 <= blue < 6

    return red * 36 + green * 6 + blue + 16


class Glyph(tuple):
    """Represents a character-color pair as a tuple of chr and int"""

    def __new__(cls, char, color=Color.White, bg_color=None, sprite_id=None):
        return tuple.__new__(cls, (char, color, bg_color))

    def __init__(self, char, color=Color.White, bg_color=None, sprite_id=None):
        tuple.__init__(self, (char, color, bg_color))
        self.ansi = Color.to_ansi(char, color, bg_color)
        self.rgb = Color.to_rgb(color)
        self.rgb_bg = Color.to_rgb(bg_color)
        self.sprite_id = sprite_id

    @property
    def char(self):
        """Returns the character part of the Glyph"""
        return self[0]

    @property
    def color(self):
        """Returns the color part of the Glyph"""
        return self[1]

    @property
    def bg_color(self):
        """Returns the background color of the Glyph"""
        return self[2]

    def rewritten(self, char):
        """Returns a Glyph with the same value but new char"""
        return Glyph(char, self[1], self[2], self.sprite_id)

    def recolored(self, color):
        """Returns a Glyph with the same value but new color"""
        return Glyph(self[0], color, self[2], self.sprite_id)

    def highlighted(self, bg_color):
        """Returns a Glyph with the same value but new background color"""
        return Glyph(self[0], self[1], bg_color, self.sprite_id)

    def unhighlighted(self):
        """Returns a Glyph with the same value but no background"""
        return Glyph(self[0], self[1], None, self.sprite_id)

    def _modify_color(self, func):
        cube = _to_cube(self.color)
        red, green, blue = (max(min(x, 1), min(func(x), 5)) for x in cube)
        return self.recolored(_by_cube(red, green, blue))

    def darker(self, amount=1):
        """Returns a Glyph with the same chr but darker color"""
        return self._modify_color(lambda x: x - amount)

    def lighter(self, amount=1):
        """Returns a Glyph with the same chr but lighter color"""
        return self._modify_color(lambda x: x + amount)
