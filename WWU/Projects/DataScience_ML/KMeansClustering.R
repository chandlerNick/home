# Do K-Means Clustering
# Attention:
#   There must be at least two of each class in the pre-assigned labels
# Inputs:
#   - data, an nxp matrix of n observations and p features
#   - labels of values (one to k), length n
#   - k (number of clusters)
# Outputs: 
#   - A list of (labels, centers)
#     - vector of labels, each position denotes an observation, value denotes cluster, length n
#     - a matrix of the final centers, kxp matrix
KMeansCluster <- function(data, labels, k) {
  # Algorithm:
  # While not converged
  #   Compute centers
  #   Compute distances over all n points
  #   Assign clusters based on min distance from centers
  
  # Error Checking -- all points labeled, labels are valid, k <= n
  stopifnot(nrow(data) == length(labels), max(labels) <= k, k <= nrow(data), nrow(data) > 1)
  
  # Computes the centers for each of the k clusters
  # Input: 
  #   data - nxp matrix of n observations, p features
  #   labels of values from one to k, length n
  # Output:
  #   centers - kxp matrix of p-dimensional centers for each k
  computeCenters <- function(data, labels, k) {
    centers <- c()
    # split data into k groups based on label
    for (i in 1:k) {
      kthData <- data[labels==i,]  # Select rows with label == i
      kthCenter <- c()
      
      for (j in 1:ncol(data)) {
        kthCenter <- c(kthCenter, mean(kthData[,j]))
      }
      
      # Compute the mean along each of p dimensions of the data
      kthCenter <- colMeans(kthData)
      
      # Append the kth center to the centers matrix
      centers <- rbind(centers, kthCenter)
    }
    row.names(centers) <- NULL
    return(centers)
  }
  
  
  # Computes the distances of each data point to each center, assigns cluster with smallest distance
  # Inputs:
  #   data - an n x p matrix, n observations of p dimensional data
  #   centers - a k x p matrix, k centers in p dimensional space
  # Output:
  #   assignment - n dimensional vector, denoting the cluster each obs. now belongs
  assignClusters <- function(data, centers) {
    assignment <- c()
    
    # Input - two vectors
    # Output - a scalar of distance
    euclideanDistance <- function(a, b) sqrt(sum((a - b)^2))
    
    print("centers")
    print(centers)
    print("iteration")
    
    
    # For each datapoint
    for (i in 1:nrow(data)) {
      minDist <- .Machine$double.xmax
      bestClust <- NULL
      for (j in 1:nrow(centers)) {  # For each center
        # compute distance to center
        dist <- dist(rbind(data[i,], centers[j,]))
        
        # If smaller, assign datapoint to jth cluster
        if (dist < minDist) {
          minDist <- dist
          bestClust <- j
        }
      }
      
      # Add the bestClust to the assignment
      assignment <- c(assignment, bestClust)
    }
    print(assignment)
    return(assignment)
  }
  
  prevResult <- labels
  # Fit loop
  while (TRUE) {
    # Compute centers
    centers <- computeCenters(data, labels, k)
    
    # Assign Data points
    result <- assignClusters(data, centers)
    
    # Check for convergence
    if (sum(result == prevResult) == nrow(data)) {
      return(list(result, centers))
    } else {
      prevResult <- result
    }
  }
}