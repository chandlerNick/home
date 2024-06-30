#All R Code for 447 Project

# In this file, we have included all the R code used in generating the 
# results we document in the writeup.
# There is additional python code but it will be attached as a separate file

#########################
### Merge Data Script ###
#########################
# This is how we merged the two datasets into one file

library(dplyr)
library(caret)

data <- read.csv("./realtor-data.zip.csv", header= TRUE)
incomeData <- read.csv("./kaggle_income.csv" ,header = TRUE)


data <- data %>%
  filter(status != "for_sale") %>%
  filter(status != "ready_to_build")

# Real estate data info
head(data)
print(unique(data$status))
dim(data)
print(unique(data$state))



# income data info
head(incomeData)

incomeData$zip_code <- incomeData$Zip_Code
incomeData$Zip_Code <- NULL


# RE Data reformatting
data$state <- tolower(data$state)
data$city <- tolower(data$city)

# income Data reformatting

incomeData$State_Name <- tolower(incomeData$State_Name)
incomeData$City <- tolower(incomeData$City)

data$LocationIdentifier <- paste(data$state, data$city, data$zip_code ,sep = "_")
incomeData$LocationIdentifier <- paste(incomeData$State_Name, incomeData$City, incomeData$zip_code, sep = "_")

dim(incomeData)
print(length(unique(incomeData$LocationIdentifier)))

incomeData <- incomeData[!duplicated(incomeData$LocationIdentifier),]

dim(incomeData)
print(length(unique(incomeData$LocationIdentifier)))

# left join data and incomeData (keep the real estate data) on zip code
mergedData <- left_join(data, incomeData, by = "LocationIdentifier")

head(mergedData)
na.omit(mergedData)
dim(mergedData)

write.csv(mergedData, file = "merged_data.csv", col.names = TRUE, row.names = FALSE)





#########################
### Linear Regression ###
#########################
# This is how we made the histogram and fit our baseline model

library(ggpubr)
library(factoextra)


data <- na.omit(read.csv("./merged_data.csv",header = TRUE))
data <- data.frame(data$price, data$bed, data$bath, data$acre_lot, data$house_size, data$Mean, data$Stdev, data$zip_code.x)
colnames(data) <- c("price", "bed", "bath", "acre_lot", "house_size", "mean_income","stdev_income", "zip_code")

par(mar=c(5,5,5,5))

# All features linear model
model <- lm(data$price ~ data$bed + data$bath + data$house_size + data$acre_lot + data$mean_income + data$stdev_income + data$zip_code)
summary(model)$coefficients[5,4]  # Coefficient assoc'd with acre_lot
summary(model)

# Setup for individual Linear Models
counts <- table(data$zip_code)
zips <- unique(data$zip_code)
RSqd <- c()
bestZips <- c()
p_values <- c()



# Loop of linear models
model <- for (i in zips) {
  data2 <- data[data$zip_code == i, ]  # Split data on Zip code
  if (dim(data2)[1] > 100) {  # If there are more than 100 observations, create a model
    model <- lm(data2$price ~ data2$bed + data2$bath + data2$house_size + data2$acre_lot + data2$mean_income + data2$stdev_income)
    score <- summary(model)$r.squared
    RSqd <- c(RSqd, score)
    p_values <- c(p_values, summary(model)$coefficients[5,4])
  }
  
}

# Analysis of R-Sqd
hist(RSqd)
#print(RSqd[RSqd > 0.9])
#print(length(RSqd[RSqd > 0.75]))

# Analysis of p-values assoc'd with acre_lot
print(length(p_values[p_values > 0.05])/length(p_values))
print(length(p_values))





##############################
### Elastic Net Regression ###
##############################
# This is how we obtained the elastic net regression results

# Elastic net regression on data
set.seed(123)
n<-dim(data)[1]
indices<- sample(1:n, size=floor(0.8*n))
indices
train_data<- data[indices, ]
test_data<- data[-indices, ]

#View(train_data)
x_train <- as.matrix(train_data[, -1]) # Exclude price
y_train <- train_data$price
x_test <- as.matrix(test_data[, -1])
y_test <- test_data$price
alpha=0.5
cv_model <- cv.glmnet(x_train, y_train, alpha = alpha)
dim(train_data)
dim(test_data)
best_lambda<-cv_model$lambda.min
elasticnetmodel<-glmnet(x_train,y_train,alpha=alpha,lambda=best_lambda)
predictions<-predict(elasticnetmodel, s=best_lambda, newx= x_test)

sst <- sum((y_test - mean(y_test))^2) # Total sum of squares
sse <- sum((y_test - predictions)^2) # Residual sum of squares
r_squared <- 1 - (sse / sst)
print(paste("R-squared:", r_squared)) #0.228

coef_summary<-as.matrix(coef(elasticnetmodel,s=best_lambda))
coef_summary

# Elastic net regression on principal components 
price_vec<-data$price
pca_data<-data[,-1]
pca_result <- prcomp(pca_data, center = TRUE, scale. = TRUE)
pca_scores<-pca_result$x

set.seed(123)
n<-dim(pca_scores)[1]
indices<- sample(1:n, size=floor(0.8*n))
train_pca_data<- pca_scores[indices, ]
test_pca_data<- pca_scores[-indices, ]

x_train <- as.matrix(train_pca_data[, -1]) # Exclude price
y_train <- price_vec[indices]
x_test <- as.matrix(test_pca_data[, -1])
y_test <- price_vec[-indices]
alpha=0.5
cv_model <- cv.glmnet(x_train, y_train, alpha = alpha)

