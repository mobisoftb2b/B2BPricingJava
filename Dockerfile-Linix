# Use an official OpenJDK runtime as a parent image
FROM openjdk:12-jdk-oracle

# set shell to bash
# source: https://stackoverflow.com/a/40944512/3128926


# Set the working directory to /app
WORKDIR /app

# Copy the fat jar into the container at /app
COPY /target/B2BPricing.jar /app

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run jar file when the container launches
CMD ["java", "-jar", "B2BPricing.jar"]


RUN mkdir /myvol
VOLUME /myvol