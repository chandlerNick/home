# Author: Nick Chandler
# Date: 2/1/2023
# Description: Numpy implementation of a deep neural network and backprop training algorithm for classification/regression.

"""
USAGE (Example): python3 prog1.py -v -train_feat dataset2.train_features.txt -train_target dataset2.train_targets.txt -dev_feat dataset2.dev_features.txt -dev_target dataset2.dev_targets.txt -epochs 3 -learnrate 0.01 -nunits 3 -type C -hidden_act relu -init_range 0.1 -num_classes 3 -mb 0 -nlayers 3
python3 prog1.py -v -train_feat dataset4.train_features.txt -train_target dataset4.train_targets.txt -dev_feat dataset4.dev_features.txt -dev_target dataset4.dev_targets.txt -epochs 3 -learnrate 0.01 -nunits 3 -type C -hidden_act relu -init_range 0.1 -num_classes 2 -mb 0 -nlayers 3


GAMEPLAN:
0 - Parse args and format data
1 - Implement forward behavior --> weights and activation
2 - Backward behavior --> Gradients (Backprop) --> watch minibatch backprop video
3 - Training Loop --> minimize loss function w gradient descent
4 - Glue --> put it all together

"""

# Imports
import numpy as np
import argparse
import sys
import warnings
import traceback

warnings.simplefilter("error")  # Makes the overflow warnings exit

# Definitions of activation functions (float128 was to cope with underflows):
sigmoidNVect = lambda x: 1 / (1 + np.exp(-x, dtype='float128'))
reluNVect = lambda x: np.maximum(0, x, dtype='float128')
tanhNVect = lambda x: np.tanh(x, dtype='float128')

sigmoid = np.vectorize(sigmoidNVect)
relu = np.vectorize(reluNVect)
tanh = np.vectorize(tanhNVect)
softmax = lambda x: np.divide(np.exp(x, dtype='float128'), np.sum(np.exp(x, dtype='float128'), dtype='float128'), dtype='float128')


def dreludx(x):
	if x > 0:
		return 1
	else:
		return 0

# Derivatives of activation functions
ddxrelu = np.vectorize(dreludx)
ddxsig = lambda x: sigmoid(x) * (1 - sigmoid(x))
ddxtanh = lambda x: 1 - np.square(np.tanh(x))  # sech^2 x = 1- tanh^2 x



### Global Variables:

# CL Args
verbose = bool(False)  # BOOL verbose mode
trainFeatFN = None  # STRING training data features file
trainTargetFN = None  # STRING target data file name
devFeatFN = None  # STRING dev file name
devTargetFN = None  # STRING dev target file name
epochs = None  # INT total number of epochs
learnRate = None  # DOUBLE learn rate
numHiddenUnits = None  # INT number of hidden units (same for all layers)
problemMode = None  # STRING problem mode
hiddenActivation = None  # STRING HiddenActive
initRange = None  # DOUBLE Initialization range (+-)
outputDimension = int(1)  # INT output dimension ????? Should this be required?
minibatchSize = int(0)  # INT minibatch size -- 0 for full batch training
numHiddenLayers = int(0)  # INT number of hidden layers (0 for linear model, 1 for a 2-layer NN)
inputDimension = None  # INT input dimension (train)

# Data Matrices
trainFeatMat = None  # Matrix with training features
trainTargetMat = None  # Matrix with training targets
devFeatMat = None  # Matrix with dev features
devTargetMat = None  # Matrix with dev targets

trainSize = None

# Weights & Biases (mats/vects)
weights = []
biases = []


# Saved Z/A vectors -- need to be cleared on each run of feedForward (use the cleanup function)
# Should be tensors: (nHL, nUnits, 1) for pure SGD and (nHL, nUnits, MB) for MB SGD
Z_ls = None
A_ls = None

# Saved feedForward output
A_k = None  # Should be a vector (outDim, 1) for Pure SGD or a Matrix (outDim, MB) for MB SGD/ LossComputation


