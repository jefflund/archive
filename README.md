pytopic
=======

This is research code, so beware!
This project is mostly meant as a learning experience for myself,
and eventually a play ground to mess around with my own models.
That said, I'm trying to make sure that the code is fairly
clean, documented, and tested.
The toolkit includes (or will include) various things such as:

Implementations of various topic models
Various inference algorithms
Full pipeline for reading and pre-processing various text corpora
Naive Bayes classification, using topic models as feature selection
Document cluster evaluation
Cross reference generation evaluation
Hyper-parameter optimization
A bunch of my own scripts, which can serve as examples

Note that I recommend running PyTopic code using PyPy.
It is (mostly) a drop in replacement for Python.
I have taken some care to make sure that PyTopic works well with the PyPy
jit and have been well rewarded.
I have seen a speed up factor of anywhere from x10 to x25 improvement
in run time!

Currently PyTopic supports only Python 2.7, although conversion to 3.x is planned once PyPy is ported to 3.
