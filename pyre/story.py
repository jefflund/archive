"""Faciliates weak procedural story generation"""

from pyre import dice, data, ui


class NameGenerator(object):
    """A data driven random name generator"""

    def __init__(self, names, order=3, prior=.001, keep=None):
        support = reduce(set.union, names, set())
        self.model = dice.MarkovModel(support, order, prior)
        for name in names:
            self.model.observe(name)

        self._generated = set()
        if keep is None:
            self._keep = lambda x: 2 < len(x) < 12 and x[1:].lower() == x[1:]
        else:
            self._keep = keep

    @classmethod
    def from_file(cls, filename, order=3, prior=.001):
        """Creates a NameGenerator from the given file"""
        names = [name.strip() for name in open(filename).readlines()]
        return cls(names, order, prior)

    def _generate_acceptable(self):
        while True:
            name = ''.join(self.model.generate())
            if self._keep(name):
                return name

    def generate(self, unique=True):
        """Generates a name from the model"""
        name = self._generate_acceptable()
        while unique and name in self._generated:
            name = self._generate_acceptable()
        self._generated.add(name)
        return name


class NameInfoDB(data.InfoDB):
    """Stores name generators trained from an info file"""

    names = data.ListField(sep='\n')
    order = data.IntField(3)
    prior = data.FloatField(.001)

    @data.entryattr
    def __init__(self, names, order, prior, **_):  # pylint: disable=W0231
        self.generator = NameGenerator(names, order, prior)

    @data.entryattr
    def generate(self):
        """Generates a name from a model trained from the entry data"""
        return self.generator.generate()


class Dialog(object):
    """Stores dialog for an NPC"""

    def __init__(self, starter):
        self.starter = starter
        self.responses = {}

    def _ensure_trigger(self, trigger):
        if trigger not in self.responses:
            self.responses[trigger] = []

    def add_response(self, trigger, response):
        """Adds a potential response to the dialog"""
        self._ensure_trigger(trigger)
        self.responses[trigger].append(response)

    def add_responses(self, trigger, responses):
        """Adds responses for a given trigger"""
        self._ensure_trigger(trigger)
        self.responses[trigger].extend(responses)

    def respond(self, query, rstrip='?!.'):
        """Gets a response to the given query"""
        if rstrip:
            query = query.rstrip(rstrip)

        match, answers = -1, ['?']
        for trigger, response in self.responses.iteritems():
            if query.startswith(trigger) and len(trigger) > match:
                match, answers = len(trigger), response
        return dice.choice(answers)

    def converse(self, terminal):
        """Runs the conversation dialog"""
        query = ui.popup_prompt(terminal, self.starter)
        while query and query.lower() not in ['bye', 'goodbye']:
            response = self.respond(query)
            query = ui.popup_prompt(terminal, response, border=True)


class DialogInfoDB(data.InfoDB):
    """Stories data needed to create instances of Dialog"""

    filters = data.FlagsField(default={})
    trigger = data.ListField()
    text = data.ListField(sep='\n')

    def make_dialog(self, starter, flags):
        """Constructs a dialog with the given parameters"""
        dialog = Dialog(starter)
        for section in self.data.itervalues():
            use_section = True
            for flag, value in section.filters.iteritems():
                if flags[flag] != value:
                    use_section = False
            if use_section:
                for trigger in section.trigger:
                    dialog.add_responses(trigger, section.text)
        return dialog