### Methods start here -- Main is at the bottom


# Description: Parses all command line args
# Returns: A dictionary of command line args
def parseInput():
	# Define the ap object
  ap = argparse.ArgumentParser()

  # add all 10 million arguments
  ap.add_argument('-v', required=False, action='store_true', help='Specify Verbose Mode')
  ap.add_argument('-train_feat', required=True, action='store', help='Specify training data feature filename')
  ap.add_argument('-train_target', required=True, action='store' ,help='Specify the training target data filename')
  ap.add_argument('-dev_feat', required=True, action='store', help='Specify the dev data features filename')
  ap.add_argument('-dev_target', required=True, action='store', help='Specify the dev target data filename')
  ap.add_argument('-epochs', required=True, action='store', help='Specify the number of epochs')
  ap.add_argument('-learnrate', required=True, action='store', help='Specify the learnRate')
  ap.add_argument('-nunits', required=True, action='store', help='Specify the number of hidden units')
  ap.add_argument('-type', required=True, action='store', choices=['C', 'R'], help="Specify problem type")
  ap.add_argument('-hidden_act', required=True, action='store', choices=['sig', 'tanh', 'relu'], help='Specify the hidden activation function')
  ap.add_argument('-init_range', required=True, action='store', help='Specify the initialization range')
  ap.add_argument('-num_classes', required=True, action='store', help='Specify the output dimension')
  ap.add_argument('-mb', required=False, action='store', default=0, help='Specify the minibatch size')
  ap.add_argument('-nlayers', required=False, action='store', default=0, help='Specify the number of hidden layers')
  
  # Parse the arguments
  args = vars(ap.parse_args())
  return args  # send back args dictionary


# Description: Set all global variables based on args dictionary
# Precondition: parseInput() must be called!
# Postcondition: All global variables should be set
def processInput(args):
	# Use global tag for each variable
	global verbose
	global trainFeatFN
	global trainTargetFN
	global devFeatFN
	global devTargetFN
	global epochs
	global learnRate
	global numHiddenUnits
	global problemMode
	global hiddenActivation
	global initRange
	global outputDimension
	global minibatchSize
	global numHiddenLayers

	# Set all variables with the args dict
	verbose = bool(args['v'])  # BOOL verbose mode -- defaults to False
	trainFeatFN = str(args['train_feat'])  # STRING training data features file
	trainTargetFN = str(args['train_target'])  # STRING target data file name
	devFeatFN = str(args['dev_feat'])  # STRING dev file name
	devTargetFN = str(args['dev_target'])  # STRING dev target file name
	epochs = int(args['epochs'])  # INT total number of epochs
	learnRate = float(args['learnrate'])  # DOUBLE learn rate
	numHiddenUnits = int(args['nunits'])  # INT number of hidden units (same for all layers)
	problemMode = str(args['type'])  # STRING problem mode
	hiddenActivation = str(args['hidden_act'])  # STRING HiddenActive
	initRange = float(args['init_range'])  # DOUBLE Initialization range (+-)
	outputDimension = int(args['num_classes'])  # INT output dimension
	minibatchSize = int(args['mb'])  # INT minibatch size -- 0 for full batch training
	numHiddenLayers = int(args['nlayers'])  # INT number of hidden layers (0 for linear model, 1 for a 2-layer NN)


	# Error checking
	if problemMode == 'C' and outputDimension == 1:
		print("Perfect accuracy, only one class", file = sys.stderr)
		sys.exit()
	if problemMode == 'C' and outputDimension == 2:
		outputDimension -= 1  # binary classification
	if outputDimension <= 0:
		print("Improper output dimension " + str(outputDimension), file = sys.stderr)
		sys.exit()
	if minibatchSize < 0:
		print("Improper MB size " + str(minibatchSize), file = sys.stderr)
		sys.exit()
	if epochs <= 0:
		print("Improper number of epochs " + str(epochs), file = sys.stderr)
		sys.exit()
	if learnRate <= 0:
		print("Improper learn rate " + str(learnRate), file = sys.stderr)
		sys.exit()
	if numHiddenUnits < 0 or numHiddenLayers < 0:
		print("Either improper num hidden layers: " + str(numHiddenLayers) + " or num hidden units: " + str(numHiddenUnits), file = sys.stderr)
		sys.exit()
	if numHiddenLayers == 0 and numHiddenUnits > 0:
		print("Cannot have 0 hidden layers and more than 0 hidden units", file = sys.stderr)
		sys.exit()
	if numHiddenLayers > 0 and numHiddenUnits == 0:
		print("Cannot have 0 hidden units with more than 0 hidden layers", file = sys.stderr)
		sys.exit()


