"""Functions for creating various maze maps"""

from __future__ import division

from pyre import dice, types, fov


# Utility Generators

def fence(grid):
    """Writes False to the borders of the grid"""
    for x in xrange(grid.cols):
        grid[x, 0] = False
        grid[x, grid.rows - 1] = False
    for y in xrange(grid.rows):
        grid[0, y] = False
        grid[grid.cols - 1, y] = False


def fill(grid, value):
    """Fills the entire Grid with the given value"""
    for x in xrange(grid.cols):
        for y in xrange(grid.rows):
            grid[x, y] = value


def chance_fill(grid, probability=.5):
    """Writes True to each tile with the given probability, False otherwise"""
    for x in xrange(grid.cols):
        for y in xrange(grid.rows):
            grid[x, y] = dice.chance(probability)


def perspective_fix(grid):
    """Rewrites wall tiles which should be fixed for tile perspective"""
    for x in xrange(grid.cols):
        for y in xrange(grid.rows - 1):
            if not grid[x, y] and not grid[x, y + 1]:
                grid[x, y] = False
        grid[x, grid.rows - 1] = False


# Abstract Maze Utility

def _expand(pos, visited):
    candidates = [pos + (1, 0), pos + (-1, 0), pos + (0, 1), pos + (0, -1)]
    return [pos for pos in candidates if pos in visited and not visited[pos]]


def _abstract_perfect(cols, rows):
    maze, visited = {}, {}
    for x in xrange(cols):
        for y in xrange(rows):
            pos = types.Coordinate(x, y)
            maze[pos] = set()
            visited[pos] = False
    start = dice.rand_coordinate(cols, rows)
    stack = [start]
    visited[start] = True

    while stack:
        curr = stack[-1]

        try:
            dig = dice.choice(_expand(curr, visited))
            stack.append(dig)
            visited[dig] = True
            maze[curr].add(dig)
        except IndexError:  # no unvisited adjacent for choice
            stack.pop()

    return maze


def _find_deadends(maze):
    return [node for node, edges in maze.iteritems() if not edges]


def _remove_deadends(maze, deadends):
    for end in deadends:
        candidates = [end + (1, 0), end + (-1, 0), end + (0, 1), end + (0, -1)]
        candidates = [pos for pos in candidates if pos in maze]
        candidates = [pos for pos in candidates if end not in maze[pos]]
        maze[end].add(dice.choice(candidates))


def _abstract_braid(cols, rows):
    maze = _abstract_perfect(cols, rows)
    deadends = _find_deadends(maze)
    _remove_deadends(maze, deadends)
    return maze


def _abstract_half_braid(cols, rows):
    maze = _abstract_perfect(cols, rows)
    deadends = _find_deadends(maze)
    deadends = [end for end in deadends if dice.coinflip()]
    _remove_deadends(maze, deadends)
    return maze


