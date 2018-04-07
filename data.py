"""Provides functions for loading and using data from file"""

import ConfigParser
import collections

from pyre import dice, types


class Field(object):
    """Base class for InfoEntry fields"""

    def __init__(self, default=None):
        self.default = default

    def __call__(self, data):
        raise NotImplementedError()


class Section(object):
    """Base class for InfoDB custom section"""

    def __call__(self, key, value):
        raise NotImplementedError()


class IntField(Field):
    """Field containing an integer"""

    def __call__(self, data):
        return int(data)


class FloatField(Field):
    """Field containing a float"""

    def __call__(self, data):
        return float(data)


class BoolField(Field):
    """Field containing a bool"""

    def __init__(self, default=None, true=None, false=None):
        Field.__init__(self, default)
        self.mapping = {}
        for key in true if true else ['true', 't', 'yes', 'y', '1']:
            self.mapping[key] = True
        for key in false if false else ['false', 'f', 'no', 'n', '0']:
            self.mapping[key] = False

    def __call__(self, data):
        return self.mapping[data.lower()]


class IntSection(Section):
    """Section containing integer values"""

    def __call__(self, key, value):
        return key, int(value)


def _parse_tile(data):
    data = tuple(x for x in data.split(','))
    char, color = data[:2]
    color = types.Color.try_by_name(color)
    if len(data) == 3:
        return types.Glyph(char, color, sprite_id=int(data[2]))
    else:
        return types.Glyph(char, color)


class TileField(Field):
    """Field containing a Glyph"""

    def __call__(self, data):
        return _parse_tile(data)


class TileSection(Section):
    """Section containing Glyph values"""

    def __call__(self, key, value):
        return key, _parse_tile(value)


class IntTupleField(Field):
    """Field containing an integer 2-tuple"""

    def __init__(self, sep=',', default=None):
        Field.__init__(self, default)
        self.sep = sep

    def __call__(self, data):
        x, y = (int(x) for x in data.split(self.sep))
        return x, y


class RollField(IntTupleField):
    """Field containing parameters for an xdy roll"""

    def __init__(self, default=None):
        IntTupleField.__init__(self, 'd', default)


class MeleeField(Field):
    """Field containing a bonus and parameters for an xdy roll"""

    def __init__(self, default=(0, (0, 0))):
        Field.__init__(self, default)

    def __call__(self, data):
        bonus, roll = data.split(',')
        x, y = (int(x) for x in roll.split('d'))
        return int(bonus), (x, y)


class StrField(Field):
    """Field containing a string value"""

    def __call__(self, data):
        return data


class ListField(Field):
    """Field containing a list of string values"""

    def __init__(self, sep=',', default=None):
        Field.__init__(self, default)
        self.sep = sep

    def __call__(self, data):
        return data.split(self.sep) if data else []


class FlagsField(Field):
    """Field containing a list of flags"""

    def __init__(self, flag_sep=';', value_sep=':', default=None, **mapping):
        Field.__init__(self, default)
        self.flag_sep = flag_sep
        self.value_sep = value_sep
        self.mapping = mapping

    def _flag(self, data):
        if self.value_sep in data:
            key, value = (x.strip() for x in data.split(self.value_sep, 1))
            if key in self.mapping:
                value = self.mapping[key](value)
            return key, value
        else:
            return data.strip(), None

    def __call__(self, data):
        return dict(self._flag(value) for value in data.split(self.flag_sep))


class entryattr(object):  # pylint: disable=C0103
    """Stores an attribute for an Entry type"""

    def __init__(self, attr):
        self.attr = attr


class InfoDBMeta(type):
    """Metaclass which collects Field attributes"""

    def __new__(mcs, name, bases, attrs):
        fields = {}
        sections = {}
        entry_attrs = {}

        for base in bases:
            if hasattr(base, 'fields'):
                fields.update(base.fields)
            if hasattr(base, 'sections'):
                sections.update(base.sections)
            if hasattr(base, 'entry_attrs'):
                entry_attrs.update(base.entry_attrs)

        for key, value in attrs.items():
            if isinstance(value, Field):
                fields[key] = value
            elif isinstance(value, Section):
                sections[key] = value
            elif isinstance(value, entryattr):
                entry_attrs[key] = value.attr
            else:
                continue  # skip pop
            attrs.pop(key)

        attrs['fields'] = fields
        attrs['sections'] = sections
        attrs['entry_attrs'] = entry_attrs

        entry_name = name + 'Entry'
        entry_keys = sorted(fields.keys() + ['name'])
        base_type = collections.namedtuple(entry_name + 'Base', entry_keys)
        attrs['Entry'] = type(entry_name, (base_type,), entry_attrs)

        return type.__new__(mcs, name, bases, attrs)


# pylint: disable=E1101

class InfoDB(object):
    """Represents an info file database"""

    __metaclass__ = InfoDBMeta

    def __init__(self, filename):
        defaults = {}
        for key, value in self.fields.iteritems():
            if value.default is not None:
                defaults[key] = value.default

        config = ConfigParser.ConfigParser()
        config.readfp(open(filename))

        self.data = {}
        for section in config.sections():
            if section in self.sections:
                entry = {}
                parser = self.sections[section]
                for key, value in config.items(section):
                    key, value = parser(key, value)
                    entry[key] = value
                self.data[section] = entry
            else:
                entry = {'name': section}
                for key, field in self.fields.iteritems():
                    if config.has_option(section, key):
                        entry[key] = field(config.get(section, key))
                    else:
                        entry[key] = defaults[key]
                self.data[section] = self.Entry(**entry)

    def __getitem__(self, name):
        return self.data[name]

    def select(self, condition):
        """Returns a list of Entry for which the condition is True"""
        return [entry for entry in self.data.values() if condition(entry)]

    def choice(self):
        """Returns a random Entry from the InfoDB"""
        return dice.choice(self.data.values())