best_lambda<-cv_model$lambda.min
elastic_net_model<-glmnet(x_train,y_train,alpha=alpha,lambda=best_lambda)
predictions<-predict(elastic_net_model, s=best_lambda, newx= x_test)

sst <- sum((y_test - mean(y_test))^2) # Total sum of squares
sse <- sum((y_test - predictions)^2) # Residual sum of squares
r_squared <- 1 - (sse / sst)
print(paste("R-squared:", r_squared))

#r^2 is 0.01083

coef_summary<-as.matrix(coef(elastic_net_model,s=best_lambda))
coef_summary





###########
### PCA ###
###########
# PCA was done with the following code

# Graphics output setup
par(mar=c(5,5,5,5))
par(mfrow=c(1,1))

# Read in data
data <- na.omit(read.csv("./merged_data.csv",header = TRUE))
data <- data.frame(data$bed, data$bath, data$acre_lot, data$house_size, data$Mean, data$Stdev)
colnames(data) <- c("bed", "bath", "acre_lot", "house_size", "mean_income","stdev_income")

# Do PCA
pca_result <- prcomp(data, center = TRUE, scale = TRUE)

# Output Results
summary(pca_result)
print(pca_result$rotation)


var_explained <- pca_result$sdev^2 / sum(pca_result$sdev^2)
plot(1:6, var_explained, main='Scree Plot', xlab = "Principal Component", ylab= 'Proportion of Variance Explained', pch=20)
lines(var_explained)

var_plot <- cumsum(var_explained)
plot(1:6, var_plot, main='Cumulative PVE Plot', xlab = "Principal Component", ylab= 'Cumulative Proportion of Variance Explained', pch=20)
lines(var_plot)
abline(h=0.9)

# visualize
biplot_actual <- function(first, second, pca_result) {
  loadings <- pca_result$rotation
  xName <- paste("PC", first)
  yName <- paste("PC", second)
  theTitle <- paste(xName, " vs. ", yName)
  filePath <- paste(theTitle, ".pdf", sep="")
  pdf(filePath)
  plot(loadings[, first], loadings[, second], type = "n", xlab = xName, ylab = yName, main = theTitle, xlim=c(-1,1), ylim=c(-1,1))
  arrows(0, 0, loadings[, first], loadings[, second], col = "red", length = 0.1)
  text(loadings[, first], loadings[, second], labels = rownames(loadings), col = "blue", pos = 4)
  dev.off()
}

biplot_actual(1,2,pca_result)
biplot_actual(1,3,pca_result)
biplot_actual(1,4,pca_result)
biplot_actual(2,3,pca_result)
biplot_actual(2,4,pca_result)
biplot_actual(3,4,pca_result)



################################
### Random Forest Regression ###
################################
# The random forest regression was done with the following code
library(randomForest)

# Setup
data <- na.omit(read.csv("./merged_data.csv",header = TRUE))
data <- data.frame(data$price, data$bed, data$bath, data$acre_lot, data$house_size, data$Mean, data$Stdev, data$zip_code.x)
colnames(data) <- c("price", "bed", "bath", "acre_lot", "house_size", "mean_income","stdev_income", "zip_code")

par(mar=c(5,5,5,5))

sample_size <- floor(nrow(data))
reduction_indices <- sample(seq_len(nrow(data)), size = sample_size)

reduct_data <- data[reduction_indices,]
no_zip_data <- reduct_data
no_zip_data$zip_code.x <- NULL

sample_size <- floor(0.8 * nrow(reduct_data))
train_indices <- sample(seq_len(nrow(reduct_data)), size = sample_size)

train_data <- reduct_data[train_indices, ]
test_data <- reduct_data[-train_indices, ]

no_zip_train_data <- no_zip_data[train_indices, ]
no_zip_test_data <- no_zip_data[-train_indices, ]



print("Spatial Data")
rsq <- c()
for (i in (1:10)) {
  rf_model <- randomForest(price ~ ., data = train_data, ntree = i*10, mtry = 2, importance = TRUE)
  
  # Predict on test data
  predictions <- predict(rf_model, test_data)
  
  SSR <- sum((test_data$price - predictions)^2)
  SST <- sum((test_data$price - mean(test_data$price))^2)
  
  # Report R^2
  print(1 - SSR/SST)
  rsq <- c(rsq, 1 - SSR/SST)
}


plot(10*(1:10), rsq, main= "N-Trees vs R-sqd - Spatial Data", xlab="N-Trees", ylab= "R-Squared")

print(rsq)


print("No Spatial Data")
rsq <- c()
for (i in (1:10)) {
  rf_model <- randomForest(price ~ ., data = no_zip_train_data, ntree = i*10, mtry = 2, importance = TRUE)
  
  # Predict on test data
  predictions <- predict(rf_model, no_zip_test_data)
  
  SSR <- sum((no_zip_test_data$price - predictions)^2)
  SST <- sum((no_zip_test_data$price - mean(no_zip_test_data$price))^2)
  
  # Report R^2
  print(1 - SSR/SST)
  rsq <- c(rsq, 1 - SSR/SST)
}


plot(10*(1:10), rsq, main= "N-Trees vs R-sqd - No Spatial Data", xlab="N-Trees", ylab= "R-Squared")
print(rsq)

# R-sq scores
# Spatial
# 0.6028382 0.6290209 0.6246975 0.6096437 0.6187748 0.6226189 0.6125671 0.6212720 0.6216183 0.6275240

# No Spatial
# 0.6222768 0.5949236 0.6320915 0.6196061 0.6121138 0.6182986 0.6073687 0.6171345 0.6195885 0.6244061



