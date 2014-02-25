"""Provides mixins used to enhance Grid object functionality"""

from __future__ import division

from pyre import dice, types


_WALL = types.Glyph('#')
_FLOOR = types.Glyph('.')
_CLOSE_DOOR = types.Glyph('+', types.Color.Brown)
_OPEN_DOOR = types.Glyph('`', types.Color.Brown)


class Grid(object):
    """A 2-dimensional map of tiles"""

    def __init__(self, cols, rows):
        """Constructs a new World with the given dimensions"""
        self.cols, self.rows = cols, rows

        self._tiles = [[_FLOOR for _ in xrange(cols)] for _ in xrange(rows)]
        self._passable = [[True for _ in xrange(cols)] for _ in xrange(rows)]
        self._pellucid = [[True for _ in xrange(cols)] for _ in xrange(rows)]

    def set_tile(self, pos, tile, passable, translucent=None):
        """Sets the tile at the given Coordinate"""
        if translucent is None:
            translucent = passable
        self._tiles[pos[1]][pos[0]] = tile
        self._passable[pos[1]][pos[0]] = passable
        self._pellucid[pos[1]][pos[0]] = translucent

    def tile_at(self, pos):
        """Returns the tile at the given Coordinate"""
        return self._tiles[pos[1]][pos[0]]

    def passable_at(self, pos):
        """Returns true if the tile at the given Coordinate is passable"""
        return self._passable[pos[1]][pos[0]]

    def translucent_at(self, pos):
        """Returns true if the tile at the given Coordinate is translucent"""
        return self._pellucid[pos[1]][pos[0]]

    def get_random_pos(self, select, tries=100):
        """Returns a random Coordinate for which select(pos) is True"""
        for _ in xrange(tries):
            pos = dice.rand_coordinate(self.cols, self.rows)
            if select(pos):
                return pos

        candidates = []
        for x in xrange(self.cols):
            for y in xrange(self.rows):
                pos = types.Coordinate(x, y)
                if select(pos):
                    candidates.append(pos)
        return dice.choice(candidates)

    def get_passable_pos(self):
        """Returns a random passable Coordinate"""
        return self.get_random_pos(self.passable_at)


class GridView(object):
    """Provides a wrapper around a Grid to provide indexed read and write"""

    def __init__(self, cols, rows, read, write):
        self.cols, self.rows = cols, rows
        self.write = write
        self.read = read

    @classmethod
    def basic(cls, grid, wall=_WALL, floor=_FLOOR):
        """Creates a basic boolean GridView around the given Grid"""
        _write = lambda p, v: grid.set_tile(p, floor if v else wall, v)
        return cls(grid.cols, grid.rows, grid.passable_at, _write)

    @classmethod
    def view(cls, grid):
        """Creates a boolean GridView to read the Grid translucency"""
        return cls(grid.cols, grid.rows, grid.translucent_at, None)

    @classmethod
    def choice(cls, grid, walls, floors):
        """Creates a boolean GridView with a selection of tiles"""
        def _write(pos, passable):
            tile = dice.choice(floors if passable else walls)
            grid.set_tile(pos, tile, passable)
        return cls(grid.cols, grid.rows, grid.passable_at, _write)

    @classmethod
    def heightmap(cls, grid, terrain):
        """Creates a basic float GridView around the given Grid"""
        def _write(pos, height):
            height_index = min(len(terrain) - 1, int(height * len(terrain)))
            tile, passable = terrain[height_index]
            grid.set_tile(pos, tile, passable, True)
        return cls(grid.cols, grid.rows, None, _write)

    @classmethod
    def door(cls, grid):
        return cls(grid.cols, grid.rows, grid.passable_at, grid.add_door)

    def __setitem__(self, pos, val):
        self.write(pos, val)

    def __getitem__(self, pos):
        return self.read(pos)


class Actor(object):
    """Represents entities which perform actions"""

    def __init__(self, face):
        self.face = face
        self.grid = None
        self.pos = None
        self.expired = False

    def act(self):
        """Performs the action of the Actor"""

    def set_pos(self, pos):
        """Updates the position of the Actor on its Grid"""
        assert self.grid

        self.grid.set_actor_pos(self, pos)

    def move(self, delta):
        """Translates the position of the Actor on its Grid"""
        self.set_pos(self.pos + delta)


class ActorMixin(object):
    """Adds Actor functionality to a Grid"""

    def __init__(self, cols, rows):
        self._occupants = [[None for _ in xrange(cols)] for _ in xrange(rows)]
        self._register = set()
        self._scheduler = DeltaClock()

    def tick(self):
        """Advances the clock, calling Actor.act where needed"""
        events = self._scheduler.advance()
        deltas = {}
        for actor in events:
            delta = actor.act()
            deltas[actor] = delta if delta is not None else 1

        expired = {actor for actor in self._register if actor.expired}
        for actor in expired:
            self.remove_actor(actor)

        for actor, delta in deltas.iteritems():
            if not actor.expired and actor in self._register:
                self._scheduler.schedule(actor, delta)

    def add_actor(self, actor, pos=None, delta=0):
        """Adds an Actor to the Grid"""
        assert actor.grid is None

        if pos is None:
            pos = self.get_open_pos()

        assert self._occupants[pos[1]][pos[0]] is None

        actor.grid = self
        actor.pos = types.Coordinate.from_tuple(pos)
        self._occupants[pos[1]][pos[0]] = actor
        self._register.add(actor)
        self._scheduler.schedule(actor, delta)

    def set_actor_pos(self, actor, pos):
        """Moves the Actor to a new Coordinate position"""
        assert actor in self._register
        assert self._occupants[pos[1]][pos[0]] is None

        self._occupants[actor.pos[1]][actor.pos[0]] = None
        actor.pos = types.Coordinate.from_tuple(pos)
        self._occupants[actor.pos[1]][actor.pos[0]] = actor

    def remove_actor(self, actor):
        """Remove an Actor from the Grid"""
        assert actor in self._register

        self._scheduler.unschedule(actor)
        self._register.remove(actor)
        self._occupants[actor.pos[1]][actor.pos[0]] = None
        actor.pos = None
        actor.grid = None

    def actor_at(self, pos):
        """Gets the Actor at the given Coordinate, or None"""
        return self._occupants[pos[1]][pos[0]]

    def open_at(self, pos):
        """Returns True if the tile is passable and has no Actor"""
        return self.passable_at(pos) and self.actor_at(pos) is None

    def get_open_pos(self):
        """Gets a random open Coordinate"""
        return self.get_random_pos(self.open_at)