def _apply_maze(grid, algorithm):
    maze = algorithm((grid.cols - 1) // 2, (grid.rows - 1) // 2)
    fill(grid, False)

    for node, edges in maze.iteritems():
        pos = types.Coordinate(node[0] * 2 + 1, node[1] * 2 + 1)
        grid[pos] = True
        for edge in edges:
            grid[pos + node.direction_to(edge)] = True


# Mazes

def perfect_maze(grid):
    """Creates a perfect maze on the grid"""
    _apply_maze(grid, _abstract_perfect)


def braid_maze(grid):
    """Creates a braid maze on the grid"""
    _apply_maze(grid, _abstract_braid)


def half_braid_maze(grid):
    """Creates a half-braid maze on the grid"""
    _apply_maze(grid, _abstract_half_braid)


# Medieval Maze

def _create_room(pos, room_dim, room_chance):
    if dice.chance(room_chance):
        dim = dice.rand_coordinate(3, 3, room_dim[0] - 1, room_dim[1] - 1)
    else:
        dim = types.Coordinate(1, 1)
    off_max = room_dim - dim
    off = dice.rand_coordinate(1, 1, off_max[0], off_max[1])
    return pos + off, pos + off + dim


def _create_rooms(maze, room_dim, room_chance):
    rooms = {}
    for node in maze:
        pos = types.Coordinate(node[0] * room_dim[0], node[1] * room_dim[1])
        rooms[node] = _create_room(pos, room_dim, room_chance)
    return rooms


def _room_connect_source(room):
    try:
        return dice.rand_coordinate(room[0][0] + 1, room[0][1] + 1,
                                    room[1][0] - 1, room[1][1] - 1)
    except ValueError:  # room size less than 3x3
        return room[0]


def _connect_rooms(start, goal, grid):
    x1, y1 = _room_connect_source(start)
    x2, y2 = _room_connect_source(goal)
    step_x = 1 if x1 < x2 else -1
    step_y = 1 if y1 < y2 else -1

    while x1 != x2:
        grid[x1, y1] = True
        x1 += step_x
    while y1 != y2:
        grid[x1, y1] = True
        y1 += step_y


def medieval(grid, room_chance=.9):
    """Creates a traditional dungeon map with rooms and corridors"""
    maze_dim = min(grid.cols, grid.rows) // 7
    room_dim = grid.cols // maze_dim, grid.rows // maze_dim
    print (grid.cols, grid.rows), maze_dim, room_dim
    maze = _abstract_half_braid(maze_dim, maze_dim)
    rooms = _create_rooms(maze, room_dim, room_chance)

    fill(grid, False)
    for room in rooms.itervalues():
        _fill_room(grid, room, True)

    for node, edges in maze.iteritems():
        for edge in edges:
            _connect_rooms(rooms[node], rooms[edge], grid)


# Caverns

class _Automata(object):
    """Simulates a cellular automata using the 5-3 rule"""

    def __init__(self, cols, rows):
        self.cols, self.rows = cols, rows
        self.state = [[False for _ in xrange(cols)] for _ in xrange(rows)]
        self._scratch = [[False for _ in xrange(cols)] for _ in xrange(rows)]
        self.reset()

    def run(self, times=3):
        """Runs the simulation the given number of iterations"""
        for _ in xrange(times):
            self.apply_rule()

    def apply_grid(self, grid):
        """Applies the state to a grid"""
        for x in xrange(grid.cols):
            for y in xrange(grid.rows):
                grid[x, y] = self.state[y][x]

    def reset(self):
        """Resets the state to a random configuration"""
        for x in xrange(1, self.cols - 1):
            for y in xrange(1, self.rows - 1):
                self.state[y][x] = dice.chance(0.6)

    def wallcount(self, x, y, r):
        """Gets count of False around (x, y) within the given distance r"""
        count = 0
        for dx in xrange(max(0, x - r), min(x + r + 1, self.cols)):
            for dy in xrange(max(0, y - r), min(y + r + 1, self.rows)):
                if not self.state[dy][dx]:
                    count += 1
        return count

    def apply_rule(self):
        """Applies the 5-3 cellular automata rull to the grid"""
        for x in xrange(1, self.cols - 1):
            for y in xrange(1, self.rows - 1):
                close_count = self.wallcount(x, y, 1)
                far_count = self.wallcount(x, y, 2)
                self._scratch[y][x] = close_count < 5 and far_count > 3
        self.state, self._scratch = self._scratch, self.state

    def _get_flood_start(self):
        try:
            candidates = []
            for x in xrange(self.cols):
                for y in xrange(self.rows):
                    if self.state[y][x]:
                        candidates.append(types.Coordinate(x, y))
            return dice.choice(candidates)
        except IndexError:  # no open pos
            return None

    def try_connect(self):
        """Attempts to connect a level, return True if successful"""
        flood = set()
        start = self._get_flood_start()
        if not start:
            return False

        stack = [self._get_flood_start()]
        while stack:
            pos = stack.pop()
            if pos in flood or not self.state[pos[1]][pos[0]]:
                continue
            flood.add(pos)
            stack.append(pos + (1, 0))
            stack.append(pos + (-1, 0))
            stack.append(pos + (0, 1))
            stack.append(pos + (0, -1))
            stack.append(pos + (1, 1))
            stack.append(pos + (1, -1))
            stack.append(pos + (-1, 1))
            stack.append(pos + (-1, -1))

        # this is rare on big maps, and cheap on small maps
        if len(flood) / (self.cols * self.rows) < .45:
            return False

        for x in xrange(self.cols):
            for y in xrange(self.rows):
                pos = x, y
                if pos not in flood:
                    self.state[pos[1]][pos[0]] = False
        return True


def cavern(grid, connect=True):
    """Uses cellular automata to generate a cavern"""
    automata = _Automata(grid.cols, grid.rows)
    automata.run()

    while connect and not automata.try_connect():
        automata.reset()
        automata.run()

    automata.apply_grid(grid)


# Heightmap Based Overworld

def _raise_ellipses(hmap, cols, rows, iters=250):
    radi = cols // 8, rows // 8
    radi2 = radi[0] ** 2, radi[1] ** 2

    for _ in xrange(iters):
        center = dice.rand_coordinate(cols, rows)
        for dx in xrange(-radi[0], radi[0]):
            for dy in xrange(-radi[1], radi[1]):
                # wrap horizontally
                x, y = (center[0] + dx) % cols, center[1] + dy
                if y < 0 or y >= rows:
                    continue

                # find componentwise distance from center
                off_x, off_y = abs(center[0] - x), abs(center[1] - y)
                off_x = min(off_x, cols - off_x)

                # if inside ellipse, raise height
                if off_x ** 2 / radi2[0] + off_y ** 2 / radi2[1] < 1:
                    hmap[x][y] += 1


def _smooth_hmap(hmap, cols, rows):
    for x in xrange(1, cols - 1):
        for y in xrange(1, rows - 1):
            hmap[x][y] = (hmap[x][y - 1] + hmap[x][y] + hmap[x][y + 1]) / 3
            hmap[x][y] = (hmap[x - 1][y] + hmap[x][y] + hmap[x + 1][y]) / 3


def _equalize(hmap, cols, rows):
    heights = sorted(hmap[x][y] for x in xrange(cols) for y in xrange(rows))
    transfer = {}
    total = 0
    for height in heights:
        total += height
        transfer[height] = total
    for x in xrange(cols):
        for y in xrange(rows):
            hmap[x][y] = transfer[hmap[x][y]]


def _normalize(hmap, cols, rows):
    heights = [hmap[x][y] for x in xrange(cols) for y in xrange(rows)]
    low = min(heights)
    span = max(heights) - low
    for x in xrange(cols):
        for y in xrange(rows):
            hmap[x][y] = (hmap[x][y] - low) / span


def overworld(grid):
    """Generates a horizontally wrapped heightmap"""
    cols, rows = grid.cols, grid.rows
    hmap = [[0 for _ in xrange(rows)] for _ in xrange(cols)]

    _raise_ellipses(hmap, cols, rows)
    _smooth_hmap(hmap, cols, rows)
    _equalize(hmap, cols, rows)
    _normalize(hmap, cols, rows)

    for x in xrange(cols):
        for y in xrange(rows):
            grid[x, y] = hmap[x][y]


# Town Generators

def _fill_room(grid, room, value):
    for x in xrange(room[0][0], room[1][0]):
        for y in xrange(room[0][1], room[1][1]):
            grid[x, y] = value


def _outline_room(grid, room, value):
    for x in xrange(room[0][0], room[1][0]):
        grid[x, room[0][1]] = value
        grid[x, room[1][1] - 1] = value
    for y in xrange(room[0][1], room[1][1]):
        grid[room[0][0], y] = value
        grid[room[1][0] - 1, y] = value


def town(grid, fill_room=True, add_entrances=False):
    """Adds buildings to a grid"""
    town_dim = (min(grid.cols, grid.rows) - 2) // 7
    room_dim = (grid.cols - 2) // town_dim, (grid.rows - 2) // town_dim

    for x in xrange(town_dim):
        for y in xrange(town_dim):
            pos = types.Coordinate(x * room_dim[0] + 1, y * room_dim[1] + 1)
            room = _create_room(pos, room_dim, .75)
            if room[1][0] - room[0][0] <= 1:
                continue

            if fill_room:
                _fill_room(grid, room, False)
            else:
                _outline_room(grid, room, False)

            if add_entrances:
                if dice.coinflip():
                    doorx = room[0][0] if dice.coinflip() else room[1][0] - 1
                    doory = dice.randrange(room[0][1] + 1, room[1][1] - 1)
                else:
                    doorx = dice.randrange(room[0][0] + 1, room[1][0] - 1)
                    doory = room[0][1] if dice.coinflip() else room[1][1] - 1
                grid[doorx, doory] = True


# Chokepoint Detection

def _match_pattern(grid, pos, base):
    nearby = [[grid[pos + (dx, dy)] for dx in [-1, 0, 1]] for dy in [-1, 0, 1]]
    if nearby == [base[0:3], base[3:6], base[6:9]]:
        return True
    if nearby == [base[2::-1], base[5:2:-1], base[8:5:-1]]:
        return True
    if nearby == [base[::3], base[1::3], base[2::3]]:
        return True
    if nearby == [base[2::3], base[1::3], base[::3]]:
        return True


def _find_doors(grid):
    doors = set()
    for x in xrange(1, grid.cols - 1):
        for y in xrange(1, grid.rows - 1):
            pos = types.Coordinate(x, y)
            if _match_pattern(grid, pos, [0, 0, 1, 1, 1, 1, 0, 0, 1]):
                doors.add(pos)
    return doors


def add_doors(grid, open_chance=0, door_chance=1):
    """Writes True where a doors should go"""
    for door in _find_doors(grid):
        if dice.chance(door_chance):
            grid[door] = dice.chance(open_chance)


def find_ambushes(grid):
    """Returns a set of potential ambush points."""
    radius = max(grid.cols, grid.rows)
    exclude = set()
    for pos in _find_doors(grid):
        exclude.update(fov.simple_fov(grid, pos, radius))

    ambushes = set()
    for x in xrange(grid.cols):
        for y in xrange(grid.rows):
            if grid[x, y] and (x, y) not in exclude:
                ambushes.add(types.Coordinate(x, y))
    return ambushes
