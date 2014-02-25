"""Contain logging functionality"""

import collections
import re


class Logger(object):
    """Allows logging of formated messages"""

    class Message(object):
        """Holds the text and stats of a log message"""

        def __init__(self, text):
            self.text = text
            self.count = 1
            self.seen = False

        def __str__(self):
            if self.count <= 1:
                return self.text
            else:
                return '{} (x{})'.format(self.text, self.count)

    def __init__(self, size=None):
        self.history = collections.deque(maxlen=size)

    def history_size(self):
        """Gets the current history size for the Logger"""
        return self.history.maxlen

    def resize(self, size):
        """Sets the history_size, truncating the history if needed"""
        self.history = collections.deque(self.history, size)

    def clear(self):
        """Clears the history"""
        self.history.clear()

    def log(self, message, *args):
        """Adds a formated message to the log history"""
        text = _format_message(message, *args)

        if self.history and self.history[-1].text == text:
            message = self.history[-1]
            message.count += 1
            message.seen = False
        else:
            self.history.append(Logger.Message(text))

    def retrieve(self):
        """Iterates through the history, marking messages as seen afterwords"""
        for message in self.history:
            yield message
            message.seen = True


_ARTICLES = {'the', 'a'}
_IRREGULAR_VERBS_SECOND = {'be': 'are'}
_IRREGULAR_VERBS_THIRD = {'do': 'does',
                          'go': 'goes',
                          'can': 'can',
                          'have': 'has',
                          'be': 'is'}
_ENDING_PUNCTUATION = {'.', '!', '?'}


def _includes_article(name):
    for article in _ARTICLES:
        if name.startswith(article + ' '):
            return True
    return False


def _get_name(noun):
    name = str(noun)
    if name == 'you':
        return 'you'
    elif _includes_article(name) or name[0].isupper():
        return name
    else:
        return 'the ' + name


def _get_reflexive(noun):
    name = str(noun)
    if name == 'you':
        return 'yourself'
    else:
        return 'itself'


def _regular_conjugation(verb):
    return verb + ('es' if verb.endswith('s') else 's')


def _get_verb(verb, subject):
    verb = str(verb).split()
    verb, phrase = verb[0], verb[1:]

    if str(subject) == 'you':
        verb = _IRREGULAR_VERBS_SECOND.get(verb, verb)
    else:
        verb = _IRREGULAR_VERBS_THIRD.get(verb, _regular_conjugation(verb))

    return ' '.join([verb] + phrase)


def _format_message(message, *fmt):
    fmt = iter(fmt)
    objects = []  # subject should always be objects[0]

    def _replace(match):
        spec = match.group()
        if spec == '%s':  # subject
            noun = next(fmt)
            objects.append(noun)
            objects[0] = noun
            return _get_name(noun)
        elif spec == '%o':  # object
            noun = next(fmt)
            objects.append(noun)
            if noun is objects[0]:
                return _get_reflexive(noun)
            else:
                return _get_name(noun)
        elif spec == '%v':  # verb
            verb = next(fmt)
            return _get_verb(verb, objects[0])
        elif spec == '%l':  # literal
            return str(next(fmt))
        else:  # assume inlined verb
            return _get_verb(spec[1:-1], objects[0])

    message = re.sub('%s|%o|%v|%l|<.+?>', _replace, message)
    message = message[0].upper() + message[1:]
    if message[-1] not in _ENDING_PUNCTUATION:
        message += '.'

    return message
