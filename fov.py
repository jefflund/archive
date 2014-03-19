"""Provides functions for computing field of view"""

import math

from pyre import types


# Field of View

def _update_table(table, pos, trans):
    if pos in table:
        table[pos].update(trans)
    else:
        table[pos] = trans


def _complete_table(table):
    for x, y in table.keys():
        _update_table(table, (y, x), {(b, a) for a, b in table[x, y]})
    for x, y in table.keys():
        _update_table(table, (-x, y), {(-a, b) for a, b in table[x, y]})
        _update_table(table, (-x, -y), {(-a, -b) for a, b in table[x, y]})
        _update_table(table, (x, -y), {(a, -b) for a, b in table[x, y]})


def _compute_table(radius):
    table = {(0, 0): {(1, 0), (1, 1)}}

    curr_break = 0
    break_count = 0

    for x in xrange(1, radius):
        next_y = 0
        for y in xrange(x + 1):
            if y == curr_break:
                table[x, y] = {(x + 1, next_y), (x + 1, next_y + 1)}
                next_y += 2
            else:
                table[x, y] = {(x + 1, next_y)}
                next_y += 1
        break_count -= 1
        if break_count < 0:
            break_count = curr_break + 1
            curr_break += 1

    for y in xrange(radius + 1):
        table[radius, y] = set()

    _complete_table(table)
    return table


_TABLE_CACHE = {}


def simple_fov(grid, origin, radius):
    """Uses a pre-computed lookup table to compute field of view"""
    if radius not in _TABLE_CACHE:
        _TABLE_CACHE[radius] = _compute_table(radius)
    table = _TABLE_CACHE[radius]

    origin = types.Coordinate.from_tuple(origin)
    fov = {origin}
    stack = {(0, 0)}

    while stack:
        offset = stack.pop()
        pos = origin + offset
        fov.add(pos)
        if grid[pos]:
            stack.update(table[offset])

    return fov


# Field of View Post-Processing

def fix_wall(fov, origin, radius):
    """Removes some artifacts along walls at steep angles"""
    for x in xrange(origin[0], origin[0] + radius):
        if (x, origin[1]) not in fov:
            break
        else:
            fov.add((x, origin[1] + 1))
            fov.add((x, origin[1] - 1))
    for x in xrange(origin[0], origin[0] - radius, -1):
        if (x, origin[1]) not in fov:
            break
        else:
            fov.add((x, origin[1] + 1))
            fov.add((x, origin[1] - 1))
    for y in range(origin[1], origin[1] + radius):
        if (origin[0], y) not in fov:
            break
        else:
            fov.add((origin[0] + 1, y))
            fov.add((origin[0] - 1, y))
    for y in range(origin[1], origin[1] - radius, -1):
        if (origin[0], y) not in fov:
            break
        else:
            fov.add((origin[0] + 1, y))
            fov.add((origin[0] - 1, y))


def _outside_circle(origin, pos, radius):
    x = origin[0] - pos[0]
    y = origin[1] - pos[1]
    return x ** 2 + y ** 2 > radius ** 2 + 1


def fix_circular(fov, origin, radius):
    """Filters a field of view to be circular"""
    outside = {pos for pos in fov if _outside_circle(origin, pos, radius)}
    fov.difference_update(outside)


_CONE_ANGLE = math.sqrt(2) / 2

def _inside_cone(origin, pos, direction, angle=_CONE_ANGLE):
    diff = pos - origin + direction
    if diff == (0, 0):
        return False
    return direction.dot(diff) / abs(diff) / abs(direction) > angle


def fix_directional(fov, origin, direction):
    inside = {pos for pos in fov if _inside_cone(origin, pos, direction)}
    fov.intersection_update(inside)


def default_fov(grid, origin, radius):
    """Computes a circular field of view with artifiact removal"""
    fov = simple_fov(grid, origin, radius)
    fix_wall(fov, origin, radius)
    fix_circular(fov, origin, radius)
    return fov


# Line of Sight

def _compute_reverse_table(radius):
    forward_table = _TABLE_CACHE[radius]
    reverse_table = {}
    for pos, edges in forward_table.iteritems():
        for edge in edges:
            reverse_table[edge] = pos
    return reverse_table


def _construct_trace(origin, table, offset, path):
    if offset != (0, 0):
        _construct_trace(origin, table, table[offset], path)
        path.append(origin + offset)
    return path


_REVERSE_TABLE_CACHE = {}


def trace(grid, origin, target):
    """Uses a pre-computed table to cast a ray from origin to target"""
    radius = origin.chebyshev_distance(target)
    if radius == 0:
        return []

    if radius not in _TABLE_CACHE:
        _TABLE_CACHE[radius] = _compute_table(radius)
    if radius not in _REVERSE_TABLE_CACHE:
        _REVERSE_TABLE_CACHE[radius] = _compute_reverse_table(radius)
    table = _REVERSE_TABLE_CACHE[radius]

    origin = types.Coordinate.from_tuple(origin)
    last = target - origin
    curr = table[last]

    while curr != (0, 0):
        if not grid[origin + curr]:
            last = curr
        curr = table[curr]

    return _construct_trace(origin, table, last, [])


def los(grid, origin, target):
    """Uses a pre-computed table to compute line of sight"""
    return trace(grid, origin, target)[-1] == target


def flood_fov(grid, origin):
    """Uses a flood fill algorithm to compute field of view"""
    fov = set()
    stack = [origin]

    while stack:
        pos = stack.pop()
        if pos in fov or not grid[pos]:
            continue
        fov.add(pos)
        stack.append(pos + (1, 0))
        stack.append(pos + (-1, 0))
        stack.append(pos + (0, 1))
        stack.append(pos + (0, -1))

    return fov
