"""Provides abstractions for terminal IO"""

PYGAME_ENABLED = False
try:
    import pygame
    try:
        pygame.display.init()  # will fail in non-graphical environment
        PYGAME_ENABLED = True
    except pygame.error:
        pass
except ImportError:
    pass

import os
import sys

from pyre import types, data


_BLANK = types.Glyph(' ')


class Terminal(object):
    """Abstract base class for providing terminal IO"""

    def __init__(self, cols, rows):
        """Creates a Terminal with the given dimensions"""
        self.cols, self.rows = cols, rows
        self._buffer = [[_BLANK for _ in xrange(cols)] for _ in xrange(rows)]
        self._saved = [[_BLANK for _ in xrange(cols)] for _ in xrange(rows)]
        self._widgets = set()

    def close(self):
        """Reverts the terminal to its original state"""
        raise NotImplementedError()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self.close()

    def __del__(self):
        self.close()

    def buffer_char(self, pos, char):
        """Buffers to the given Glyph at the specified Coordinate"""
        self._buffer[pos[1]][pos[0]] = char

    def char_at(self, pos):
        """Gets the Glyph in the buffer at the specified Coordinate"""
        return self._buffer[pos[1]][pos[0]]

    def clear(self):
        """Clears the entire buffer"""
        for x in xrange(self.cols):
            for y in xrange(self.rows):
                self._buffer[y][x] = _BLANK

    def save(self):
        """Saves the buffer for later recall"""
        for x in xrange(self.cols):
            for y in xrange(self.rows):
                self._saved[y][x] = self._buffer[y][x]

    def recall(self):
        """Recalls the buffer to the last saved point"""
        for x in xrange(self.cols):
            for y in xrange(self.rows):
                self._buffer[y][x] = self._saved[y][x]

    def update(self):
        """Calls update on all Terminal Widgets"""
        for widget in self._widgets:
            widget.update()

    def refresh(self):
        """Updates the screen to reflect the current state of the buffer"""
        raise NotImplementedError()

    def get_key(self):
        """Returns the next keypress, blocking until there is one"""
        raise NotImplementedError()

    def add_widget(self, widget):
        """Adds an updating Widget to the Terminal"""
        self._widgets.add(widget)

    def remove_widgets(self):
        """Removes each Widget from the Terminal"""
        self._widgets.clear()


class AnsiTerminal(Terminal):
    """Uses Ansi escape sequences to provide terminal IO"""

    def __init__(self, cols, rows, title=None):
        Terminal.__init__(self, cols, rows)

        self._settings = os.popen('stty -g').read()
        os.system('stty -echo -icanon')
        os.system('tput civis')
        os.system('clear')
        if title:
            sys.stdout.write('\x1b]2;{}\x07'.format(title))

    def close(self):
        if self._settings:
            os.system('stty {}'.format(self._settings))
            os.system('tput cnorm')
            sys.stdout.write('\n')
            os.system('clear')
            self._settings = None

    def refresh(self):
        output = [''.join(char.ansi for char in row) for row in self._buffer]
        sys.stdout.write('\x1b[H')
        sys.stdout.write('\n'.join(output))

    def get_key(self):
        return sys.stdin.read(1)


class SpriteIDSection(data.Section):
    """Section containing specifications for sprites"""

    def __call__(self, key, value):
        name, x, y = value.split(',')
        return int(key), (name, int(x), int(y))


class SpriteDB(data.InfoDB):
    """Stores info about sprite sheets"""

    filename = data.StrField()
    tilesize = data.IntTupleField('x')

    ids = SpriteIDSection()

    def __init__(self, filename):
        data.InfoDB.__init__(self, filename)
        self.base_path = os.path.dirname(filename)

    def build_sprite_cache(self, rescale):
        """Builds a cache of sprite surfaces from using the SpriteDB data"""
        sheets = {}
        for name, entry in self.data.iteritems():
            if isinstance(entry, SpriteDB.Entry):  # pylint: disable=E1101
                filename = os.path.join(self.base_path, entry.filename)
                sheet = pygame.image.load(filename).convert_alpha()
                sheets[name] = sheet, entry.tilesize

        sprites = {}
        for sprite_id in self['ids']:
            sheet_name, col, row = self['ids'][sprite_id]
            sheet, tilesize = sheets[sheet_name]
            sprite_rect = ((col * tilesize[0], row * tilesize[1]), tilesize)
            sprite = sheet.subsurface(sprite_rect)
            sprites[sprite_id] = pygame.transform.scale(sprite, rescale)

        return sprites


class PyGameTerminal(Terminal):
    """Uses PyGame to provide terminal IO"""

    def __init__(self, cols, rows, title='', font=('freemono', 15)):
        Terminal.__init__(self, cols, rows)

        pygame.font.init()
        pygame.display.init()
        pygame.display.set_caption(title)

        pygame.key.set_repeat(130, 60)

        try:
            self.font = pygame.font.Font(*font)
        except IOError:
            self.font = pygame.font.SysFont(*font)
        self.tilesize = self.font.size('@')

        screen_size = self.tilesize[0] * cols, self.tilesize[1] * rows
        self.display = pygame.display.set_mode(screen_size)

        self.render_cache = {}
        self.sprite_cache = {}

    def close(self):
        pygame.quit()
        self.display = None
        self.font = None

    def refresh(self):
        self.display.fill((0, 0, 0, 255))
        for y in xrange(self.rows):
            for x in xrange(self.cols):
                pos = (x * self.tilesize[0], y * self.tilesize[1])
                self.display.blit(self._render(self._buffer[y][x]), pos)
        pygame.display.flip()

    def _render(self, char):
        if char.sprite_id in self.sprite_cache:
            return self._render_sprite(char)
        else:
            return self._render_char(char)

    def _render_char(self, char):
        try:
            return self.render_cache[char.char, char.rgb, char.rgb_bg]
        except KeyError:
            render = self.font.render(char.char, True, char.rgb, char.rgb_bg)
            self.render_cache[char] = render
            return render

    def _render_sprite(self, char):
        try:
            return self.render_cache[char.sprite_id, char.rgb, char.rgb_bg]
        except KeyError:
            sprite = self.sprite_cache[char.sprite_id].copy()
            array = pygame.PixelArray(sprite)  # pylint: disable=E1121
            # Note that these replaces are very Oryx specific
            array.replace((33, 33, 33), char.rgb_bg)
            array.replace((196, 196, 196), char.rgb)
            render = array.make_surface()
            self.render_cache[char.sprite_id, char.rgb] = render
            return render

    def get_key(self):
        event = pygame.event.wait()
        while event.type != pygame.KEYDOWN or len(event.unicode) != 1:
            if event.type == pygame.QUIT:
                raise SystemExit()
            event = pygame.event.wait()
        return event.unicode
