"""Provides helper functions for manipulating user interfaces"""

import time
import string

from pyre import data, types, fov


def dim_screen(term):
    """Dims the current screen"""
    for x in xrange(term.cols):
        for y in xrange(term.rows):
            pos = x, y
            term.buffer_char(pos, term.char_at(pos).darker(2))


def _popup_text_draw(term, text, just, dim, border):
    text = text.split('\n')

    if dim:
        dim_screen(term)

    row_max = max(len(row) for row in text)
    if just:
        text = [just(line, row_max) for line in text]

    x_min = (term.cols - row_max) // 2
    x_max = x_min + row_max
    y_min = (term.rows - len(text)) // 2
    y_max = y_min + len(text)

    if border:
        for y in xrange(y_min - 1, y_max + 1):
            term.buffer_char((x_min - 1, y), types.Glyph('|'))
            term.buffer_char((x_max, y), types.Glyph('|'))
        for x in xrange(x_min - 1, x_max + 1):
            term.buffer_char((x, y_min - 1), types.Glyph('-'))
            term.buffer_char((x, y_max), types.Glyph('-'))

    for y, row in enumerate(text, (term.rows - len(text)) // 2):
        for x, char in enumerate(row, (term.cols - len(row)) // 2):
            term.buffer_char((x, y), types.Glyph(char))

    return (x_min, y_min), (x_max, y_max)


def popup_text(term, text, just=None, dim=False, border=None):
    """Displays text centered on the Terminal screen until a key is pressed"""
    term.save()
    _popup_text_draw(term, text, just, dim, border)
    term.refresh()
    term.get_key()
    term.recall()


def popup_prompt(term, text, just=string.ljust, dim=False, border=None):
    """Displays a prompt on screen, and returns the user response"""
    term.save()
    bounds = _popup_text_draw(term, text + '\n>', just, dim, border)
    term.refresh()

    response = []
    key = term.get_key()
    x, y = bounds[0][0] + 1, bounds[1][1] - 1

    while key not in '\n\r\x1b':
        if key == '\b' or key == '\x7f':
            term.buffer_char((x, y), types.Glyph(' '))
            if response:
                x -= 1
                response.pop()
        else:
            x += 1
            term.buffer_char((x, y), types.Glyph(key))
            response.append(key)
        term.refresh()
        key = term.get_key()

    term.recall()
    term.refresh()
    term.refresh()

    if key == '\x1b':
        return None
    else:
        return ''.join(response)


def flash_text(term, text, y=0):
    """Quickly displays a temporary message on the screen"""
    for x, char in enumerate(text.ljust(term.cols)):
        term.buffer_char((x, y), types.Glyph(char))
    term.refresh()


def text_reader(term, title, text):
    """Display long text in a scrollable format"""
    term.save()

    lines = [line.strip() for line in text.strip().split('\n')]
    top = 0
    key = '\x00'

    while key != 'q':
        curr_lines = [title] + lines[top: top + term.rows - 1]
        term.clear()
        for y, line in enumerate(curr_lines):
            for x, char in enumerate(line):
                try:
                    term.buffer_char((x, y), types.Glyph(char))
                except IndexError:
                    pass
        term.refresh()
        key = term.get_key()
        delta = types.Direction.key_to_dir(key)
        if delta and delta[0] == 0:
            top = max(0, min(top + delta[1], len(lines)))

    term.recall()
    term.refresh()


def dropdown_select(term, title, items, dim=False):
    """Displays a list of items, and allows user to select one"""
    term.save()

    if dim:
        dim_screen(term)

    items = list(items)
    rows = [title]
    rows += ['{}) {}'.format(chr(n), i) for n, i in enumerate(items, ord('a'))]
    max_len = max(len(row) for row in rows)

    for y, row in enumerate(rows):
        for x in xrange(max_len):
            if x < len(row):
                term.buffer_char((x, y), types.Glyph(row[x]))
            else:
                term.buffer_char((x, y), types.Glyph(' '))
    term.refresh()

    try:
        key = term.get_key()
        return items[ord(key) - ord('a')]
    except IndexError:
        return None
    finally:
        term.recall()
        term.refresh()


def aim(camera, reticle, trace=None, default=None, accept='t\n\r'):
    """Allows the user to select a target within the Camera fov"""
    camera.viewport.term.save()

    pos = types.Coordinate.from_tuple(camera.pos)
    if default:
        targets = {x for x in camera.fov if default(x) and x != camera.pos}
        if targets:
            pos = min(targets, key=camera.pos.chebyshev_distance)

    key = '\x00'
    while key not in accept and key != '\x1b':
        camera.viewport.term.recall()
        if trace:
            for trace_pos in fov.trace(camera.view, camera.pos, pos):
                camera.draw_relative(trace_pos, trace)
        camera.draw_relative(pos, reticle)
        camera.viewport.term.refresh()

        key = camera.viewport.term.get_key()
        delta = types.Direction.key_to_dir(key)
        if delta:
            update = pos + delta
            if update in camera.fov:
                pos = update

    camera.viewport.term.recall()
    camera.viewport.term.refresh()
    return pos if key in accept else None


def animate_trace(camera, trace, tile, beam=False):
    """Animates a trace for the camera"""
    camera.viewport.term.save()

    for pos in trace:
        if not beam:
            camera.viewport.term.recall()
        if pos in camera.fov:
            camera.draw_relative(pos, tile)
        camera.viewport.term.refresh()
        time.sleep(.01)

    camera.viewport.term.recall()
    camera.viewport.term.refresh()


def animate_fov(camera, center, field, tile):
    """Animates a fov radiating from the center"""
    camera.viewport.term.save()
    center = types.Coordinate.from_tuple(center)

    dist_map = {}
    for pos in field.difference({center}):
        if pos in camera.fov:
            dist = center.euclidean_distance(pos)
            if dist not in dist_map:
                dist_map[dist] = set()
            dist_map[dist].add(pos)

    for dist in sorted(dist_map):
        for pos in dist_map[dist]:
            camera.draw_relative(pos, tile)
        camera.viewport.term.refresh()
        time.sleep(.01)

    camera.viewport.term.recall()
    camera.viewport.term.refresh()


def default_examine(camera, pos):
    """Uses popup_text to display the description at the given position"""
    description = camera.grid.get_description(pos)
    _popup_text_draw(camera.viewport.term, description, None, False, False)


def look(camera, reticle, examine=default_examine):
    """Allows a camera to examine its surroundings"""
    camera.viewport.term.save()

    pos = types.Coordinate.from_tuple(camera.pos)
    key = '\x00'
    while key != '\x1b':
        camera.viewport.term.recall()
        camera.draw_relative(pos, reticle)
        examine(camera, pos)
        camera.viewport.term.refresh()

        key = camera.viewport.term.get_key()
        delta = types.Direction.key_to_dir(key)
        if delta:
            update = pos + delta
            if update in camera.fov:
                pos = update

    camera.viewport.term.recall()
    camera.viewport.term.refresh()


class RewriteField(data.Field):
    """Field containing a list of rewrite rules for drawing backgrounds"""

    _tile_parser = data.TileField()

    def __init__(self, default=None):
        data.Field.__init__(self, default if default else {})

    def __call__(self, raw_data):
        rewrites = {}
        for line in raw_data.split('\n'):
            key, value = line.split('=')
            rewrites[key] = self._tile_parser(value)
        return rewrites


class MenuField(data.Field):
    """Field containing a list of menu items"""

    def __init__(self, default=None):
        data.Field.__init__(self, default if default else [])

    def __call__(self, raw_data):
        items = []
        for line in raw_data.split('\n'):
            x, y, item = line.split(',', 2)
            pos = types.Coordinate(int(x), int(y))
            items.append((pos, item))
        return items


class ScreenInfoDB(data.InfoDB):
    """Stores info for drawing data defined background screens"""

    screen = data.StrField()
    rewrites = RewriteField()
    menu = MenuField()

    @data.entryattr
    def buffer_screen(self, term):
        """Buffers the named background screen onto the given Terminal"""
        term.clear()
        lines = self.screen.split('\n') # pylint: disable=E1101
        for y, line in enumerate(lines):
            for x, char in enumerate(line):
                if char in self.rewrites:
                    char = self.rewrites[char]
                else:
                    char = types.Glyph(char)
                term.buffer_char((x, y), char)

    @data.entryattr
    def run_menu(self, term):
        """Runs the menu on the given Terminal"""
        term.save()

        curr = 0
        key = '\x00'

        while key != '\n' and key != '\r':
            self.buffer_screen(term)
            for i, item in enumerate(self.menu):
                color = types.Color.White if i == curr else types.Color.Gray
                pos, text = item
                for x, char in enumerate(text, pos[0]):
                    char = types.Glyph(char, color)
                    term.buffer_char((x, pos[1]), char)
            term.refresh()
            key = term.get_key()
            delta = types.Direction.key_to_dir(key)
            if delta and delta.is_orthognal():
                curr = (curr + sum(delta)) % len(self.menu)

        term.recall()
        term.refresh()
        return self.menu[curr][1]
