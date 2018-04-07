"""Provides various automatically updating Widgets"""

from pyre import fov, types, model


class Widget(object):
    """Base class for all Widgets"""

    def __init__(self, term, pos, dim):
        term.add_widget(self)
        self.term = term
        self.pos = types.Coordinate.from_tuple(pos)
        self.dim = types.Coordinate.from_tuple(dim)

    def draw(self, relpos, char):
        """Buffers a character relative to the Widget position"""
        assert self.inbounds(relpos)
        self.term.buffer_char(self.pos + relpos, char)

    def update(self):
        """Updates the Widget to reflect the lastest data"""

    def inbounds(self, relpos):
        """Returns True if the relative position is within the bounds"""
        return 0 <= relpos[0] < self.dim[0] and 0 <= relpos[1] < self.dim[1]


class TextWidget(Widget):
    """Displays generated text in a box"""

    def __init__(self, term, pos, dim, text_func):
        Widget.__init__(self, term, pos, dim)
        self.text_func = text_func

    def update(self):
        text = self.text_func()
        x, y = 0, 0
        for char in text:
            if char == '\n':
                x, y = 0, y + 1
            else:
                self.draw(types.Coordinate(x, y), types.Glyph(char))
                x += 1


class CameraWidget(Widget):
    """Displays the field of view for a Camera"""

    def __init__(self, term, pos, dim, camera):
        assert camera.viewport is None

        Widget.__init__(self, term, pos, dim)
        self.camera = camera
        self.center = types.Coordinate((dim[0] - 1) // 2, (dim[1] - 1) // 2)
        self.camera.view_dist = min(self.center)
        self.camera.viewport = self

        self.mem_local = set()
        self.mem_grid = None

    def update(self):
        offset = self.center - self.camera.pos

        # draw memory
        if self.mem_grid is self.camera.grid:
            forgotten = set()
            for pos in self.mem_local:
                relpos = pos + offset
                if self.inbounds(relpos):
                    tile = self.camera.grid.tile_at(pos)
                    self.draw(relpos, tile.recolored(types.Color.Smoke))
                else:
                    forgotten.add(pos)
            self.mem_local -= forgotten
        else:
            self.mem_grid = self.camera.grid
            self.mem_local.clear()

        # draw field of view
        for pos in self.camera.fov:
            self.draw(pos + offset, self.camera.grid.look_at(pos))
            self.mem_local.add(pos)


class CameraMixin(object):
    """Allows an Actor to be easily turned into a Camera"""

    def __init__(self, fov_func=fov.default_fov):
        self.fov = {}
        self.view_dist = 0
        self.viewport = None
        self.fov_func = fov_func

    @property
    def view(self):
        """Gets a view wrapper for the Camera grid"""
        return model.GridView.view(self.grid)

    def compute_fov(self):
        """Updates the Camera fov"""
        self.fov = self.fov_func(self.view, self.pos, self.view_dist)

    def draw_relative(self, rel_pos, char):
        """Draws a Glyph relative to Camera on screen"""
        self.viewport.draw(self.viewport.center - self.pos + rel_pos, char)


class LoggerWidget(Widget):
    """Displays formated log messages"""

    def __init__(self, term, pos, dim, logger):
        Widget.__init__(self, term, pos, dim)
        self.logger = logger
        logger.resize(dim[1])

    def update(self):
        for y, message in enumerate(self.logger.retrieve()):
            color = types.Color.Gray if message.seen else types.Color.White
            for x, char in enumerate(str(message)):
                self.draw(types.Coordinate(x, y),
                          types.Glyph(char, color))