# Description: Read data into matrices and set input dimension
# Precondition: processInput must be called!
# Postcondition: 4 fundamental data-matrices are set as well as the inputDimension/trainSize
def readInData():
	# Read training features
	try:
		global trainFeatMat
		trainFeatMat = np.genfromtxt(trainFeatFN, delimiter=' ')
	except:
		print("Error reading trainFeatFN", file = sys.stderr)
		sys.exit(1)
	
	# Read training targets
	try:
		global trainTargetMat
		trainTargetMat = np.genfromtxt(trainTargetFN, delimiter=' ')
		
	except:
		print("Error reading trainFeatFN", file = sys.stderr)
		sys.exit(1)

	# Read dev features
	try:
		global devFeatMat
		devFeatMat = np.genfromtxt(devFeatFN, delimiter=' ')
		print()
	except:
		print("Error reading trainFeatFN", file = sys.stderr)
		sys.exit(1)

	# Read dev targets
	try:
		global devTargetMat
		devTargetMat = np.genfromtxt(devTargetFN, delimiter=' ')
		
	except:
		print("Error reading trainFeatFN", file = sys.stderr)
		sys.exit(1)


	global inputDimension
	global trainSize
	inputDimension = int(np.shape(trainFeatMat)[1])  # INT input dimension (train)
	trainSize = len(trainFeatMat)  # n = number of training observations

	if trainSize == 0:
		print("Cannot operate on 0 inputs.", file=sys.stderr)
		sys.exit(1)


# Description: Formats the target matrices, encoding one hots and forcing the dimensions: (outDim, n)
# Preconditions: ReadInData and prerequisites should have been called
# Postconditions: The dev and train targets are formatted: (outDim, n). One-hots are created for classification
def formatTargets():
	global trainTargetMat
	global devTargetMat

	# Enforce the dimensionality of the inputs
	try:
		if problemMode == 'C':
		# Need onehots
			if outputDimension == 1:
				trainTargetMat = np.transpose(np.reshape(trainTargetMat.astype(int), (-1,1))).astype(int)  # (outDim, n)
				devTargetMat = np.transpose(np.reshape(devTargetMat.astype(int), (-1,1))).astype(int)  # (outDim, n)
			else:
				trainTargetMat = np.transpose(np.eye(outputDimension)[trainTargetMat.astype(int)]).astype(int)   # (outDim, n)
				devTargetMat = np.transpose(np.eye(outputDimension)[devTargetMat.astype(int)]).astype(int)   # (outDim, n)
		else:
			if outputDimension == 1:
				trainTargetMat = np.transpose(np.reshape(trainTargetMat, (-1,1)))
				devTargetMat = np.transpose(np.reshape(devTargetMat, (-1,1)))
			else:
				trainTargetMat = np.transpose(trainTargetMat)
				devTargetMat = np.transpose(devTargetMat)
	except:
		print("There was an error formatting the Targets", file =sys.stderr)
		sys.exit()