class DeltaClock(object):
    """A mechanism to store and retrieve pending timed events"""

    class Node(object):
        """A node in the DeltaClock data structure"""

        def __init__(self, delta, link):
            self.delta = delta
            self.link = link
            self.events = set()

    def __init__(self):
        self.head = None
        self.event_nodes = {}

    def schedule(self, event, delta=1):
        """Schedules an event at the given delta"""
        assert event not in self.event_nodes

        prev, curr = None, self.head
        while curr and delta > curr.delta:
            delta -= int(curr.delta)
            prev, curr = curr, curr.link

        if curr and delta == curr.delta:
            node = curr
        else:
            node = DeltaClock.Node(delta, curr)
            if not prev:
                self.head = node
            else:
                prev.link = node

            if curr:
                curr.delta -= int(delta)

        node.events.add(event)
        self.event_nodes[event] = node

    def unschedule(self, event):
        """Removes an event from the DeltaClock"""
        if event in self.event_nodes:
            self.event_nodes[event].events.remove(event)
            del self.event_nodes[event]

    def advance(self):
        """Pops the next set of events in the DeltaClock"""
        assert self.head

        events = self.head.events
        for event in events:
            del self.event_nodes[event]
        self.head = self.head.link
        return events


class ItemMixin(object):
    """Adds Item functionality to a Grid"""

    def __init__(self, cols, rows):
        self._items = [[None for _ in xrange(cols)] for _ in xrange(rows)]

    def place_item(self, item, pos=None):
        """Places an Item on the Grid"""
        if pos is None:
            pos = self.get_empty_pos()

        assert self._items[pos[1]][pos[0]] is None

        self._items[pos[1]][pos[0]] = item

    def remove_item(self, pos):
        """Removes and returns the Item at the given Coordinate position"""
        item = self._items[pos[1]][pos[0]]
        self._items[pos[1]][pos[0]] = None
        return item

    def item_at(self, pos):
        """Gets the Item at the given Coordinate, or None"""
        return self._items[pos[1]][pos[0]]

    def empty_at(self, pos):
        """Returns True if the tile is passable and has no Item"""
        return self.passable_at(pos) and self.item_at(pos) is None

    def get_empty_pos(self):
        """Gets a random empty Coordinate"""
        return self.get_random_pos(self.empty_at)


class DescribeMixin(object):
    """Adds descriptor functionality to a Grid"""

    def __init__(self, cols, rows, *descriptors):
        self._descriptions = [['' for _ in xrange(cols)] for _ in xrange(rows)]
        self._descriptors = list(descriptors)

    def add_descriptor(self, descriptor):
        """Adds a descriptor to the stack"""
        self._descriptors.append(descriptor)

    def set_description(self, pos, description):
        """Sets the default description for a given position"""
        self._descriptions[pos[1]][pos[0]] = description

    def get_description(self, pos):
        """Gets the description for a given position"""
        for descriptor in self._descriptors:
            description = descriptor(pos)
            if description:
                return str(description)
        return self._descriptions[pos[1]][pos[0]]


class DoorMixin(object):
    """Adds door functionality to a Grid"""

    def __init__(self, closed_door=_CLOSE_DOOR, open_door=_OPEN_DOOR, ):
        self._doors = {}
        self._closed_door = closed_door
        self._open_door = open_door

    def _set_door_tile(self, pos, door_open):
        if door_open:
            self.set_tile(pos, self._open_door, True)
        else:
            self.set_tile(pos, self._closed_door, False)

    def add_door(self, pos, door_open=False):
        self._doors[pos] = door_open
        self._set_door_tile(pos, door_open)

    def door_open_at(self, pos):
        return pos in self._doors and self._doors[pos]

    def door_closed_at(self, pos):
        return pos in self._doors and not self._doors[pos]

    def open_door(self, pos):
        assert self.door_closed_at(pos)

        self._doors[pos] = True
        self._set_door_tile(pos, True)

    def close_door(self, pos):
        assert self.door_open_at(pos)

        self._doors[pos] = False
        self._set_door_tile(pos, False)


class LevelMixin(ActorMixin, ItemMixin, DescribeMixin):
    """Adds level functionality to a Grid"""

    def __init__(self, cols, rows):
        ActorMixin.__init__(self, cols, rows)
        ItemMixin.__init__(self, cols, rows)
        DescribeMixin.__init__(self, cols, rows, self.actor_at, self.item_at)
