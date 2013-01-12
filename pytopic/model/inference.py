from pytopic.model.basic import InferenceAlgorithm
from pytopic.model.vanilla import VanillaLDA, gibbs_vanilla

class Gibbs(InferenceAlgorithm):

    implementations = {VanillaLDA: gibbs_vanilla}