# Description: Should initialize the proper number of bias and weight matrices to the global lists
# Preconditions: readInData and all prior functions should have been called 
# Postconditions: The weight and bias lists should have the proper number of correct dimension matrices/vectors
def createWBMats():
	# Declare the weights and biases as global
	global weights
	global biases

	try:
		# 0 hidden layers - Just do a linear model
		if numHiddenLayers == 0:
			W_0 = np.random.uniform(-initRange, initRange, (outputDimension, inputDimension))
			b_0 = np.random.uniform(-initRange, initRange, (outputDimension, 1))
			weights.append(W_0)
			biases.append(b_0)

		# n hidden layers
		else:
			# Input to hidden vect/mat
			weights.append(np.random.uniform(-initRange, initRange, (numHiddenUnits, inputDimension)))
			biases.append(np.random.uniform(-initRange, initRange, (numHiddenUnits, 1)))

			# Hidden to Hidden vect/mat -- won't trigger on 1 hidden unit
			for i in range(1, numHiddenLayers):
				weights.append(np.random.uniform(-initRange, initRange, (numHiddenUnits, numHiddenUnits)))
				biases.append(np.random.uniform(-initRange, initRange, (numHiddenUnits, 1)))

			# Hidden to output vect/mat
			weights.append(np.random.uniform(-initRange, initRange, (outputDimension, numHiddenUnits)))
			biases.append(np.random.uniform(-initRange, initRange, (outputDimension, 1)))
	except:
		print("There was an error initializing weights and biases in createWBMats", file=sys.stderr)
		sys.exit(1)


# Description: Compute a single a-vector
# Inputs: activateFunct - type of activation function, zVector - z vector output of a hidden layer
# Returns: a-vector output of a hidden layer
def computeOneA(activateFunct, zVector):
	try:
		result = zVector
		if activateFunct == "relu":
			result = relu(zVector)
		elif activateFunct == "tanh":
			result = tanh(zVector)
		elif activateFunct == "sig":
			result = sigmoid(zVector)
		elif activateFunct == "identity":
			result = zVector
		elif activateFunct == "softmax":
			result = softmax(zVector)
		else:
			print("Unrecognized hidden activation function.", file=sys.stderr)
			sys.exit(1)
		return result
	except:
		print("Error in computeOneA", file=sys.stderr)
		sys.exit(1)


# Description: Sets the string outActFunc to the proper type of activation function (in feedForward)
# Return: a string (lowercase) corresponding to the proper output activation function for the scenario presented
def outputActivationFunct():
	# Decision logic here
	result = ""
	if problemMode == "R":
		result += "identity"
	else:
		if outputDimension == 1:
			result += "sig"
		else:
			result += "softmax"
	return result  # Should be a string (all lowercase) of the type of activation function


# Description: Computes Z equation for MB > 1
# Returns: a (biasDimension, MB) matrix
def computeMBZ(w, x, b):
	try:
		result = np.add((w @ x), b)
		return np.reshape(result, (-1, x.shape[1]))  # should be (L, MB) matrix
	except:
		print("There was an error in computeMBZ", file=sys.stderr)
		sys.exit()


# Description: Compute Z equation for pure SGD
# Returns: a (biasDimension, 1) vector
def computePureZ(w, x, b):
	try:
		result = np.add((w @ x), b)
		return np.reshape(result, (-1, 1))
	except:
		print("There was an error in computePureZ", file=sys.stderr)
		sys.exit()


