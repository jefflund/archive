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
  * also lets you reuse work as you compute each element of gradient function

dz/dx = dz/dy * dy/dx

f(x, y, z) = (x + y) * z
q = x + y
f = q * z
   *
 +   z
x y

=Tensorflow=
* placeholder - input to a computation graph
  * Example: x = tf.placeholder(tf.float32, shape=[None, D])
  * None in shape means variable size
* variable - the parameters we want to learn
  * Example: w1 = tf.Variable(1e-3 * np.random.randn(D, H).astype(np.float32))
* we don't actually calculate - we declare steps in a computation graph
  * Example: a = tf.matmul(x, w1)
* optimizers are just part of the computation graph
  * Example: train_step = tf.train.GradientDescentOptimizer(learning_rate).minimize(loss)

* summary writer saves things like computation graphs for Tensorboard
  * Example: w = tf.summary.FileWriter('./tf_logs', sess.graph)
  * Group parts of graph with name scopes and names:
    * with tf.namescope('layer'):
          w = tf.Variable(..., name='W')

=Activation=
* Use ReLU
* Try Leaky ReLU / Maxout / ELU
  * In theory, Leaky ReLU should work better; in practice, about the same
* Try tanh, but don't expect anything
* Screw sigmoid

=Tensorboard=
* summaries, written to file, can be opened by tensorboard

=Debugging=
* overfit
  * start out with small subset of data, should be able to quickly overfit
  * turn off regularization and make sure loss is reasonable
* learning rate
  * very high learning rate -> nan, or increasing loss
  * high learning rate -> flattens early, but too high
  * low learning rate -> never flattens
* initialization
  * bad initialization -> sigmoidal loss
  * diminishing/exploding gradient -> small/big weight initialization
    * use xavier initialization (divide by 2 for relu)
  * whiten data by subtracting mean and dividing by mean (makes data gaussian)
* gaps between train and test accuracy
  * overfitting -> big gap (increase regularization)
  * low model capacity -> no gap

=Regularization=
* L2, L1, Elastic Net (L2+L1) are old school
==DNN Regularization: Dropout==
* Randomly set some neurons to zero in the forward pass
* Various interpretations of what this is doing:
  * Hinton: for overfit, neurons must conspire - dropout prevents conspiracy
  * Alternatively: dropout is training large ensemble of models
* Can use a mask which is passed in via feed_dict
* How to test?
  * Integrate out noise with Monte Carlo approximation
  * Leave all neurons on, but re-weighted so expected sum is same as training
    * Approximately equivalent to using all models in the ensemble

* DropConnect solves structured problems of multiplying rank 1 matrix multiply
  * Makes sure the neurons dropped don't have structure between layers
