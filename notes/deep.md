=Deep Learning=
Why DNN so powerful?
* Stat model learns patterns and clusters
* DNN learn computation
Why DDN now vs previous NN?
* New activation functions
* Better regularization
* Better initialization
* GPUs 10x speedup vs CPU
* More data (~7 OOM)
* More parameters

=Missed Stuff=
lol loss="measure of how sad I am"
review hinge loss (SVM)
review softmax - scores are unnormalized log probabilities

=Loss Functions (Part 2)=
* Loss for softmax -log probability of true class
  * Aims to maximize probability of true class, thus the negative
  * Log is monotonic, so we can use it without changing ranks
* Need non-linear activation, or can't do multiple layers
  * (W_1 * W_2) * x is just a different linear transform
* Activations functions
  * sigmoid - only good in small networks
  * tanh - similar squashing but between -1 and 1
  * ReLU - state of the art activation
    * Varients: Leaky ReLU, ELU (exponential linear unit)
* Activation functions are applied element-wise

=Computation Graphs=
* Computation Graphs - course analysis of computational dependencies
* It is basically a parse tree for a complex mathematical expression
* Tensorboard - part of Tensorflow to generate these graphs

=Derivatives and Gradients=
* gradient is the vector of partial derivatives
* gradient gives us the direction of steepest ascent
* chain rule of derivatives lets you break down complicated functions