# Description: Does feedforward NN behavior (matrix/vector multiplications and activation function)
# Input: Either a vector for purely stochastic or a matrix,  for minibatched; MBStatus == True for MB training, False for sgd
#		Use MBStatus == true for evaluation and loss computation
# 	x mb needs to be (inputDim, MB), pure (inputDim, 1)
# Return: The output of the model, (predictions) A_k
# Postcondition: 4 fundamental Lists are filled with needed vectors -- See above z_ls and a_ls for gradient computation
# Note: 
#			Works for MBStatus == true, outputdim 1 and n for both lms and nns
#			Works for MBStatus == false, output dim 1 and n for both lms and nns
def feedForward(x, MBStatus):
	# Saved layer outputs
	global Z_ls  
	global A_ls
	global A_k

	outActFunc = outputActivationFunct()
	zVectList = []  # Indexed by layers
	aVectList = []  # Indexed by layers
	
	try:
		if MBStatus == False:
		# Purely Stochastic -> output vectors
			if numHiddenLayers == 0:
			# NHlayers == 0
				zVectList.append(computePureZ(weights[0], x, biases[0]))
				A_k = computeOneA(outActFunc, zVectList[0])
			else:
			# NHlayers > 0 -- We are dealing with a neural network

				# Compute input to hidden z/a
				zVectList.append(computePureZ(weights[0], x, biases[0]))
				aVectList.append(computeOneA(hiddenActivation, zVectList[0]))

				# Compute hidden to hidden z/a
				for i in range(1, numHiddenLayers):
					zVectList.append(computePureZ(weights[i], aVectList[i - 1], biases[i]))
					aVectList.append(computeOneA(hiddenActivation, zVectList[i]))

				# Hold onto z vectors from this run in a tensor
				Z_ls = np.asarray(zVectList)
				A_ls = np.asarray(aVectList)

				# Compute hidden to output z/a
				zVectList.append(computePureZ(weights[numHiddenLayers], aVectList[numHiddenLayers - 1], biases[numHiddenLayers]))
				# Save the prediction
				A_k = computeOneA(outActFunc, zVectList[numHiddenLayers])
		else:
		# Minibatched -> output matrices
			if numHiddenLayers == 0:
			# NHlayers == 0
				zVectList.append(computeMBZ(weights[0], x, biases[0]))
				A_k = computeOneA(outActFunc, zVectList[0])
			else:
			# NHlayers > 0 -- We are dealing with a neural network
				# Compute input to hidden z/a
				zVectList.append(computeMBZ(weights[0], x, biases[0]))
				aVectList.append(computeOneA(hiddenActivation, zVectList[0]))
				
				# Compute hidden to hidden z/a
				for i in range(1, numHiddenLayers):
					zVectList.append(computeMBZ(weights[i], aVectList[i - 1], biases[i]))
					aVectList.append(computeOneA(hiddenActivation, zVectList[i]))

				# Save the relevant vectors  --> this is a (MB, numHiddenUnits, numHiddenLayers) tensor
				Z_ls = np.asarray(zVectList)
				A_ls = np.asarray(aVectList)

				# Compute hidden to output z/a
				zVectList.append(computeMBZ(weights[numHiddenLayers], aVectList[numHiddenLayers - 1], biases[numHiddenLayers]))

				# Save the prediction
				A_k = computeOneA(outActFunc, zVectList[numHiddenLayers])
		return A_k  # return the prediction
	except:
		print("Error encountered in feedForward", file = sys.stderr)
		sys.exit(1)


# Description: Applies derivative of activation function to z and element-wise multiplies f(z) and WTDelta
# Return: a Matrix/Vector delta_l-1
def computeOneDelta(z, w, delta): 
	try:
		WlDeltal = w @ delta

		if hiddenActivation == "relu":
			return np.multiply(ddxrelu(z), WlDeltal)
		elif hiddenActivation == "sig":
			return np.multiply(ddxsig(z), WlDeltal) 
		elif hiddenActivation == "tanh":
			return np.multiply(ddxtanh(z), WlDeltal) 
		else:
			print("Unrecognized hiddenActivation function in computeOneDelta", file=sys.stderr)
			sys.exit(1)
	except:
		print("There was an error in computeOneDelta", file=sys.stderr)
		sys.exit(1)



# Description: Computes the partial derivatives wrt loss for the model's weights and biases.
# Input: targetData is in the same dimension as A_k --> pure: (OD, 1), MB: (OD, MB). x is the input to feedForward
# Return: a tuple (ddxLossW, ddxLossBias), lists of partial derivatives wrt each weight/bias matrix/vector
# Notes:
#			Works for MBStatus == true, outputdim 1 and n for both lms and nns
#			Works for MBStatus == false, output dim 1 and n for both lms and  nns
def backProp(x, targets):
	delta_list = []
	ddxLossWeights = []
	ddxLossBiases = []
	MB = minibatchSize
	# Set full batch training
	if MB == 0:
		MB = trainSize

	try:
		# Initial Delta
		delta_list.append(A_k - targets)  # in dimension pure: (outDim, 1) or MB: (outDim, MB)

		delta_counter = 0
		if minibatchSize != 1:
			# if nHL == 0 --> the loop is not triggered
			# Loop Inv: after each iteration, the ddx's of layer l will be in the lists, the delta at l-1 will be in delta list
			for i in range(numHiddenLayers, 0, -1):
				ddxLossWeights.append(np.divide(delta_list[delta_counter] @ np.transpose(A_ls[i - 1, :, :]), MB))
				ddxLossBiases.append(np.divide(delta_list[delta_counter] @ np.ones((MB, 1)), MB))
				delta_list.append(computeOneDelta(Z_ls[i - 1, :, :], np.transpose(weights[i]), delta_list[delta_counter]))  # nextDelta
				delta_counter += 1
			ddxLossWeights.append(np.divide(delta_list[delta_counter] @ np.transpose(x), MB))
			ddxLossBiases.append(np.divide(delta_list[delta_counter] @ np.ones((MB, 1)), MB))

		else:
		# MB == 1
			# if nHL == 0 --> the loop is not triggered
			# Loop Inv: after each iteration, the ddx's of layer l will be in the lists, the delta at l-1 will be in delta list
			for i in range(numHiddenLayers, 0, -1):
				ddxLossWeights.append(delta_list[delta_counter] @ np.transpose(A_ls[i - 1, :, :]))
				ddxLossBiases.append(delta_list[delta_counter])
				delta_list.append(computeOneDelta(Z_ls[i-1, :, :], np.transpose(weights[i]), delta_list[delta_counter]))  # nextDelta
				delta_counter += 1
			ddxLossWeights.append(delta_list[delta_counter] @ np.transpose(x))
			ddxLossBiases.append(delta_list[delta_counter])
		ddxLossWeights.reverse()  # In same order as weight matrice & bias vector lists
		ddxLossBiases.reverse()
		return (ddxLossWeights, ddxLossBiases)
	except:
		print("There was an error in backprop", file=sys.stderr)
		sys.exit(1)


# Description: Clears the saved fields for feedForward
# Postconditions: Z_ls, A_ls, A_k are set to None --> cannot be accesssed until feedForward is called again
def cleanup():
	global Z_ls
	global A_ls
	global A_k

	Z_ls = None
	A_ls = None
	A_k = None


# Description: Updates the weight and bias matrices given the gradients
# Inputs: a list of partial derivatives for weight and a list of pds for biases
# Preconditions: backprop & feed forward should have run
# Postcondition: Weight and bias matrices have been updated
def update(ddxLossW, ddxLossB):
	global weights
	global biases
	try:
		for i in range(0, numHiddenLayers + 1):
			weights[i] = weights[i] - learnRate * ddxLossW[i]
			biases[i] = biases[i] - learnRate * ddxLossB[i]
	except:
		print("There was an error in update", file= sys.stderr)
		sys.exit(1)


# Description: Trains the model and prints the error
# Preconditions: createWBMatrices, FormatData should have been run, feedForward and Backprop should be implemented
# Postconditions: The model should be trained as specified by options. The error should be printed to stderr
def train():
	updateCounter = 1  # for determining where in the input data to feed
	chunkSize = None  # Size of minibatch

	try:
		# Determine MB parameters
		if minibatchSize > 1:
			numMBs = trainSize // minibatchSize 
			chunkSize = minibatchSize
		elif minibatchSize == 0:
			numMBs = 1
			chunkSize = trainSize  # Train size is number of inputs n
		else:
		# Pure SGD
			numMBs = trainSize
			chunkSize = 1

		# Train Loop
		for i in range(epochs):
			trainError = 0
			# Shuffle inputs -- Need to both be in (n,otherdim) when permuted
			global trainTargetMat
			global trainFeatMat
			perm = np.random.permutation(trainSize)
			trainTargetMat = np.transpose(np.transpose(trainTargetMat)[perm])
			trainFeatMat = trainFeatMat[perm]

			for j in range(numMBs):
				# Find slice of MB data
				startIdx = j*chunkSize
				endIdx = (j+1)*chunkSize

				# mb_y is already transposed in the format data method -- must transpose mb_x to match
				mb_x = np.transpose(trainFeatMat[startIdx:endIdx])
				mb_y = trainTargetMat[:, startIdx:endIdx]
				
				# Predictions
				hofx = None
				if minibatchSize == 1:
					hofx = feedForward(mb_x, False)
				else:
					hofx = feedForward(mb_x, True)

				# Gradients
				gradients = backProp(mb_x, mb_y)

				# Update(ddxWeights, ddxBiases)
				update(gradients[0], gradients[1])

				# Clear the fields: A_k, A_ls, Z_ls
				cleanup()

				#Print output 
				if verbose == True:
					trainError = computeLoss(np.transpose(trainFeatMat), trainTargetMat)
					devError = computeLoss(np.transpose(devFeatMat), devTargetMat)
					print("Update " + "{:06d}".format(updateCounter) + ": train=" + "{0:.3f}".format(trainError) + " dev=" + "{0:.3f}".format(devError), file=sys.stderr)
					updateCounter += 1
			if verbose == False:
				trainError = computeLoss(np.transpose(trainFeatMat), trainTargetMat)
				devError = computeLoss(np.transpose(devFeatMat), devTargetMat)
				print("Epoch " + "{:03d}".format(i + 1) + ": train=" + "{0:.3f}".format(trainError) + " dev=" + "{0:.3f}".format(devError), file=sys.stderr)
	except:
		print(traceback.format_exc())
		print("There was an error in train", file=sys.stderr)
		sys.exit(1)


# Description: Computes the accuracy of the model h(x) wrt y
# Input: x input (inDim, n) -- DEFAULT READ IN IS (n, inDim), y targets dimension: (outDim, n)
# Return: a scalar value on the interval [0,1]
# Preconditions: cleanup() sould have been called since the last run of FF
# Postconditions: A_ls, Z_ls, A_k should be cleared --> cleanup should be called.
def computeLoss(x, y):
	result = 0
	hofx = feedForward(x, True)  # do full batch training on the x -- output is same dim as y
	try:
		n = y.shape[1]
		cleanup()
		if problemMode == 'C':
			if outputDimension == 1:
			# Binary class.
				hofx = np.rint(hofx)
				result = np.sum(hofx == y)/n
			else:
			# Multi class
				for i in range(n):
					hofxidx = np.argmax(hofx[:, i])  # index where the highest output is 
					yidx = np.argmax(y[:, i])  # index where the highest output is
					if hofxidx == yidx:  # add to result if pred and actual have the same index
						result += 1
				result = result/n
		else:
		# Problem mode == R
			if outputDimension == 1:
			# 1 dim LR
				result = np.sum(np.square(hofx - y))/n
			else:
			# Multivariate LR
				sumVal = 0
				for i in range(n):
					vecthofx = np.reshape(hofx[:, i], (outputDimension, 1))
					vecty = np.reshape(y[:, i], (outputDimension, 1))
					sumVal += np.square(np.linalg.norm(vecthofx - vecty))
				result = sumVal / n
		return result
	except:
		print("There was an error in computeLoss", file=sys.stderr)
		sys.exit(1)


# Main
if __name__ == "__main__":
	# Setup
	processInput(parseInput())
	readInData()
	formatTargets()
	createWBMats()

	# Execute
	train()
